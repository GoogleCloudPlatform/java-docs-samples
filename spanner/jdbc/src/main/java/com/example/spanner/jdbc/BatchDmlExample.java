/*
 * Copyright 2020 Google Inc.
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

//[START spanner_jdbc_batch_transaction]
import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

class BatchDmlExample {
  static class Singer {
    final long singerId;
    final String firstName;
    final String lastName;
    final BigDecimal revenues;
    
    Singer(long singerId, String firstName, String lastName, BigDecimal revenues) {
      this.singerId = singerId;
      this.firstName = firstName;
      this.lastName = lastName;
      this.revenues = revenues;
    }
  }

  static void batchDml() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    batchDml(projectId, instanceId, databaseId);
  }

  // This example shows how to execute a batch of DML statements with the JDBC driver.
  static void batchDml(String projectId, String instanceId, String databaseId) throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);

    ImmutableList<Singer> singers = ImmutableList.of(
        new Singer(10, "Marc", "Richards", BigDecimal.valueOf(10000)),
        new Singer(11, "Amirah", "Finney", BigDecimal.valueOf(195944.10d)),
        new Singer(12, "Reece", "Dunn", BigDecimal.valueOf(10449.90))
    );
    
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      connection.setAutoCommit(false);
      // Use prepared statements for the lowest possible latency when executing the same SQL string
      // multiple times.
      try (PreparedStatement statement = connection.prepareStatement(
          "INSERT INTO Singers (SingerId, FirstName, LastName, Revenues)\n" 
              + "VALUES (?, ?, ?, ?)")) {
        for (Singer singer : singers) {
          statement.setLong(1, singer.singerId);
          statement.setString(2, singer.firstName);
          statement.setString(3, singer.lastName);
          statement.setBigDecimal(4, singer.revenues);
          // Add the current parameter values to the batch.
          statement.addBatch();
        }
        // Execute the batched statements.
        int[] updateCounts = statement.executeBatch();
        connection.commit();
        System.out.printf("Batch insert counts: %s%n", Arrays.toString(updateCounts));
      }
    }
  }
}
//[END spanner_jdbc_batch_transaction]
