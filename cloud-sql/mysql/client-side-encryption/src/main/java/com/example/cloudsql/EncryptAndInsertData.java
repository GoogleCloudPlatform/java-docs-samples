/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.cloudsql;

// [START cloud_sql_mysql_encrypt_insert]
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.KmsClients;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.integration.gcpkms.GcpKmsClient;
import com.google.crypto.tink.proto.KeyTemplate;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import javax.sql.DataSource;

class EncryptAndInsertData {

  public static void main(String[] args) throws GeneralSecurityException, SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String dbUser = "your-username"; // e.g. "root", "mysql"
    String dbPass = "your-password";
    String dbName = "your-database-name";
    String cloudSqlConnectionName = "project:region:instance";
    String kmsUri = "gcp-kms://" + "your-kms-uri";
    String team = "TABS";
    String tableName = "votes";
    String email = "hello@example.com";

    // Initialize database connection pool and create table
    DataSource pool = createConnectionPool(dbUser, dbPass, dbName, cloudSqlConnectionName);
    createTable(pool, tableName);

    // Initialize envelope AEAD
    Aead envAead = getEnvelopeAead(kmsUri);

    encryptAndInsertData(pool, envAead, tableName, team, email);
  }

  public static void encryptAndInsertData(DataSource pool, Aead envAead, String tableName,
      String team, String email)
      throws GeneralSecurityException, SQLException {

    try (Connection conn = pool.getConnection()) {
      String stmt = String.format(
          "INSERT INTO %s (team, time_cast, voter_email) VALUES (?, ?, ?);", tableName);
      try (PreparedStatement voteStmt = conn.prepareStatement(stmt);) {
        voteStmt.setString(1, team);
        voteStmt.setTimestamp(2, new Timestamp(new Date().getTime()));

        // Use the envelope AEAD primitive to encrypt the email, using the team name as
        // associated data
        byte[] encrypted_email = envAead.encrypt(email.getBytes(), team.getBytes());
        voteStmt.setBytes(3, encrypted_email);

        // Finally, execute the statement. If it fails, an error will be thrown.
        voteStmt.execute();
        System.out.println(String.format("Successfully inserted row into table %s", tableName));
      }
    }
  }

  public static Aead getEnvelopeAead(String kmsUri) throws GeneralSecurityException {
    AeadConfig.register();

    // Generate a new envelope key template, then generate key material.
    KeyTemplate kmsEnvKeyTemplate = AeadKeyTemplates
        .createKmsEnvelopeAeadKeyTemplate(kmsUri, AeadKeyTemplates.AES128_GCM);
    KeysetHandle keysetHandle = KeysetHandle.generateNew(kmsEnvKeyTemplate);

    // Register the KMS client.
    KmsClients.add(new GcpKmsClient()
        .withDefaultCredentials());

    // Create envelope AEAD primitive from keysetHandle
    return keysetHandle.getPrimitive(Aead.class);
  }

  public static DataSource createConnectionPool(String dbUser, String dbPass, String dbName,
      String cloudSqlConnectionName) throws GeneralSecurityException {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(String.format("jdbc:mysql:///%s", dbName));
    config.setUsername(dbUser);
    config.setPassword(dbPass);
    config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.mysql.SocketFactory");
    config.addDataSourceProperty("cloudSqlInstance", cloudSqlConnectionName);
    DataSource pool = new HikariDataSource(config);
    return pool;
  }

  public static void createTable(DataSource pool, String tableName) throws SQLException {
    // Safely attempt to create the table schema.
    try (Connection conn = pool.getConnection()) {
      String stmt = String.format("CREATE TABLE IF NOT EXISTS %s ( "
              + "vote_id SERIAL NOT NULL, time_cast timestamp NOT NULL, team CHAR(6) NOT NULL,"
              + "voter_email VARBINARY(255), PRIMARY KEY (vote_id) );", tableName);
      try (PreparedStatement createTableStatement = conn.prepareStatement(stmt);) {
        createTableStatement.execute();
      }
    }
  }
}
// [END cloud_sql_mysql_encrypt_insert]