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

class PgConnectToDatabaseSample {

  static void pgConnectToDatabase() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    pgConnectToDatabase(projectId, instanceId, databaseId);
  }

  // Creates a JDBC connection to a Cloud Spanner database with the PostgreSQL dialect.
  static void pgConnectToDatabase(String projectId, String instanceId, String databaseId)
      throws SQLException {
    // Connecting to a Cloud Spanner PostgreSQL database using the Spanner JDBC driver uses the same
    // JDBC URL as for normal Spanner databases. The JDBC driver will automatically detect the
    // dialect that is used by the underlying database.
    try (Connection connection =
        DriverManager.getConnection(
            String.format(
                "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
                projectId, instanceId, databaseId))) {
      try (ResultSet rs = connection.createStatement().executeQuery("SELECT now()")) {
        while (rs.next()) {
          System.out.printf(
              "Connected to Cloud Spanner PostgreSQL at [%s]%n", rs.getTimestamp(1).toString());
        }
      }
    }
  }
}