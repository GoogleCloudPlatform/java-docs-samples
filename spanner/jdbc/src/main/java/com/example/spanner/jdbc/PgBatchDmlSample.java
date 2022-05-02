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

package com.example.spanner.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

class PgBatchDmlSample {

  static void pgBatchDml() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    pgBatchDml(projectId, instanceId, databaseId);
  }

  static void pgBatchDml(String projectId, String instanceId, String databaseId)
      throws SQLException {
    // Create a JDBC connection to the database. A connection can be reused to execute multiple
    // statements. After completing all of your statements, call the "close" method on the
    // connection to safely clean up any remaining resources.
    try (Connection connection =
        DriverManager.getConnection(
            String.format(
                "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
                projectId, instanceId, databaseId))) {
      // Spanner PostgreSQL supports BatchDML statements. This will batch multiple DML statements
      // into one request, which reduces the number of round trips that is needed for multiple DML
      // statements. Use the standard JDBC PreparedStatement batching feature to batch multiple DML
      // statements together.
      try (PreparedStatement statement =
          connection.prepareStatement(
              "INSERT INTO Singers (SingerId, FirstName, LastName) " + "VALUES (?, ?, ?)")) {
        statement.setLong(1, 10L);
        statement.setString(2, "Alice");
        statement.setString(3, "Henderson");
        statement.addBatch();

        statement.setLong(1, 11L);
        statement.setString(2, "Bruce");
        statement.setString(3, "Allison");
        statement.addBatch();

        int[] updateCounts = statement.executeBatch();
        int totalUpdateCount = Arrays.stream(updateCounts).sum();
        System.out.printf("Inserted %d singers\n", totalUpdateCount);
      }
    }
  }
}