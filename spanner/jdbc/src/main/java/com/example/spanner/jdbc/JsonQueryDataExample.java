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

// [START spanner_jdbc_json_query_data]
import com.google.cloud.spanner.jdbc.JsonType;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class JsonQueryDataExample {
  static void queryJsonData() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    queryJsonData(projectId, instanceId, databaseId);
  }

  static void queryJsonData(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    String exampleJson = "{\"rating\": \"9\"}";
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      try (PreparedStatement ps =
          connection.prepareStatement(
              "SELECT VenueId, VenueDetails\n"
                  + "FROM Venues\n"
                  + "WHERE JSON_VALUE(VenueDetails, '$.rating') = "
                  + "JSON_VALUE(?, '$.rating')")) {
        // Instruct the JDBC driver to treat the parameter as JSON and not as a string.
        ps.setObject(1, exampleJson, JsonType.INSTANCE);
        try (ResultSet resultSet = ps.executeQuery()) {
          while (resultSet.next()) {
            System.out.printf(
                "VenueId: %s, VenueDetails: %s%n",
                resultSet.getLong("VenueId"), resultSet.getString("VenueDetails"));
          }
        }
      }
    }
  }
}
// [END spanner_jdbc_json_query_data]
