/*
 * Copyright 2022 Google Inc.
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

class PgDmlWithParametersSample {

  static void pgDmlWithParameters() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    pgDmlWithParameters(projectId, instanceId, databaseId);
  }

  static void pgDmlWithParameters(String projectId, String instanceId, String databaseId)
      throws SQLException {
    try (Connection connection =
        DriverManager.getConnection(
            String.format(
                "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
                projectId, instanceId, databaseId))) {
      try (PreparedStatement statement =
          connection.prepareStatement(
              "INSERT INTO Singers (SingerId, FirstName, LastName) "
                  + "VALUES (?, ?, ?), (?, ?, ?)")) {
        statement.setLong(1, 10L);
        statement.setString(2, "Alice");
        statement.setString(3, "Henderson");
        statement.setLong(4, 11L);
        statement.setString(5, "Bruce");
        statement.setString(6, "Allison");
        int updateCount = statement.executeUpdate();
        System.out.printf("Inserted %d singers\n", updateCount);
      }
    }
  }
}