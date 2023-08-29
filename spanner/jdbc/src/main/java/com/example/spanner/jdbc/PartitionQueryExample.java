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

//[START spanner_jdbc_partition_query]
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PartitionQueryExample {

  public static void main(String[] args) throws SQLException {
    partitionQuery();
  }

  static void partitionQuery() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    partitionQuery(projectId, instanceId, databaseId);
  }

  // This example shows how to partition a query and execute each returned partition with the JDBC
  // driver.
  static void partitionQuery(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl = String.format("jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
        projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(
        connectionUrl); Statement statement = connection.createStatement()) {

      // Partition a query and then execute each partition sequentially.
      List<String> partitions = new ArrayList<>();
      // This will partition the query and return a result set with the partition IDs encoded as a
      // string. Each of these partition IDs can be executed with "RUN PARTITION '<partition-id>'".
      System.out.println("Partitioning query 'SELECT SingerId, FirstName, LastName from singers'");
      try (ResultSet partitionsResultSet = statement.executeQuery(
          "PARTITION SELECT SingerId, FirstName, LastName from Singers")) {
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
      System.out.println("Finished executing all partitions");
    }
  }
}
//[END spanner_jdbc_partition_query]
