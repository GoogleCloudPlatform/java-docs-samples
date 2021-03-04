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

public class CloudSqlConnectionPool {

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