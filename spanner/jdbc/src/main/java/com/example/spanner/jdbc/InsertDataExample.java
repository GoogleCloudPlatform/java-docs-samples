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

//[START spanner_jdbc_insert]
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

class InsertDataExample {
  // Class to contain singer sample data.
  static class Singer {
    final long singerId;
    final String firstName;
    final String lastName;
    final BigDecimal revenues;

    Singer(long singerId, String firstName, String lastName, BigDecimal revenues) {
      this.singerId = singerId;
      this.firstName = firstName;
      this.lastName = lastName;
      this.revenues = revenues;
    }
  }

  static final List<Singer> SINGERS =
      Arrays.asList(
          new Singer(10, "Marc", "Richards", new BigDecimal("104100.00")),
          new Singer(20, "Catalina", "Smith", new BigDecimal("9880.99")),
          new Singer(30, "Alice", "Trentor", new BigDecimal("300183")),
          new Singer(40, "Lea", "Martin", new BigDecimal("20118.12")),
          new Singer(50, "David", "Lomond", new BigDecimal("311399.26")));

  static void insertData() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    insertData(projectId, instanceId, databaseId);
  }

  @SuppressFBWarnings(
      value = "OBL_UNSATISFIED_OBLIGATION",
      justification = "https://github.com/spotbugs/spotbugs/issues/293")
  static void insertData(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      try (PreparedStatement ps =
          connection.prepareStatement(
              "INSERT INTO Singers\n"
                  + "(SingerId, FirstName, LastName, SingerInfo, Revenues)\n"
                  + "VALUES\n"
                  + "(?, ?, ?, ?, ?)")) {
        for (Singer singer : SINGERS) {
          ps.setLong(1, singer.singerId);
          ps.setString(2, singer.firstName);
          ps.setString(3, singer.lastName);
          ps.setNull(4, Types.BINARY);
          ps.setBigDecimal(5, singer.revenues);
          ps.addBatch();
        }
        int[] updateCounts = ps.executeBatch();
        System.out.printf("Insert counts: %s%n", Arrays.toString(updateCounts));
      }
    }
  }
}
//[END spanner_jdbc_insert]
