/*
 * Copyright 2022 Google LLC.
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

import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.jdbc.CloudSpannerJdbcConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

class PgOrderNullsSample {

  static void pgOrderNulls() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    pgOrderNulls(projectId, instanceId, databaseId);
  }

  static void pgOrderNulls(String projectId, String instanceId, String databaseId)
      throws SQLException {
    // Create a JDBC connection to the database. A connection can be reused to execute multiple
    // statements. After completing all of your statements, call the "close" method on the
    // connection to safely clean up any remaining resources.
    try (Connection connection =
        DriverManager.getConnection(
            String.format(
                "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
                projectId, instanceId, databaseId))) {

      // Spanner PostgreSQL follows the ORDER BY rules for NULL values of PostgreSQL. This means
      // that:
      // 1. NULL values are ordered last by default when a query result is ordered in ascending
      //    order.
      // 2. NULL values are ordered first by default when a query result is ordered in descending
      //    order.
      // 3. NULL values can be order first or last by specifying NULLS FIRST or NULLS LAST in the
      //    ORDER BY clause.
      connection
          .createStatement()
          .execute(
              "CREATE TABLE Singers ("
                  + "  SingerId bigint NOT NULL PRIMARY KEY,"
                  + "  Name     varchar(1024)"
                  + ")");

      connection
          .unwrap(CloudSpannerJdbcConnection.class)
          .write(
              Arrays.asList(
                  Mutation.newInsertBuilder("Singers")
                      .set("SingerId")
                      .to(1L)
                      .set("Name")
                      .to("Alice")
                      .build(),
                  Mutation.newInsertBuilder("Singers")
                      .set("SingerId")
                      .to(2L)
                      .set("Name")
                      .to("Bruce")
                      .build(),
                  Mutation.newInsertBuilder("Singers")
                      .set("SingerId")
                      .to(3L)
                      .set("Name")
                      .to((String) null)
                      .build()));

      // This returns the singers in order Alice, Bruce, null
      System.out.println("Singers ORDER BY Name");
      try (ResultSet singers =
          connection.createStatement().executeQuery("SELECT * FROM Singers ORDER BY Name")) {
        printSingerNames(singers);
      }

      // This returns the singers in order null, Bruce, Alice
      System.out.println("Singers ORDER BY Name DESC");
      try (ResultSet singers =
          connection.createStatement().executeQuery("SELECT * FROM Singers ORDER BY Name DESC")) {
        printSingerNames(singers);
      }

      // This returns the singers in order null, Alice, Bruce
      System.out.println("Singers ORDER BY Name NULLS FIRST");
      try (ResultSet singers =
          connection
              .createStatement()
              .executeQuery("SELECT * FROM Singers ORDER BY Name NULLS FIRST")) {
        printSingerNames(singers);
      }

      // This returns the singers in order Bruce, Alice, null
      System.out.println("Singers ORDER BY Name DESC NULLS LAST");
      try (ResultSet singers =
          connection
              .createStatement()
              .executeQuery("SELECT * FROM Singers ORDER BY Name DESC NULLS LAST")) {
        printSingerNames(singers);
      }
    }
  }

  static void printSingerNames(ResultSet singers) throws SQLException {
    while (singers.next()) {
      System.out.printf(
          "\t%s\n", singers.getString("name") == null ? "<null>" : singers.getString("name"));
    }
  }
}