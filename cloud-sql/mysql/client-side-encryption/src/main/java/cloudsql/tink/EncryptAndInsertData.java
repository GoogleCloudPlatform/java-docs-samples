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

package cloudsql.tink;

// [START cloud_sql_mysql_encrypt_insert]

import com.google.crypto.tink.Aead;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import javax.sql.DataSource;

public class EncryptAndInsertData {

  public static void main(String[] args) throws GeneralSecurityException, SQLException {
    // Saving credentials in environment variables is convenient, but not secure - consider a more
    // secure solution such as Cloud Secret Manager to help keep secrets safe.
    String dbUser = System.getenv("DB_USER"); // e.g. "root", "mysql"
    String dbPass = System.getenv("DB_PASS"); // e.g. "mysupersecretpassword"
    String dbName = System.getenv("DB_NAME"); // e.g. "votes_db"
    String cloudSqlConnectionName =
        System.getenv("CLOUD_SQL_CONNECTION_NAME"); // e.g. "project-name:region:instance-name"
    String kmsUri = System.getenv("CLOUD_KMS_URI"); // e.g. "gcp-kms://projects/...path/to/key

    String team = "TABS";
    String tableName = "votes";
    String email = "hello@example.com";

    // Initialize database connection pool
    DataSource pool = CloudSqlConnectionPool.createConnectionPool(dbUser, dbPass, dbName, cloudSqlConnectionName);

    // Create table if it does not exist
    CloudSqlConnectionPool.createTable(pool, tableName);

    // Initialize envelope AEAD
    Aead envAead = CloudKmsEnvelopeAead.getEnvelopeAead(kmsUri);

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
        byte[] encryptedEmail = envAead.encrypt(email.getBytes(), team.getBytes());
        voteStmt.setBytes(3, encryptedEmail);

        // Finally, execute the statement. If it fails, an error will be thrown.
        voteStmt.execute();
        System.out.println(String.format("Successfully inserted row into table %s", tableName));
      }
    }
  }
}
// [END cloud_sql_mysql_encrypt_insert]