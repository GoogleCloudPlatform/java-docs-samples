package com.example.cloudsql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.sql.DataSource;

public class ConnectionPoolFactory {

  // Saving credentials in environment variables is convenient, but not secure - consider a more
  // secure solution such as https://cloud.google.com/kms/ to help keep secrets safe.
  private static final String INSTANCE_CONNECTION_NAME =
      System.getenv("INSTANCE_CONNECTION_NAME");
  private static final String INSTANCE_HOST = System.getenv("INSTANCE_HOST");
  private static final String INSTANCE_UNIX_SOCKET = System.getenv("INSTANCE_UNIX_SOCKET");
  private static final String DB_PORT = System.getenv("DB_PORT");
  private static final String DB_USER = System.getenv("DB_USER");
  private static final String DB_PASS = System.getenv("DB_PASS");
  private static final String DB_NAME = System.getenv("DB_NAME");

  private static final String TRUST_CERT_KEYSTORE_PATH = System.getenv("TRUST_CERT_KEYSTORE_PATH");
  private static final String TRUST_CERT_KEYSTORE_PASSWD = System.getenv(
      "TRUST_CERT_KEYSTORE_PASSWD");
  private static final String CLIENT_CERT_KEYSTORE_PATH = System.getenv(
      "CLIENT_CERT_KEYSTORE_PATH");
  private static final String CLIENT_CERT_KEYSTORE_PASSWD = System.getenv(
      "CLIENT_CERT_KEYSTORE_PASSWD");

  @SuppressFBWarnings(
      value = "USBR_UNNECESSARY_STORE_BEFORE_RETURN",
      justification = "Necessary for sample region tag.")
  private static HikariConfig configureConnectionPool(HikariConfig config) {
    // [START cloud_sql_mysql_servlet_limit]
    // maximumPoolSize limits the total number of concurrent connections this pool will keep. Ideal
    // values for this setting are highly variable on app design, infrastructure, and database.
    config.setMaximumPoolSize(5);
    // minimumIdle is the minimum number of idle connections Hikari maintains in the pool.
    // Additional connections will be established to meet this value unless the pool is full.
    config.setMinimumIdle(5);
    // [END cloud_sql_mysql_servlet_limit]

    // [START cloud_sql_mysql_servlet_timeout]
    // setConnectionTimeout is the maximum number of milliseconds to wait for a connection checkout.
    // Any attempt to retrieve a connection from this pool that exceeds the set limit will throw an
    // SQLException.
    config.setConnectionTimeout(10000); // 10 seconds
    // idleTimeout is the maximum amount of time a connection can sit in the pool. Connections that
    // sit idle for this many milliseconds are retried if minimumIdle is exceeded.
    config.setIdleTimeout(600000); // 10 minutes
    // [END cloud_sql_mysql_servlet_timeout]

    // [START cloud_sql_mysql_servlet_backoff]
    // Hikari automatically delays between failed connection attempts, eventually reaching a
    // maximum delay of `connectionTimeout / 2` between attempts.
    // [END cloud_sql_mysql_servlet_backoff]

    // [START cloud_sql_mysql_servlet_lifetime]
    // maxLifetime is the maximum possible lifetime of a connection in the pool. Connections that
    // live longer than this many milliseconds will be closed and reestablished between uses. This
    // value should be several minutes shorter than the database's timeout value to avoid unexpected
    // terminations.
    config.setMaxLifetime(1800000); // 30 minutes
    // [END cloud_sql_mysql_servlet_lifetime]
    return config;
  }

  public static DataSource createConnectionPool() {
    if (INSTANCE_HOST != null) {
      return createConnectionPoolWithTCP();
    } else {
      return createConnectionPoolWithConnector();
    }
  }

  private static DataSource createConnectionPoolWithConnector() {
    // [START cloud_sql_mysql_servlet_connect_connector]
    // [START cloud_sql_mysql_servlet_connect_unix]
    // The configuration object specifies behaviors for the connection pool.
    HikariConfig config = new HikariConfig();

    // The following URL is equivalent to setting the config options below:
    // jdbc:mysql:///<DB_NAME>?cloudSqlInstance=<INSTANCE_CONNECTION_NAME>&
    // socketFactory=com.google.cloud.sql.mysql.SocketFactory&user=<DB_USER>&password=<DB_PASS>
    // See the link below for more info on building a JDBC URL for the Cloud SQL JDBC Socket Factory
    // https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory#creating-the-jdbc-url

    // Configure which instance and what database user to connect with.
    config.setJdbcUrl(String.format("jdbc:mysql:///%s", DB_NAME));
    config.setUsername(DB_USER); // e.g. "root", "mysql"
    config.setPassword(DB_PASS); // e.g. "my-password"

    config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.mysql.SocketFactory");
    config.addDataSourceProperty("cloudSqlInstance", INSTANCE_CONNECTION_NAME);

    // [END cloud_sql_mysql_servlet_connect_connector]
    // Unix sockets are not natively supported in Java, so it is necessary to use the Cloud SQL JDBC
    // Socket Factory to connect.
    // Note: For Java users, the Cloud SQL JDBC Socket Factory can provide authenticated connections
    // which is preferred to using the Cloud SQL Proxy with Unix sockets.
    // See https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory for details.
    if (INSTANCE_UNIX_SOCKET != null) {
      config.addDataSourceProperty("unixSocketPath", INSTANCE_UNIX_SOCKET);
    }
    // [START cloud_sql_mysql_servlet_connect_connector]


    // The ipTypes argument can be used to specify a comma delimited list of preferred IP types
    // for connecting to a Cloud SQL instance. The argument ipTypes=PRIVATE will force the
    // SocketFactory to connect with an instance's associated private IP.
    config.addDataSourceProperty("ipTypes", "PUBLIC,PRIVATE");

    // ... Specify additional connection properties here.
    // [START_EXCLUDE]
    configureConnectionPool(config);
    // [END_EXCLUDE]

    // Initialize the connection pool using the configuration object.
    DataSource pool = new HikariDataSource(config);
    // [END cloud_sql_mysql_servlet_connect_connector]
    // [END cloud_sql_mysql_servlet_connect_unix]
    return pool;
  }

  @SuppressFBWarnings(
      value = "USBR_UNNECESSARY_STORE_BEFORE_RETURN",
      justification = "Necessary for sample region tag.")
  private static DataSource createConnectionPoolWithTCP() {
    // [START cloud_sql_mysql_servlet_connect_tcp]
    // [START cloud_sql_mysql_servlet_connect_tcp_sslcerts]
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
    //
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
    DataSource pool = new HikariDataSource(config);
    // [END cloud_sql_mysql_servlet_connect_tcp]
    // [END cloud_sql_mysql_servlet_connect_tcp_sslcerts]
    return pool;
  }
}
