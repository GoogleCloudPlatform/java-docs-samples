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

// [START cloud_sql_mysql_query_decrypt]

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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.sql.DataSource;

class QueryAndDecryptData {

  public static void main(String[] args) throws GeneralSecurityException, SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String dbUser = "your-username"; // e.g. "root", "mysql"
    String dbPass = "your-password";
    String dbName = "your-database-name";
    String cloudSqlConnectionName = "project:region:instance";
    String kmsUri = "gcp-kms://" + "your-kms-uri";
    String tableName = "votes";

    // Initialize database connection pool
    DataSource pool = createConnectionPool(dbUser, dbPass, dbName, cloudSqlConnectionName);

    // Initialize envelope AEAD
    Aead envAead = getEnvelopeAead(kmsUri);

    queryAndDecryptData(pool, envAead, tableName);
  }

  public static void queryAndDecryptData(DataSource pool, Aead envAead, String tableName)
      throws GeneralSecurityException, SQLException {

    try (Connection conn = pool.getConnection()) {
      String stmt = String.format(
          "SELECT team, time_cast, voter_email FROM %s ORDER BY time_cast DESC LIMIT 5", tableName);
      try (PreparedStatement voteStmt = conn.prepareStatement(stmt);) {
        ResultSet voteResults = voteStmt.executeQuery();

        System.out.println("Team\tTime Cast\tEmail");
        while (voteResults.next()) {
          String team = voteResults.getString(1);
          Timestamp timeCast = voteResults.getTimestamp(2);

          // Use the envelope AEAD primitive to decrypt the email, using the team name as
          // associated data
          String email = new String(envAead.decrypt(voteResults.getBytes(3), team.getBytes()));

          System.out.println(String.format("%s\t%s\t%s", team, timeCast, email));
        }
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
}
// [END cloud_sql_mysql_query_decrypt]