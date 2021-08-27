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

// [START spanner_jdbc_json_insert_data]

import com.google.cloud.spanner.Value;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

class JsonInsertDataExample {
  // Class to contain Venue sample data.
  static class Venue {
    final long venueId;
    final String venueDetails;

    Venue(long venueId, String venueDetails) {
      this.venueId = venueId;
      this.venueDetails = venueDetails;
    }
  }

  static final List<Venue> VENUES =
      Arrays.asList(
          new Venue(
              4, "[{\"name\":\"room 1\",\"open\":true},{\"name\":\"room 2\",\"open\":false}]"),
          new Venue(19, "{\"rating\":9,\"open\":true}"),
          new Venue(
              42,
              "\"{\"name\":null,"
                  + "\"open\":{\"Monday\":true,\"Tuesday\":false},"
                  + "\"tags\":[\"large\",\"airy\"]}"));

  static void insertJsonData() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    insertJsonData(projectId, instanceId, databaseId);
  }

  static void insertJsonData(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      try (PreparedStatement ps =
          connection.prepareStatement("INSERT INTO Venues\n"
                      + "(VenueId, VenueDetails)\n"
                      + "VALUES\n"
                      + "(?, ?)")) {
        for (Venue venue : VENUES) {
          ps.setLong(1, venue.venueId);
          ps.setObject(2, Value.json(venue.venueDetails));
          ps.addBatch();
        }
        int[] updateCounts = ps.executeBatch();
        System.out.printf("Insert counts: %s%n", Arrays.toString(updateCounts));
      }
    }
  }
}
// [END spanner_jdbc_json_insert_data]
