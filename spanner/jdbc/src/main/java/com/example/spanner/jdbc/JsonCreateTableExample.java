/*
 * Copyright 2021 Google LLC
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

// [START spanner_jdbc_json_create_table]
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class JsonCreateTableExample {
  static void createTableWithJsonDataType() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    createTableWithJsonDataType(projectId, instanceId, databaseId);
  }

  static void createTableWithJsonDataType(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      connection
          .createStatement()
          .execute(
              "CREATE TABLE Venues (\n"
                  + "  VenueId      INT64,\n"
                  + "  VenueDetails JSON\n"
                  + ") PRIMARY KEY (VenueId)");
      System.out.println("Created table with JSON data type");
    }
  }
}
// [END spanner_jdbc_json_create_table]
