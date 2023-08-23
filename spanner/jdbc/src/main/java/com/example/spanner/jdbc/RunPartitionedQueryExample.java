/*
 * Copyright 2023 Google Inc.
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

package com.example.spanner.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RunPartitionedQueryExample {

  public static void main(String[] args) throws SQLException {
    runPartitionedQuery();
  }

  static void runPartitionedQuery() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    runPartitionedQuery(projectId, instanceId, databaseId);
  }

  static void runPartitionedQuery(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl = String.format("jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
        projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(
        connectionUrl); Statement statement = connection.createStatement()) {

      // Partition a query and then execute each of them serially.
      List<String> partitions = new ArrayList<>();
      // This will partition the query and return a result set with the partition IDs encoded as a
      // string. Each of these partition IDs can be executed with "RUN PARTITION '<partition-id>'".
      System.out.println("Partitioning query 'SELECT id, firstname, lastname from singers'");
      try (ResultSet partitionsResultSet = statement.executeQuery(
          "PARTITION SELECT id, firstname, lastname from singers")) {
        while (partitionsResultSet.next()) {
          partitions.add(partitionsResultSet.getString(1));
        }
      }
      System.out.printf("Partition command returned %d partitions\n", partitions.size());

      // This executes the partitions serially on the same connection, but each partition can also
      // be executed on a different JDBC connection (even on a different host).
      for (String partitionId : partitions) {
        try (ResultSet resultSet = statement.executeQuery(
            String.format("RUN PARTITION '%s'", partitionId))) {
          while (resultSet.next()) {
            System.out.printf("%s %s %s%n", resultSet.getString(1), resultSet.getString(2),
                resultSet.getString(3));
          }
        }
      }

      // The partition ID can also be set as a query parameter. The query parameter does not need to
      // be specified in the query string.
      try (PreparedStatement preparedStatement = connection.prepareStatement("RUN PARTITION")) {
        for (String partitionId : partitions) {
          preparedStatement.setString(1, partitionId);
          try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
              System.out.printf("%s %s %s%n", resultSet.getString(1), resultSet.getString(2),
                  resultSet.getString(3));
            }
          }
        }
      }

      // Run a query directly as a partitioned query.
      // This will execute at most max_partitioned_parallelism partitions in parallel.
      statement.execute("set max_partitioned_parallelism=8");
      try (ResultSet resultSet = statement.executeQuery(
          "RUN PARTITIONED QUERY SELECT id, firstname, lastname FROM singers")) {
        while (resultSet.next()) {
          System.out.printf("%s %s %s%n", resultSet.getString(1), resultSet.getString(2),
              resultSet.getString(3));
        }
      }

      // A connection can also be set to 'auto_partition_mode', which will instruct it to execute
      // all queries as a partitioned query. This is essentially the same as automatically prefixing
      // all queries with 'RUN PARTITIONED QUERY ...'.
      statement.execute("set auto_partition_mode=true");
      try (ResultSet resultSet = statement.executeQuery(
          "SELECT id, firstname, lastname FROM singers")) {
        while (resultSet.next()) {
          System.out.printf("%s %s %s%n", resultSet.getString(1), resultSet.getString(2),
              resultSet.getString(3));
        }
      }

      // Setting 'data_boost_enabled' to true will instruct the JDBC connection to execute all
      // partitioned queries using Data Boost. This setting applies to all the above methods that
      // can be used to run a partitioned query:
      // 1. RUN PARTITION '...'
      // 2. RUN PARTITIONED QUERY ...
      // 3. SET AUTO_PARTITION_MODE=TRUE; SELECT ...
      statement.execute("set data_boost_enabled=true");
      // This query will be executed as a partitioned query, because auto_partition_mode=true was
      // set for this connection.
      try (ResultSet resultSet = statement.executeQuery(
          "SELECT id, firstname, lastname FROM singers")) {
        long rowCount = 0L;
        while (resultSet.next()) {
          System.out.printf("%s %s %s%n", resultSet.getString(1), resultSet.getString(2),
              resultSet.getString(3));
          rowCount++;
        }
        System.out.printf("Data boost query returned %d rows\n", rowCount);
      }
    }
  }
}
