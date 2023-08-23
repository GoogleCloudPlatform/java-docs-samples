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

//[START spanner_jdbc_data_boost]
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBoostExample {

  public static void main(String[] args) throws SQLException {
    dataBoost();
  }

  static void dataBoost() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    dataBoost(projectId, instanceId, databaseId);
  }

  // This example shows how to execute queries with data boost using the JDBC driver.
  static void dataBoost(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl = String.format("jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
        projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(
        connectionUrl); Statement statement = connection.createStatement()) {
      
      // A connection can also be set to 'auto_partition_mode', which will instruct it to execute
      // all queries as a partitioned query. This is essentially the same as automatically prefixing
      // all queries with 'RUN PARTITIONED QUERY ...'.
      statement.execute("set auto_partition_mode=true");
      
      // This will execute at most max_partitioned_parallelism partitions in parallel.
      statement.execute("set max_partitioned_parallelism=8");
      
      // Setting 'data_boost_enabled' to true will instruct the JDBC connection to execute all
      // partitioned queries using Data Boost. This setting applies to all the above methods that
      // can be used to run a partitioned query:
      // 1. RUN PARTITION '...'
      // 2. RUN PARTITIONED QUERY ...
      // 3. SET AUTO_PARTITION_MODE=TRUE; SELECT ...
      statement.execute("set data_boost_enabled=true");
      
      // This query will be executed as a partitioned query using data boost.
      try (ResultSet resultSet = statement.executeQuery(
          "SELECT SingerId, FirstName, LastName FROM singers")) {
        while (resultSet.next()) {
          System.out.printf("%s %s %s%n", resultSet.getString(1), resultSet.getString(2),
              resultSet.getString(3));
        }
      }
    }
  }
}
//[END spanner_jdbc_data_boost]
