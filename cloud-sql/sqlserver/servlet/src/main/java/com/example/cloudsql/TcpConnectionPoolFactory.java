/*
 * Copyright 2022 Google LLC
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

// [START cloud_sql_sqlserver_servlet_connect_tcp]
// [START cloud_sql_sqlserver_servlet_connect_tcp_sslcerts]

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class TcpConnectionPoolFactory extends ConnectionPoolFactory {

  // Saving credentials in environment variables is convenient, but not secure - consider a more
  // secure solution such as https://cloud.google.com/secret-manager/ to help keep secrets safe.
  private static final String DB_USER = System.getenv("DB_USER");
  private static final String DB_PASS = System.getenv("DB_PASS");
  private static final String DB_NAME = System.getenv("DB_NAME");

  private static final String INSTANCE_HOST = System.getenv("INSTANCE_HOST");
  private static final String DB_PORT = System.getenv("DB_PORT");

  // [END cloud_sql_sqlserver_servlet_connect_tcp]
  private static final String TRUST_CERT_KEYSTORE_PATH = System.getenv(
      "TRUST_CERT_KEYSTORE_NAME");
  private static final String TRUST_CERT_KEYSTORE_PASSWD = System.getenv(
      "TRUST_CERT_KEYSTORE_PASSWD");
  // [START cloud_sql_sqlserver_servlet_connect_tcp]

  public static DataSource createConnectionPool() {
    // The configuration object specifies behaviors for the connection pool.
    HikariConfig config = new HikariConfig();

    // Configure which instance and what database user to connect with.
    config.setJdbcUrl(
        String.format("jdbc:sqlserver://%s:%s;databaseName=%s", INSTANCE_HOST, DB_PORT, DB_NAME));
    config.setUsername(DB_USER); // e.g. "root", "sqlserver"
    config.setPassword(DB_PASS); // e.g. "my-password"

    // [END cloud_sql_sqlserver_servlet_connect_tcp]
    // (OPTIONAL) Configure SSL certificates
    // For deployments that connect directly to a Cloud SQL instance without
    // using the Cloud SQL Proxy, configuring SSL certificates will ensure the
    // connection is encrypted.
    // For details about how the SQL Server JDBC driver handles SSL encryption, see the link below
    // https://docs.microsoft.com/en-us/sql/connect/jdbc/understanding-ssl-support?view=sql-server-ver15

    if (TRUST_CERT_KEYSTORE_PATH != null) {
      config.addDataSourceProperty("encrypt", "true");
      config.addDataSourceProperty("trustStore", TRUST_CERT_KEYSTORE_PATH);
      config.addDataSourceProperty("trustStorePassword", TRUST_CERT_KEYSTORE_PASSWD);
    }
    // [START cloud_sql_sqlserver_servlet_connect_tcp]

    // ... Specify additional connection properties here.
    // [START_EXCLUDE]
    configureConnectionPool(config);
    // [END_EXCLUDE]

    // Initialize the connection pool using the configuration object.
    return new HikariDataSource(config);
  }
}
// [END cloud_sql_sqlserver_servlet_connect_tcp]
// [END cloud_sql_sqlserver_servlet_connect_tcp_sslcerts]
