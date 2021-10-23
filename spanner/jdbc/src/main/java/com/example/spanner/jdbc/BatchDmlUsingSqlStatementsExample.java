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
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

class BatchDmlUsingSqlStatementsExample {

  static void batchDmlUsingSqlStatements() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    batchDmlUsingSqlStatements(projectId, instanceId, databaseId);
  }

  @SuppressFBWarnings(
      value = "OBL_UNSATISFIED_OBLIGATION",
      justification = "https://github.com/spotbugs/spotbugs/issues/293")
  static void batchDmlUsingSqlStatements(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl);
        Statement statement = connection.createStatement()) {
      statement.execute("START BATCH DML");
      statement.execute(
          "INSERT INTO Singers (SingerId, FirstName, LastName, Revenues)\n"
              + "VALUES (14, 'Aayat', 'Curran', 12004.82)");
      statement.execute(
          "INSERT INTO Singers (SingerId, FirstName, LastName, Revenues)\n"
              + "VALUES (15, 'Tudor', 'Mccarthy', 38193.20)");
      statement.execute(
          "INSERT INTO Singers (SingerId, FirstName, LastName, Revenues)\n"
              + "VALUES (16, 'Cobie', 'Webb', 52909.87)");
      statement.execute("RUN BATCH");
      // The 'RUN BATCH' statement returns the update counts as a result set.
      try (ResultSet rs = statement.getResultSet()) {
        if (rs.next()) {
          Array array = rs.getArray("UPDATE_COUNTS");
          // 'RUN BATCH' returns the update counts as an array of Longs, as this is the default
          // for Cloud Spanner.
          Long[] updateCounts = (Long[]) array.getArray();
          System.out.printf("Batch insert counts: %s%n", Arrays.toString(updateCounts));
        }
      }
    }
  }
}
