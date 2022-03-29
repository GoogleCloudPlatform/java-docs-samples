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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

class PgFunctionsSample {

  static void pgFunctions() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    pgFunctions(projectId, instanceId, databaseId);
  }

  static void pgFunctions(String projectId, String instanceId, String databaseId)
      throws SQLException {
    try (Connection connection =
        DriverManager.getConnection(
            String.format(
                "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
                projectId, instanceId, databaseId))) {
      // Use the PostgreSQL `to_timestamp` function to convert a number of seconds since epoch to a
      // timestamp. 1284352323 seconds = Monday, September 13, 2010 4:32:03 AM.
      try (ResultSet resultSet =
          connection.createStatement().executeQuery("SELECT to_timestamp(1284352323) AS t")) {
        while (resultSet.next()) {
          Timestamp timestamp = resultSet.getTimestamp("t");
          System.out.printf(
              "1284352323 seconds after epoch is %s\n",
              OffsetDateTime.ofInstant(
                  Instant.ofEpochMilli(timestamp.getTime()), ZoneId.of("UTC")));
        }
      }
    }
  }
}