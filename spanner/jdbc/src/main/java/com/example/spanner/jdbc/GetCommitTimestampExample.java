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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class GetCommitTimestampExample {

  static void getCommitTimestamp() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    getCommitTimestamp(projectId, instanceId, databaseId);
  }

  @SuppressFBWarnings(
      value = "OBL_UNSATISFIED_OBLIGATION",
      justification = "https://github.com/spotbugs/spotbugs/issues/293")
  // Get the commit timestamp of a transaction.
  static void getCommitTimestamp(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl);
        Statement statement = connection.createStatement()) {
      connection.setAutoCommit(false);
      statement.executeUpdate(
          "INSERT INTO Singers (SingerId, FirstName, LastName)\n"
              + "VALUES (20, 'Tasneem', 'Rodgers')");
      connection.commit();
      try (ResultSet rs = statement.executeQuery("SHOW VARIABLE COMMIT_TIMESTAMP")) {
        if (rs.next()) {
          System.out.printf(
              "Commit timestamp: [%s]%n", rs.getTimestamp("COMMIT_TIMESTAMP").toString());
        }
      }
    }
  }
}
