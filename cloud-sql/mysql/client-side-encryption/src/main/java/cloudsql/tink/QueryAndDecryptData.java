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

// [START cloud_sql_mysql_query_decrypt]

import com.google.crypto.tink.Aead;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.sql.DataSource;

public class QueryAndDecryptData {

  public static void main(String[] args) throws GeneralSecurityException, SQLException {
    // Saving credentials in environment variables is convenient, but not secure - consider a more
    // secure solution such as Cloud Secret Manager to help keep secrets safe.
    String dbUser = System.getenv("DB_USER"); // e.g. "root", "mysql"
    String dbPass = System.getenv("DB_PASS"); // e.g. "mysupersecretpassword"
    String dbName = System.getenv("DB_NAME"); // e.g. "votes_db"
    String cloudSqlConnectionName =
        System.getenv("CLOUD_SQL_CONNECTION_NAME"); // e.g. "project-name:region:instance-name"
    String kmsUri = System.getenv("CLOUD_KMS_URI"); // e.g. "gcp-kms://projects/...path/to/key

    String tableName = "votes123";

    // Initialize database connection pool and create table if it does not exist
    // See CloudSqlConnectionPool.java for setup details
    DataSource pool = CloudSqlConnectionPool.createConnectionPool(dbUser, dbPass, dbName, cloudSqlConnectionName);
    CloudSqlConnectionPool.createTable(pool, tableName);

    // Initialize envelope AEAD
    // See CloudKmsEnvelopeAead.java for setup details
    Aead envAead = CloudKmsEnvelopeAead.getEnvelopeAead(kmsUri);

    // Insert row into table to test
    // See EncryptAndInsert.java for setup details
    EncryptAndInsertData
        .encryptAndInsertData(pool, envAead, tableName, "SPACES", "hello@example.com");

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
}
// [END cloud_sql_mysql_query_decrypt]