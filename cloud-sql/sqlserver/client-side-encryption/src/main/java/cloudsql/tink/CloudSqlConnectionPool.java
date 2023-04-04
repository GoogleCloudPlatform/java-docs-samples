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

// [START cloud_sql_sqlserver_cse_db]

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

public class CloudSqlConnectionPool {

  public static DataSource createConnectionPool(String dbUser, String dbPass, String dbName,
      String instanceConnectionName) {
    HikariConfig config = new HikariConfig();
    config.setDataSourceClassName("com.microsoft.sqlserver.jdbc.SQLServerDataSource");
    config.setUsername(dbUser); // e.g. "root", "sqlserver"
    config.setPassword(dbPass); // e.g. "my-password"
    config.addDataSourceProperty("databaseName", dbName);

    // The Cloud SQL Java Connector provides SSL encryption so 
    // it should be disabled at the driver level
    config.addDataSourceProperty("encrypt", "false");

    config.addDataSourceProperty("socketFactoryClass",
        "com.google.cloud.sql.sqlserver.SocketFactory");
    config.addDataSourceProperty("socketFactoryConstructorArg", instanceConnectionName);
    DataSource pool = new HikariDataSource(config);
    return pool;
  }

  public static void createTable(DataSource pool, String tableName) throws SQLException {
    // Safely attempt to create the table schema.
    try (Connection conn = pool.getConnection()) {

      String stmt = String.format("IF NOT EXISTS("
          + "SELECT * FROM sysobjects WHERE name='%s' and xtype='U')"
          + "CREATE TABLE %s ("
          + "vote_id INT NOT NULL IDENTITY,"
          + "time_cast DATETIME NOT NULL,"
          + "team VARCHAR(6) NOT NULL,"
          + "voter_email VARBINARY(255)"
          + "PRIMARY KEY (vote_id));", tableName, tableName);
      try (PreparedStatement createTableStatement = conn.prepareStatement(stmt);) {
        createTableStatement.execute();
      }
    }
  }
}
// [END cloud_sql_sqlserver_cse_db]
