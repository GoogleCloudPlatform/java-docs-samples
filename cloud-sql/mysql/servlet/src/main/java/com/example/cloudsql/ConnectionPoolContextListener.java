/*
 * Copyright 2018 Google LLC
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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

@WebListener("Creates a connection pool that is stored in the Servlet's context for later use.")
public class ConnectionPoolContextListener implements ServletContextListener {

  // Saving credentials in environment variables is convenient, but not secure - consider a more
  // secure solution such as https://cloud.google.com/kms/ to help keep secrets safe.
  private static final String CLOUD_SQL_INSTANCE_NAME = System.getenv("CLOUD_SQL_INSTANCE_NAME");
  private static final String DB_USER = System.getenv("DB_USER");
  private static final String DB_PASS = System.getenv("DB_PASS");
  private static final String DB_NAME = System.getenv("DB_NAME");

  private DataSource mysqlConnectionPool() {
    // [START cloud_sql_mysql_connection_pool]
    // The configuration object specifies behaviors for the connection pool.
    HikariConfig config = new HikariConfig();

    // Configure which instance and what database user to connect with.
    config.setJdbcUrl(String.format("jdbc:mysql:///%s", DB_NAME));
    config.setUsername(DB_USER); // e.g. "root", "postgres"
    config.setPassword(DB_PASS); // e.g. "my-password"

    // For Java users, the Cloud SQL JDBC Socket Factory can provide authenticated connections.
    // See https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory for details.
    config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.mysql.SocketFactory");
    config.addDataSourceProperty("cloudSqlInstance", CLOUD_SQL_INSTANCE_NAME);
    config.addDataSourceProperty("useSSL", "false");

    // ... Specify additional connection properties here.

    // [START_EXCLUDE]

    // [START cloud_sql_max_connections]
    // maximumPoolSize limits the total number of concurrent connections this pool will keep. Ideal
    // values for this setting are highly variable on app design, infrastructure, and database.
    config.setMaximumPoolSize(5);
    // [END cloud_sql_max_connections]

    // [START cloud_sql_connection_timeout]
    // setConnectionTimeout is the maximum number of milliseconds to wait for a connection checkout.
    // Any attempt to retrieve a connection from this pool that exceeds the set limit will throw an
    // SQLException.
    config.setConnectionTimeout(10000); // 10 seconds
    // [END cloud_sql_connection_timeout]

    // [START cloud_sql_connection_backoff]
    // Hikari automatically delays between failed connection attempts, eventually reaching a
    // maximum delay of `connectionTimeout / 2` between attempts.
    // [END cloud_sql_connection_backoff]

    // [START cloud_sql_connection_lifetime]
    // maxLifetime is the maximum possible lifetime of a connection in the pool. Connections that
    // live longer than this many milliseconds will be closed and reestablished between uses. This
    // value should be several minutes shorter than the database's timeout value to avoid unexpected
    // terminations.
    config.setMaxLifetime(1800000); // 30 minutes
    // [END cloud_sql_connection_lifetime]

    // [START cloud_sql_idle_connections]
    // minimumIdle is the minimum number of idle connections Hikari maintains in the pool.
    // Additional connections will be established to meet this value unless the pool is full.
    config.setMinimumIdle(5);
    // idleTimeout is the maximum amount of time a connection can sit in the pool. Connections that
    // sit idle for this many milliseconds are retried if minimumIdle is exceeded.
    config.setIdleTimeout(600000); // 10 minutes
    // [END cloud_sql_idle_connections]
    // [END_EXCLUDE]

    // Initialize the connection pool using the configuration object.
    DataSource pool = new HikariDataSource(config);
    // [END cloud_sql_mysql_connection_pool]
    return pool;
  }

  private void createTableSchema(DataSource pool) {
    // Safely attempt to create the table schema.
    try (Connection conn = pool.getConnection()) {
      PreparedStatement createTableStatement = conn.prepareStatement(
          "CREATE TABLE IF NOT EXISTS votes ( "
              + "vote_id SERIAL NOT NULL, time_cast timestamp NOT NULL, canidate CHAR(6) NOT NULL, "
              + "PRIMARY KEY (vote_id) );"
      );
      createTableStatement.execute();
    } catch (SQLException e) {
      throw new Error(
          "Unable to successfully verify table schema. Please double check the steps in the README"
              + " and restart the application. \n" + e.toString());
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    // This function is called when the Servlet is destroyed.
    DataSource pool = (DataSource) event.getServletContext().getAttribute("my-pool");
    if (pool != null) {
      try {
        pool.unwrap(HikariDataSource.class).close();
      } catch (SQLException e) {
        // Handle exception
        System.out.println("Any error occurred while the application was shutting down: " + e);
      }
    }
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {
    // This function is called when the application starts and will safely create a connection pool
    // that can be used to connect to.
    DataSource pool = (DataSource) event.getServletContext().getAttribute("my-pool");
    if (pool == null) {
      pool = mysqlConnectionPool();
      event.getServletContext().setAttribute("my-pool", pool);
    }
    createTableSchema(pool);
  }
}
