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

public class SingleUseReadOnlyTimestampBoundExample {

  static void singleUseReadOnlyTimestampBound() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    singleUseReadOnlyTimestampBound(projectId, instanceId, databaseId);
  }

  @SuppressFBWarnings(
      value = "OBL_UNSATISFIED_OBLIGATION",
      justification = "https://github.com/spotbugs/spotbugs/issues/293")
  static void singleUseReadOnlyTimestampBound(
      String projectId, String instanceId, String databaseId) throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl);
        Statement statement = connection.createStatement()) {
      // Set staleness to max 10 seconds. This is only allowed in autocommit mode.
      statement.execute("SET READ_ONLY_STALENESS = 'MAX_STALENESS 10s'");
      try (ResultSet rs =
          statement.executeQuery(
              "SELECT SingerId, FirstName, LastName, Revenues FROM Singers ORDER BY LastName")) {
        while (rs.next()) {
          System.out.printf(
              "%d %s %s %s%n",
              rs.getLong(1), rs.getString(2), rs.getString(3), rs.getBigDecimal(4));
        }
      }
      // Get the read timestamp that was used by the query.
      try (ResultSet rs = statement.executeQuery("SHOW VARIABLE READ_TIMESTAMP")) {
        while (rs.next()) {
          System.out.printf("Read timestamp used: %s%n", rs.getTimestamp(1));
        }
      }
    }
  }
}
