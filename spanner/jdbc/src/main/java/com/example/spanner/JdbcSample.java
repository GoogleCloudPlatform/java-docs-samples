/*
 * Copyright 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.spanner;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.jdbc.CloudSpannerJdbcConnection;
import com.google.cloud.spanner.jdbc.JdbcConstants;
import com.google.cloud.spanner.jdbc.JdbcDataSource;
import com.google.cloud.spanner.jdbc.JdbcSqlExceptionFactory.JdbcAbortedException;
import io.grpc.Status.Code;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;

/** Example showing how to use the Cloud Spanner open source JDBC driver. */
public class JdbcSample {

  /** Class to contain singer sample data. */
  static class Singer {

    final long singerId;
    final String firstName;
    final String lastName;

    Singer(long singerId, String firstName, String lastName) {
      this.singerId = singerId;
      this.firstName = firstName;
      this.lastName = lastName;
    }
  }

  static final List<Singer> SINGERS =
      Arrays.asList(
          new Singer(1, "Marc", "Richards"),
          new Singer(2, "Catalina", "Smith"),
          new Singer(3, "Alice", "Trentor"),
          new Singer(4, "Lea", "Martin"),
          new Singer(5, "David", "Lomond"));

  /**
   * This example shows how to create a JDBC connection and use this to execute queries and updates.
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 3 || args.length > 4) {
      printUsageAndExit();
    }

    /*
     * CREATE TABLE Singers (
     *   SingerId   INT64 NOT NULL,
     *   FirstName  STRING(1024),
     *   LastName   STRING(1024),
     *   SingerInfo BYTES(MAX),
     * ) PRIMARY KEY (SingerId);
     *
     * CREATE TABLE Albums (
     *   SingerId        INT64 NOT NULL,
     *   AlbumId         INT64 NOT NULL,
     *   AlbumTitle      STRING(MAX),
     *   MarketingBudget INT64
     * ) PRIMARY KEY(SingerId, AlbumId),
     *   INTERLEAVE IN PARENT Singers ON DELETE CASCADE;
     *
     * CREATE TABLE Songs (
     *   SingerId  INT64 NOT NULL,
     *   AlbumId   INT64 NOT NULL,
     *   TrackId   INT64 NOT NULL,
     *   SongName  STRING(MAX),
     *   Duration  INT64,
     *   SongGenre STRING(25)
     * ) PRIMARY KEY(SingerId, AlbumId, TrackId),
     *   INTERLEAVE IN PARENT Albums ON DELETE CASCADE;
     */

    String command = args[0];
    String projectId = SpannerOptions.getDefaultProjectId();
    String instanceId = args[1];
    String databaseId = args[2];
    String credentialsFile = args.length > 3 ? args[3] : null;

    run(command, projectId, instanceId, databaseId, credentialsFile);
  }

  static void run(
      String command,
      String projectId,
      String instanceId,
      String databaseId,
      String credentialsFile)
      throws SQLException {
    switch (command) {
      // Connection samples.
      case "baseurl":
        JdbcConnectSamples.baseUrl(projectId, instanceId, databaseId);
        break;
      case "defaultprojectid":
        JdbcConnectSamples.defaultProjectId(instanceId, databaseId);
        break;
      case "connectionurlproperties":
        JdbcConnectSamples.connectionUrlProperties(projectId, instanceId, databaseId);
        break;
      case "connectionproperties":
        JdbcConnectSamples.connectionProperties(projectId, instanceId, databaseId);
        break;
      case "customcredentials":
        JdbcConnectSamples.customCredentials(projectId, instanceId, databaseId, credentialsFile);
        break;
      case "datasource":
        JdbcConnectSamples.datasource(projectId, instanceId, databaseId);
        break;
      case "customhost":
        JdbcConnectSamples.customHost(projectId, instanceId, databaseId, 9010);
        break;

      // Initialization samples.
      case "createtable":
        createTable(projectId, instanceId, databaseId);
        break;
      case "insertdata":
        insertData(projectId, instanceId, databaseId);
        break;

      // Autocommit samples.
      case "singleusereadonly":
        JdbcAutoCommitSamples.singleUseReadOnly(projectId, instanceId, databaseId);
        break;
      case "singleusereadonlytimestampbound":
        JdbcAutoCommitSamples.singleUseReadOnlyTimestampBound(projectId, instanceId, databaseId);
        break;
      case "update":
        JdbcAutoCommitSamples.update(projectId, instanceId, databaseId);
        break;
      case "partitioneddml":
        JdbcAutoCommitSamples.partitionedDml(projectId, instanceId, databaseId);
        break;

      // Transaction samples.
      case "readonlytransaction":
        JdbcTransactionSamples.readOnlyTransaction(projectId, instanceId, databaseId);
        break;
      case "readwritetransaction":
        JdbcTransactionSamples.readWriteTransaction(projectId, instanceId, databaseId);
        break;

      // Batch samples.
      case "batchdml":
        JdbcBatchSamples.batchDml(projectId, instanceId, databaseId);
        break;
      case "batchdmlusingsql":
        JdbcBatchSamples.batchDmlUsingSqlStatements(projectId, instanceId, databaseId);
        break;
      case "batchddl":
        JdbcBatchSamples.batchDdl(projectId, instanceId, databaseId);
        break;
      case "batchddlusingsql":
        JdbcBatchSamples.batchDdlUsingSqlStatements(projectId, instanceId, databaseId);
        break;
      case "abortbatch":
        JdbcBatchSamples.abortBatch(projectId, instanceId, databaseId);
        break;

      // Variables samples.
      case "committimestamp":
        JdbcVariablesExamples.getCommitTimestamp(projectId, instanceId, databaseId);
        break;
      case "readtimestamp":
        JdbcVariablesExamples.getReadTimestamp(projectId, instanceId, databaseId);
        break;

      // QueryOptions samples.
      case "connectionwithqueryoptions":
        connectionWithQueryOptions(projectId, instanceId, databaseId);
        break;
      case "setqueryoptions":
        setQueryOptions(projectId, instanceId, databaseId);
        break;

      // Custom methods samples.
      case "bufferedwrite":
        CustomMethodsSamples.bufferedWrite(projectId, instanceId, databaseId);
        break;
      case "transactionwithretryloop":
        CustomMethodsSamples.transactionWithRetryLoop(projectId, instanceId, databaseId);
        break;
      case "jdbctransactionwithretryloop":
        CustomMethodsSamples.genericJdbcTransactionWithRetryLoop(projectId, instanceId, databaseId);
        break;

      default:
        printUsageAndExit();
    }
  }

  static void printUsageAndExit() {
    System.err.println("Usage:");
    System.err.println("    JdbcSample <command> <instance_id> <database_id>");
    System.err.println("");
    System.err.println("Examples:");

    // Connection samples.
    System.err.println("    JdbcSample baseurl my-instance example-db");
    System.err.println("    JdbcSample defaultprojectid my-instance example-db");
    System.err.println("    JdbcSample connectionurlproperties my-instance example-db");
    System.err.println("    JdbcSample connectionproperties my-instance example-db");
    System.err.println("    JdbcSample customcredentials my-instance example-db credentialsFile");
    System.err.println("    JdbcSample datasource my-instance example-db");
    System.err.println("    JdbcSample customhost my-instance example-db");

    // Initialization samples.
    System.err.println("    JdbcSample createtable my-instance example-db");
    System.err.println("    JdbcSample insertdata my-instance example-db");

    // Autocommit samples.
    System.err.println("    JdbcSample singleusereadonly my-instance example-db");
    System.err.println("    JdbcSample singleusereadonlytimestampbound my-instance example-db");
    System.err.println("    JdbcSample update my-instance example-db");
    System.err.println("    JdbcSample partitioneddml my-instance example-db");

    // Transaction samples.
    System.err.println("    JdbcSample readonlytransaction my-instance example-db");
    System.err.println("    JdbcSample readwritetransaction my-instance example-db");

    // Batch samples.
    System.err.println("    JdbcSample batchdml my-instance example-db");
    System.err.println("    JdbcSample batchdmlusingsql my-instance example-db");
    System.err.println("    JdbcSample batchddl my-instance example-db");
    System.err.println("    JdbcSample batchddlusingsql my-instance example-db");
    System.err.println("    JdbcSample abortbatch my-instance example-db");

    // Variables samples.
    System.err.println("    JdbcSample committimestamp my-instance example-db");
    System.err.println("    JdbcSample readtimestamp my-instance example-db");

    // QueryOptions samples.
    System.err.println("    JdbcSample connectionwithqueryoptions my-instance example-db");
    System.err.println("    JdbcSample setqueryoptions my-instance example-db");

    // Custom methods samples.
    System.err.println("    JdbcSample bufferedwrite my-instance example-db");
    System.err.println("    JdbcSample transactionwithretryloop my-instance example-db");
    System.err.println("    JdbcSample jdbctransactionwithretryloop my-instance example-db");

    System.exit(1);
  }

  static void createTable(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      try (Statement statement = connection.createStatement()) {
        statement.execute(
            "CREATE TABLE Singers (\n"
                + "              SingerId   INT64 NOT NULL,\n"
                + "              FirstName  STRING(1024),\n"
                + "              LastName   STRING(1024),\n"
                + "              SingerInfo BYTES(MAX),\n"
                + "              ) PRIMARY KEY (SingerId)\n");
      }
    }
    System.out.println("Created table [Singers]");
  }

  static void insertData(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      connection.setAutoCommit(false);
      try (PreparedStatement ps =
          connection.prepareStatement(
              "INSERT INTO Singers\n"
                  + "(SingerId, FirstName, LastName, SingerInfo)\n"
                  + "VALUES\n"
                  + "(?, ?, ?, ?)")) {
        for (Singer singer : SINGERS) {
          ps.setLong(1, singer.singerId);
          ps.setString(2, singer.firstName);
          ps.setString(3, singer.lastName);
          ps.setNull(4, Types.BINARY);
          ps.addBatch();
        }
        int[] updateCounts = ps.executeBatch();
        connection.commit();
        System.out.printf("Insert counts: %s\n", Arrays.toString(updateCounts));
      }
    }
  }

  // [START spanner_jdbc_connection_with_query_options]
  static void connectionWithQueryOptions(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String optimizerVersion = "1";
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s?optimizerVersion=%s",
            projectId, instanceId, databaseId, optimizerVersion);
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      try (Statement statement = connection.createStatement()) {
        // Execute a query using the optimizer version '1'.
        try (ResultSet rs =
            statement.executeQuery(
                "SELECT SingerId, FirstName, LastName FROM Singers ORDER BY LastName")) {
          while (rs.next()) {
            System.out.printf("%d %s %s\n", rs.getLong(1), rs.getString(2), rs.getString(3));
          }
        }
        try (ResultSet rs = statement.executeQuery("SHOW VARIABLE OPTIMIZER_VERSION")) {
          while (rs.next()) {
            System.out.printf("Optimizer version: %s\n", rs.getString(1));
          }
        }
      }
    }
  }
  // [END spanner_jdbc_connection_with_query_options]

  // [START spanner_jdbc_set_statement_for_query_options]
  static void setQueryOptions(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      try (Statement statement = connection.createStatement()) {
        // Instruct the JDBC connection to use version '1' of the query optimizer.
        statement.execute("SET OPTIMIZER_VERSION='1'");
        // Execute a query using the latest optimizer version.
        try (ResultSet rs =
            statement.executeQuery(
                "SELECT SingerId, FirstName, LastName FROM Singers ORDER BY LastName")) {
          while (rs.next()) {
            System.out.printf("%d %s %s\n", rs.getLong(1), rs.getString(2), rs.getString(3));
          }
        }
        try (ResultSet rs = statement.executeQuery("SHOW VARIABLE OPTIMIZER_VERSION")) {
          while (rs.next()) {
            System.out.printf("Optimizer version: %s\n", rs.getString(1));
          }
        }
      }
    }
  }
  // [END spanner_jdbc_set_statement_for_query_options]

  /**
   * Examples showing the different options for getting a JDBC {@link Connection} for Google Cloud
   * Spanner.
   */
  static class JdbcConnectSamples {
    /**
     * The formal definition of the JDBC connection URL for Google Cloud Śpanner is:
     *
     * <code>
     * jdbc:cloudspanner:[//host[:port]]/projects/project-id
     *                   [/instances/instance-id[/databases/database-name]]
     *                   [\?property-name=property-value[;property-name=property-value]*]?
     * </code>
     *
     * The base JDBC connection URL for Cloud Spanner consists of the following components: Prefix:
     * jdbc:cloudspanner: Project: /projects/[project-id] Instance: /instances/[instance-id]
     * Database: /databases/[database-name]
     *
     * <p>This example will create a read/write connection in autocommit mode. The connection will
     * use the default application credentials of this environment (i.e. the credentials that are
     * also returned by the method {@link GoogleCredentials#getApplicationDefault()}).
     */
    static void baseUrl(String projectId, String instanceId, String databaseId)
        throws SQLException {
      try (Connection connection =
          DriverManager.getConnection(
              String.format(
                  "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
                  projectId, instanceId, databaseId))) {
        try (ResultSet rs =
            connection.createStatement().executeQuery("SELECT CURRENT_TIMESTAMP()")) {
          while (rs.next()) {
            System.out.println(
                "Connected to Cloud Spanner at [" + rs.getTimestamp(1).toString() + "]");
          }
        }
      }
    }

    /**
     * Create a {@link Connection} using custom credentials. The 'credentials' property must
     * reference a file containing a service account that should be used.
     */
    static void customCredentials(
        String projectId, String instanceId, String databaseId, String pathToCredentialsFile)
        throws SQLException {
      try (Connection connection =
          DriverManager.getConnection(
              String.format(
                  "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s?credentials=%s",
                  projectId, instanceId, databaseId, pathToCredentialsFile))) {
        try (ResultSet rs =
            connection.createStatement().executeQuery("SELECT CURRENT_TIMESTAMP()")) {
          while (rs.next()) {
            System.out.println(
                "Connected to Cloud Spanner at [" + rs.getTimestamp(1).toString() + "]");
          }
        }
      }
    }

    /**
     * Instead of specifying a fixed project id in the URL, the URL may also contain the placeholder
     * DEFAULT_PROJECT_ID. This placeholder will be replaced by the default project id of the
     * environment where it is running.
     */
    static void defaultProjectId(String instanceId, String databaseId) throws SQLException {
      try (Connection connection =
          DriverManager.getConnection(
              String.format(
                  "jdbc:cloudspanner:/projects/DEFAULT_PROJECT_ID/instances/%s/databases/%s",
                  instanceId, databaseId))) {
        try (ResultSet rs =
            connection.createStatement().executeQuery("SELECT CURRENT_TIMESTAMP()")) {
          while (rs.next()) {
            System.out.println(
                "Connected to Cloud Spanner at [" + rs.getTimestamp(1).toString() + "]");
          }
        }
      }
    }

    /**
     * The initial state of the connection can be configured by specifying properties in the
     * connection URL: readonly: Should the connection initially be in read-only mode. Default:
     * false. autocommit: Should the connection initially be in autocommit mode. Default: true.
     *
     * <p>Multiple properties in the connection URL should be separated using a semicolon.
     */
    static void connectionUrlProperties(String projectId, String instanceId, String databaseId)
        throws SQLException {
      try (Connection connection =
          DriverManager.getConnection(
              String.format(
                  "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s"
                      + "?readonly=true;autocommit=false",
                  projectId, instanceId, databaseId))) {
        System.out.println("Readonly: " + connection.isReadOnly());
        System.out.println("Autocommit: " + connection.getAutoCommit());
      }
    }

    /**
     * Properties that can be specified in the connection URL may also be specified using a {@link
     * Properties} instance.
     */
    static void connectionProperties(String projectId, String instanceId, String databaseId)
        throws SQLException {
      Properties properties = new Properties();
      properties.setProperty("readonly", "true");
      properties.setProperty("autocommit", "false");
      try (Connection connection =
          DriverManager.getConnection(
              String.format(
                  "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
                  projectId, instanceId, databaseId),
              properties)) {
        System.out.println("Readonly: " + connection.isReadOnly());
        System.out.println("Autocommit: " + connection.getAutoCommit());
      }
    }

    /**
     * A {@link Connection} can also be obtained through a {@link DataSource} instead of through the
     * {@link DriverManager}.
     */
    static void datasource(String projectId, String instanceId, String databaseId)
        throws SQLException {
      JdbcDataSource datasource = new JdbcDataSource();
      datasource.setUrl(
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId));
      datasource.setReadonly(true);
      datasource.setAutocommit(false);
      try (Connection connection = datasource.getConnection()) {
        System.out.println("Readonly: " + connection.isReadOnly());
        System.out.println("Autocommit: " + connection.getAutoCommit());
      }
    }

    /**
     * The JDBC URL may contain a custom host for testing purposes. This can be used if you want to
     * connect to a local mock server or other test server. When connecting to a local emulator or a
     * mock server, it is also possible to specify that the connection should use plain text
     * communication.
     */
    static void customHost(String projectId, String instanceId, String databaseId, int port)
        throws SQLException {
      try (Connection connection =
          DriverManager.getConnection(
              String.format(
                  "jdbc:cloudspanner://localhost:%d/projects/%s/instances/%s/databases/%s"
                      + "?usePlainText=true",
                  port, projectId, instanceId, databaseId))) {
        System.out.printf(
            "Connected to %s\n",
            connection.unwrap(CloudSpannerJdbcConnection.class).getConnectionUrl());
      }
    }
  }

  /** Samples for using the JDBC driver in autocommit mode. */
  static class JdbcAutoCommitSamples {
    /** Execute a query in single-use read-only mode with the default strong timestamp bound. */
    static void singleUseReadOnly(String projectId, String instanceId, String databaseId)
        throws SQLException {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        if (!connection.getAutoCommit()) {
          throw new IllegalStateException("Connection should be in autocommit mode");
        }
        // When the connection is in autocommit mode, any query that is executed will automatically
        // be executed using a single-use read-only transaction, even if the connection itself is in
        // read/write mode.
        try (ResultSet rs =
            connection
                .createStatement()
                .executeQuery(
                    "SELECT SingerId, FirstName, LastName FROM Singers ORDER BY LastName")) {
          while (rs.next()) {
            System.out.printf("%d %s %s\n", rs.getLong(1), rs.getString(2), rs.getString(3));
          }
        }
      }
    }

    /**
     * Execute a query in single-use read-only mode with a custom timestamp bound. The timestamp
     * bound can be specified using the following statement:
     *
     * <code>
     * SET READ_ONLY_STALENESS = 'STRONG'
     *                         | 'MIN_READ_TIMESTAMP <timestamp>'
     *                         | 'READ_TIMESTAMP <timestamp>'
     *                         | 'MAX_STALENESS <int64>s|ms|us|ns'
     *                         | 'EXACT_STALENESS (<int64>s|ms|us|ns)'
     * </code>
     */
    static void singleUseReadOnlyTimestampBound(
        String projectId, String instanceId, String databaseId) throws SQLException {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        if (!connection.getAutoCommit()) {
          throw new IllegalStateException("Connection should be in autocommit mode");
        }
        Statement statement = connection.createStatement();
        // Set staleness to max 1 second. This is only allowed in autocommit mode.
        statement.execute("SET READ_ONLY_STALENESS = 'MAX_STALENESS 1s'");
        try (ResultSet rs =
            connection
                .createStatement()
                .executeQuery(
                    "SELECT SingerId, FirstName, LastName FROM Singers ORDER BY LastName")) {
          while (rs.next()) {
            System.out.printf("%d %s %s\n", rs.getLong(1), rs.getString(2), rs.getString(3));
          }
        }
      }
    }

    /**
     * Execute an INSERT statement in autocommit mode will cause the JDBC {@link Connection} to
     * create a read/write transaction in the background and commit this transaction automatically.
     */
    static void update(String projectId, String instanceId, String databaseId) throws SQLException {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        if (!connection.getAutoCommit()) {
          throw new IllegalStateException("Connection should be in autocommit mode");
        }
        // The following statement will automatically be committed by the connection.
        int updateCount =
            connection
                .createStatement()
                .executeUpdate(
                    "INSERT INTO Singers (SingerId, FirstName, LastName)\n"
                    + "VALUES (9999, 'Lloyd', 'Pineda')");
        System.out.printf("Inserted %d row(s)\n", updateCount);
      }
    }

    /**
     * A JDBC {@link Connection} in autocommit mode can be instructed to execute DML statements as
     * partitioned DML transactions. Use the following statement to turn partitioned DML on/off:
     *
     * <pre>
     * SET AUTOCOMMIT_DML_MODE = 'PARTITIONED_NON_ATOMIC'|'TRANSACTIONAL'
     * </pre>
     */
    static void partitionedDml(String projectId, String instanceId, String databaseId)
        throws SQLException {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        if (!connection.getAutoCommit()) {
          throw new IllegalStateException("Connection should be in autocommit mode");
        }
        Statement statement = connection.createStatement();
        // Turn on partitioned DML mode.
        statement.execute("SET AUTOCOMMIT_DML_MODE = 'PARTITIONED_NON_ATOMIC'");
        int updateCount =
            statement.executeUpdate(
                "UPDATE Singers SET SingerInfo = SHA512(LastName)\n"
                + "WHERE SingerId >= 1 AND SingerId <= 5");
        System.out.printf("Updated %d row(s)\n", updateCount);
      }
    }
  }

  /** Samples for executing DML and DDL batches with the JDBC driver. */
  static class JdbcBatchSamples {
    /**
     * Execute a number of DML statements as one batch. The statements will be sent to Cloud Spanner
     * as one batch using the executeBatchDml rpc.
     */
    static void batchDml(String projectId, String instanceId, String databaseId)
        throws SQLException {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        try (Statement statement = connection.createStatement()) {
          statement.addBatch(
              "INSERT INTO Singers (SingerId, FirstName, LastName)\n"
              + "VALUES (10, 'Marc', 'Richards')");
          statement.addBatch(
              "INSERT INTO Singers (SingerId, FirstName, LastName)\n"
              + "VALUES (11, 'Amirah', 'Finney')");
          statement.addBatch(
              "INSERT INTO Singers (SingerId, FirstName, LastName)\n"
              + "VALUES (12, 'Reece', 'Dunn')");
          int[] updateCounts = statement.executeBatch();
          System.out.printf("Batch insert counts: %s\n", Arrays.toString(updateCounts));
        }
      }
    }

    /**
     * Batching DML statements can also be achieved using the SQL statements 'START BATCH DML' and
     * 'RUN BATCH'. This example will have the exact same effect as the example {@link #batchDml()}.
     */
    static void batchDmlUsingSqlStatements(String projectId, String instanceId, String databaseId)
        throws SQLException {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        try (Statement statement = connection.createStatement()) {
          statement.execute("START BATCH DML");
          statement.execute(
              "INSERT INTO Singers (SingerId, FirstName, LastName)\n"
              + "VALUES (14, 'Aayat', 'Curran')");
          statement.execute(
              "INSERT INTO Singers (SingerId, FirstName, LastName)\n"
              + "VALUES (15, 'Tudor', 'Mccarthy')");
          statement.execute(
              "INSERT INTO Singers (SingerId, FirstName, LastName)\n"
              + "VALUES (16, 'Cobie', 'Webb')");
          statement.execute("RUN BATCH");
          // The 'RUN BATCH' statement returns the update counts as a result set.
          try (ResultSet rs = statement.getResultSet()) {
            if (rs.next()) {
              Array array = rs.getArray("UPDATE_COUNTS");
              // 'RUN BATCH' returns the update counts as an array of Longs, as this is the default
              // for Cloud Spanner.
              Long[] updateCounts = (Long[]) array.getArray();
              System.out.printf("Batch insert counts: %s\n", Arrays.toString(updateCounts));
            }
          }
        }
      }
    }

    /**
     * Execute a number of DDL statements as one batch. The statements will be sent to Cloud Spanner
     * as one batch using the updateDatabaseDdl rpc. The returned array will contain a {@link
     * JdbcConstants#STATEMENT_NO_RESULT} value for each statement.
     */
    static void batchDdl(String projectId, String instanceId, String databaseId)
        throws SQLException {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        try (Statement statement = connection.createStatement()) {
          statement.addBatch(
              "CREATE TABLE Albums (\n"
                  + "  SingerId        INT64 NOT NULL,\n"
                  + "  AlbumId         INT64 NOT NULL,\n"
                  + "  AlbumTitle      STRING(MAX),\n"
                  + "  MarketingBudget INT64\n"
                  + ") PRIMARY KEY(SingerId, AlbumId),\n"
                  + "  INTERLEAVE IN PARENT Singers ON DELETE CASCADE");
          statement.addBatch(
              "CREATE TABLE Songs (\n"
                  + "  SingerId  INT64 NOT NULL,\n"
                  + "  AlbumId   INT64 NOT NULL,\n"
                  + "  TrackId   INT64 NOT NULL,\n"
                  + "  SongName  STRING(MAX),\n"
                  + "  Duration  INT64,\n"
                  + "  SongGenre STRING(25)\n"
                  + ") PRIMARY KEY(SingerId, AlbumId, TrackId),\n"
                  + "  INTERLEAVE IN PARENT Albums ON DELETE CASCADE");
          int[] updateCounts = statement.executeBatch();
          System.out.printf("DDL update counts: %s\n", Arrays.toString(updateCounts));
        }
      }
    }

    /**
     * Batching DML statements can also be achieved using the SQL statements 'START BATCH DDL' and
     * 'RUN BATCH'. This example will have the exact same effect as the example {@link #batchDdl()}.
     */
    static void batchDdlUsingSqlStatements(String projectId, String instanceId, String databaseId)
        throws SQLException {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        try (Statement statement = connection.createStatement()) {
          // Start a DDL batch.
          statement.execute("START BATCH DDL");
          // Execute the DDL statements.
          statement.execute(
              "CREATE TABLE Concerts (\n"
                  + "  VenueId      INT64 NOT NULL,\n"
                  + "  SingerId     INT64 NOT NULL,\n"
                  + "  ConcertDate  DATE NOT NULL,\n"
                  + "  BeginTime    TIMESTAMP,\n"
                  + "  EndTime      TIMESTAMP,\n"
                  + "  TicketPrices ARRAY<INT64>,\n"
                  + "  CONSTRAINT Fk_Concerts_Singer FOREIGN KEY (SingerId)\n"
                  + "                                REFERENCES Singers (SingerId)\n"
                  + ") PRIMARY KEY(VenueId, SingerId, ConcertDate)");
          // Update count for a DDL statement will always be JdbcConstants#STATEMENT_NO_RESULT.
          System.out.printf(
              "Update count for CREATE TABLE Concerts: %d\n", statement.getUpdateCount());

          statement.execute("CREATE INDEX SingersByFirstLastName ON Singers(FirstName, LastName)");
          System.out.printf(
              "Update count for CREATE INDEX SingersByFirstLastName: %d\n",
              statement.getUpdateCount());

          // The 'RUN BATCH' statement will not return any values for a DDL batch.
          statement.execute("RUN BATCH");
          System.out.println("Executed DDL batch");
        }
      }
    }

    /** A batch of DML or DDL statements can be aborted using the 'ABORT BATCH' statement. */
    static void abortBatch(String projectId, String instanceId, String databaseId)
        throws SQLException {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        try (Statement statement = connection.createStatement()) {
          statement.execute("START BATCH DML");
          statement.execute(
              "INSERT INTO Singers (SingerId, FirstName, LastName)\n"
              + "VALUES (14, 'Aayat', 'Curran')");
          statement.execute(
              "INSERT INTO Singers (SingerId, FirstName, LastName)\n"
              + "VALUES (15, 'Tudor', 'Mccarthy')");
          statement.execute(
              "INSERT INTO Singers (SingerId, FirstName, LastName)\n"
              + "VALUES (16, 'Cobie', 'Webb')");
          statement.execute("ABORT BATCH");
          System.out.println("Aborted DML batch");
        }
      }
    }
  }

  /**
   * Samples showing how to use the custom methods that are defined on the {@link
   * CloudSpannerJdbcConnection} interface.
   */
  static class CustomMethodsSamples {
    /** Use {@link Mutation}s to insert new records. */
    static void bufferedWrite(String projectId, String instanceId, String databaseId)
        throws SQLException {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId);
      long singerId = 30;
      long albumId = 10;
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        // Unwrap the Cloud Spanner specific interface to be able to access custom methods.
        CloudSpannerJdbcConnection spannerConnection =
            connection.unwrap(CloudSpannerJdbcConnection.class);
        spannerConnection.setAutoCommit(false);
        Mutation mutationSingers =
            Mutation.newInsertBuilder("Singers")
                .set("SingerId")
                .to(singerId)
                .set("FirstName")
                .to("Marvin")
                .set("LastName")
                .to("Mooney")
                .build();
        Mutation mutationAlbums =
            Mutation.newInsertBuilder("Albums")
                .set("SingerId")
                .to(singerId)
                .set("AlbumId")
                .to(albumId)
                .set("AlbumTitle")
                .to("Hand in hand")
                .set("MarketingBudget")
                .to(1000)
                .build();
        spannerConnection.bufferedWrite(Arrays.asList(mutationSingers, mutationAlbums));
        spannerConnection.commit();
        System.out.printf(
            "Transaction committed at [%s]\n", spannerConnection.getCommitTimestamp().toString());
      }
    }

    /**
     * Example showing how to retry a transaction in case the transaction was aborted by Cloud
     * Spanner using Cloud Spanner specific interfaces and classes.
     *
     * <p>Google Cloud Spanner may abort a read/write transaction. The JDBC driver will by default
     * retry these transactions internally, unless the connection property <code>
     * retryAbortsInternally</code> has been set to <code>false</code>. In case of an aborted
     * transaction, the {@link Connection} will throw a {@link JdbcAbortedException}.
     */
    static void transactionWithRetryLoop(String projectId, String instanceId, String databaseId)
        throws SQLException {
      // Create a connection that has automatic retry for aborted transactions disabled.
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s"
              + ";retryAbortsInternally=false",
              projectId, instanceId, databaseId);
      long singerId = 31;
      long albumId = 11;
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        while (true) {
          try {
            CloudSpannerJdbcConnection spannerConnection =
                connection.unwrap(CloudSpannerJdbcConnection.class);
            spannerConnection.setAutoCommit(false);
            Mutation mutationSingers =
                Mutation.newInsertBuilder("Singers")
                    .set("SingerId")
                    .to(singerId)
                    .set("FirstName")
                    .to("Breanna")
                    .set("LastName")
                    .to("Fountain")
                    .build();
            Mutation mutationAlbums =
                Mutation.newInsertBuilder("Albums")
                    .set("SingerId")
                    .to(singerId)
                    .set("AlbumId")
                    .to(albumId)
                    .set("AlbumTitle")
                    .to("No discounts")
                    .set("MarketingBudget")
                    .to(1000)
                    .build();
            spannerConnection.bufferedWrite(Arrays.asList(mutationSingers, mutationAlbums));
            spannerConnection.commit();
            System.out.printf(
                "Transaction committed at [%s]\n",
                spannerConnection.getCommitTimestamp().toString());
            break;
          } catch (JdbcAbortedException e) {
            // Transaction aborted, retry.
            System.out.println("Transaction aborted, starting retry");
          }
        }
      }
    }

    /**
     * Example showing how to retry a transaction in case the transaction was aborted by Cloud
     * Spanner using only standard JDBC functionality.
     *
     * <p>Google Cloud Spanner may abort a read/write transaction. The JDBC driver will by default
     * retry these transactions internally, unless the connection property <code>
     * retryAbortsInternally</code> has been set to <code>false</code>.
     */
    static void genericJdbcTransactionWithRetryLoop(
        String projectId, String instanceId, String databaseId) throws SQLException {
      // Create a connection that has automatic retry for aborted transactions disabled.
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s"
              + ";retryAbortsInternally=false",
              projectId, instanceId, databaseId);
      long singerId = 32;
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        while (true) {
          try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps =
                connection.prepareStatement(
                    "INSERT INTO Singers (SingerId, FirstName, LastName) VALUES (?, ?, ?)")) {
              ps.setLong(1, singerId);
              ps.setString(2, "Marsha");
              ps.setString(3, "Roberts");
              ps.executeUpdate();
            }
            connection.commit();
            try (ResultSet rs =
                connection.createStatement().executeQuery("SHOW VARIABLE COMMIT_TIMESTAMP")) {
              if (rs.next()) {
                System.out.printf(
                    "Transaction committed at [%s]\n",
                    rs.getTimestamp("COMMIT_TIMESTAMP").toString());
              }
            }
            break;
          } catch (SQLException e) {
            if (e.getErrorCode() == Code.ABORTED.value()) {
              // Transaction aborted, retry.
              System.out.println("Transaction aborted, starting retry");
            } else {
              throw e;
            }
          }
        }
      }
    }
  }

  /** Examples showing transaction usage with the Cloud Spanner JDBC driver. */
  static class JdbcTransactionSamples {
    /** Execute a number of DML statements as one transaction. */
    static void readWriteTransaction(String projectId, String instanceId, String databaseId)
        throws SQLException {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        try (Statement statement = connection.createStatement()) {
          // Explicitly begin a transaction. If the connection is in autocommit mode, this will
          // create a temporary transaction. Otherwise, this is a no-op.
          statement.execute("BEGIN TRANSACTION");
          // This statement will set this transaction to be a read/write transaction, regardless of
          // the read/write / read-only state of the connection.
          statement.execute("SET TRANSACTION READ WRITE");
          statement.execute(
              "INSERT INTO Singers (SingerId, FirstName, LastName) VALUES (17, 'Aqib', 'Currie')");
          statement.execute(
              "INSERT INTO Singers (SingerId, FirstName, LastName) VALUES (18, 'Chaya', 'Best')");
          statement.execute(
              "INSERT INTO Singers (SingerId, FirstName, LastName) VALUES (19, 'Om', 'Marks')");
          connection.commit();
          Timestamp commitTimestamp =
              connection.unwrap(CloudSpannerJdbcConnection.class).getCommitTimestamp();
          System.out.printf(
              "Transaction committed with commit timestamp [%s]\n", commitTimestamp.toString());
        }
      }
    }

    /** Execute a read-only transaction. */
    static void readOnlyTransaction(String projectId, String instanceId, String databaseId)
        throws SQLException {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        try (Statement statement = connection.createStatement()) {
          // Explicitly begin a transaction. If the connection is in autocommit mode, this will
          // create a temporary transaction. Otherwise, this is a no-op.
          statement.execute("BEGIN TRANSACTION");
          // This statement will set this transaction to be a read-only transaction, regardless of
          // the read/write / read-only state of the connection.
          statement.execute("SET TRANSACTION READ ONLY");
          try (ResultSet rs =
              connection
                  .createStatement()
                  .executeQuery(
                      "SELECT SingerId, FirstName, LastName FROM Singers ORDER BY LastName")) {
            while (rs.next()) {
              System.out.printf("%d %s %s\n", rs.getLong(1), rs.getString(2), rs.getString(3));
            }
          }
          connection.commit();
          Timestamp readTimestamp =
              connection.unwrap(CloudSpannerJdbcConnection.class).getReadTimestamp();
          System.out.printf(
              "Read-only transaction used read timestamp [%s]\n", readTimestamp.toString());
        }
      }
    }
  }

  /** Examples showing how to get and set variable values from a Cloud Spanner JDBC connection. */
  static class JdbcVariablesExamples {
    /** Get the commit timestamp of a transaction. */
    static void getCommitTimestamp(String projectId, String instanceId, String databaseId)
        throws SQLException {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        if (connection.getAutoCommit()) {
          connection.setAutoCommit(false);
        }
        connection
            .createStatement()
            .executeUpdate(
                "INSERT INTO Singers (SingerId, FirstName, LastName)\n"
                + "VALUES (20, 'Tasneem', 'Rodgers')");
        connection.commit();
        try (ResultSet rs =
            connection.createStatement().executeQuery("SHOW VARIABLE COMMIT_TIMESTAMP")) {
          if (rs.next()) {
            System.out.printf(
                "Commit timestamp: [%s]\n", rs.getTimestamp("COMMIT_TIMESTAMP").toString());
          }
        }
      }
    }

    /** Get the read timestamp of a read-only transaction. */
    static void getReadTimestamp(String projectId, String instanceId, String databaseId)
        throws SQLException {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              projectId, instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        if (connection.getAutoCommit()) {
          connection.setAutoCommit(false);
        }
        if (!connection.isReadOnly()) {
          connection.setReadOnly(true);
        }
        try (ResultSet rs =
            connection.createStatement().executeQuery("SELECT * FROM Singers ORDER BY LastName")) {
          while (rs.next()) {
            System.out.printf("%d %s %s\n", rs.getLong(1), rs.getString(2), rs.getString(3));
          }
        }
        // Indicate the end of the read-only transaction.
        connection.commit();
        try (ResultSet rs =
            connection.createStatement().executeQuery("SHOW VARIABLE READ_TIMESTAMP")) {
          if (rs.next()) {
            System.out.printf(
                "Read timestamp: [%s]\n", rs.getTimestamp("READ_TIMESTAMP").toString());
          }
        }
      }
    }
  }
}
