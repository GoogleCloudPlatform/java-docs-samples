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

import com.google.cloud.spanner.SpannerOptions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

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
    if (args.length != 3) {
      printUsageAndExit();
    }

    /*
     * CREATE TABLE Singers (
     *   SingerId   INT64 NOT NULL,
     *   FirstName  STRING(1024),
     *   LastName   STRING(1024),
     *   SingerInfo BYTES(MAX),
     * ) PRIMARY KEY (SingerId);
     */

    String command = args[0];
    String projectId = SpannerOptions.getDefaultProjectId();
    String instanceId = args[1];
    String databaseId = args[2];

    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      run(command, connection, projectId, instanceId, databaseId);
    }
  }

  static void run(
      String command, Connection connection, String projectId, String instanceId, String databaseId)
      throws SQLException {
    switch (command) {
      case "createtable":
        createTable(connection);
        break;
      case "insertdata":
        insertData(connection);
        break;
      case "connectionwithqueryoptions":
        connectionWithQueryOptions(projectId, instanceId, databaseId);
        break;
      case "setqueryoptions":
        setQueryOptions(projectId, instanceId, databaseId);
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
    System.err.println("    JdbcSample createtable my-instance example-db");
    System.err.println("    JdbcSample insertdata my-instance example-db");
    System.err.println("    JdbcSample connectionwithqueryoptions my-instance example-db");
    System.err.println("    JdbcSample setqueryoptions my-instance example-db");

    System.exit(1);
  }

  static void createTable(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(
          "CREATE TABLE Singers (\n"
              + "              SingerId   INT64 NOT NULL,\n"
              + "              FirstName  STRING(1024),\n"
              + "              LastName   STRING(1024),\n"
              + "              SingerInfo BYTES(MAX),\n"
              + "              ) PRIMARY KEY (SingerId)\n");
    }
    System.out.println("Created table [Singers]");
  }

  static void insertData(Connection connection) throws SQLException {
    boolean originalAutoCommit = connection.getAutoCommit();
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
    } finally {
      connection.setAutoCommit(originalAutoCommit);
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

}
