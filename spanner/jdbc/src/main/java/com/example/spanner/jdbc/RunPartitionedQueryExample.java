/*
 * Copyright 2023 Google LLC
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

//[START spanner_jdbc_run_partitioned_query]
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

  // This example shows how to run a query directly as a partitioned query on a JDBC connection.
  // The query will be partitioned and each partition will be executed using the same JDBC
  // connection. You can set the maximum parallelism that should be used to execute the query with
  // the SQL statement 'SET max_partitioned_parallelism=<parallelism>'.
  static void runPartitionedQuery(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl = String.format("jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
        projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(
        connectionUrl); Statement statement = connection.createStatement()) {
      // Run a query directly as a partitioned query.
      // This will execute at most max_partitioned_parallelism partitions in parallel.
      statement.execute("set max_partitioned_parallelism=8");
      try (ResultSet resultSet = statement.executeQuery(
          "RUN PARTITIONED QUERY SELECT SingerId, FirstName, LastName FROM singers")) {
        while (resultSet.next()) {
          System.out.printf("%s %s %s%n", resultSet.getString(1), resultSet.getString(2),
              resultSet.getString(3));
        }
      }
    }
  }
}
//[END spanner_jdbc_run_partitioned_query]
