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

// [START cloud_sql_mysql_servlet_connect_tcp]
// [START cloud_sql_mysql_servlet_connect_tcp_sslcerts]

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

  // [END cloud_sql_mysql_servlet_connect_tcp]
  private static final String TRUST_CERT_KEYSTORE_PATH = System.getenv(
      "TRUST_CERT_KEYSTORE_PATH");
  private static final String TRUST_CERT_KEYSTORE_PASSWD = System.getenv(
      "TRUST_CERT_KEYSTORE_PASSWD");
  private static final String CLIENT_CERT_KEYSTORE_PATH = System.getenv(
      "CLIENT_CERT_KEYSTORE_PATH");
  private static final String CLIENT_CERT_KEYSTORE_PASSWD = System.getenv(
      "CLIENT_CERT_KEYSTORE_PASSWD");
  // [START cloud_sql_mysql_servlet_connect_tcp]

  public static DataSource createConnectionPool() {
    // The configuration object specifies behaviors for the connection pool.
    HikariConfig config = new HikariConfig();

    // The following URL is equivalent to setting the config options below:
    // jdbc:mysql://<INSTANCE_HOST>:<DB_PORT>/<DB_NAME>?user=<DB_USER>&password=<DB_PASS>
    // See the link below for more info on building a JDBC URL for the Cloud SQL JDBC Socket Factory
    // https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory#creating-the-jdbc-url

    // Configure which instance and what database user to connect with.
    config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s", INSTANCE_HOST, DB_PORT, DB_NAME));
    config.setUsername(DB_USER); // e.g. "root", "mysql"
    config.setPassword(DB_PASS); // e.g. "my-password"

    // [END cloud_sql_mysql_servlet_connect_tcp]
    // (OPTIONAL) Configure SSL certificates
    // For deployments that connect directly to a Cloud SQL instance without
    // using the Cloud SQL Proxy, configuring SSL certificates will ensure the
    // connection is encrypted.
    // See the link below for more information on how to configure SSL Certificates for use with
    // MySQL Connector/J
    // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-using-ssl.html
    if (CLIENT_CERT_KEYSTORE_PATH != null && TRUST_CERT_KEYSTORE_PATH != null) {
      config.addDataSourceProperty("trustCertificateKeyStoreUrl",
          String.format("file:%s", TRUST_CERT_KEYSTORE_PATH));
      config.addDataSourceProperty("trustCertificateKeyStorePassword", TRUST_CERT_KEYSTORE_PASSWD);
      config.addDataSourceProperty("clientCertificateKeyStoreUrl",
          String.format("file:%s", CLIENT_CERT_KEYSTORE_PATH));
      config.addDataSourceProperty("clientCertificateKeyStorePassword",
          CLIENT_CERT_KEYSTORE_PASSWD);
    }
    // [START cloud_sql_mysql_servlet_connect_tcp]

    // ... Specify additional connection properties here.
    // [START_EXCLUDE]
    configureConnectionPool(config);
    // [END_EXCLUDE]

    // Initialize the connection pool using the configuration object.
    return new HikariDataSource(config);
  }
}
// [END cloud_sql_mysql_servlet_connect_tcp]
// [END cloud_sql_mysql_servlet_connect_tcp_sslcerts]
