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

import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.jdbc.CloudSpannerJdbcConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

class PgCaseSensitivitySample {

  static void pgCaseSensitivity() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    pgCaseSensitivity(projectId, instanceId, databaseId);
  }

  static void pgCaseSensitivity(String projectId, String instanceId, String databaseId)
      throws SQLException {
    try (Connection connection =
        DriverManager.getConnection(
            String.format(
                "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
                projectId, instanceId, databaseId))) {

      // Spanner PostgreSQL follows the case sensitivity rules of PostgreSQL. This means that:
      // 1. Identifiers that are not double-quoted are folded to lower case.
      // 2. Identifiers that are double-quoted retain their case and are case-sensitive.
      // See https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS
      // for more information.

      connection
          .createStatement()
          .execute(
              "CREATE TABLE Singers ("
                  // SingerId will be folded to `singerid`.
                  + "  SingerId      bigint NOT NULL PRIMARY KEY,"
                  // FirstName and LastName are double-quoted and will therefore retain their
                  // mixed case and are case-sensitive. This means that any statement that
                  // references any of these columns must use double quotes.
                  + "  \"FirstName\" varchar(1024) NOT NULL,"
                  + "  \"LastName\"  varchar(1024) NOT NULL"
                  + ")");

      connection
          .unwrap(CloudSpannerJdbcConnection.class)
          .write(
              Collections.singleton(
                  Mutation.newInsertBuilder("Singers")
                      .set("singerid")
                      .to(1L)
                      // Column names in mutations are always case-insensitive, regardless whether
                      // the columns were double-quoted or not during creation.
                      .set("firstname")
                      .to("Bruce")
                      .set("lastname")
                      .to("Allison")
                      .build()));

      try (ResultSet singers = connection
          .createStatement()
          .executeQuery("SELECT SingerId, \"FirstName\", \"LastName\" FROM Singers")) {
        while (singers.next()) {
          System.out.printf(
              "SingerId: %d, FirstName: %s, LastName: %s\n",
              // SingerId is automatically folded to lower case. Accessing the column by its name in
              // a result set must therefore use all lower-case letters.
              singers.getLong("singerid"),
              // FirstName and LastName were double-quoted during creation, and retain their mixed
              // case when returned in a result set.
              singers.getString("FirstName"),
              singers.getString("LastName"));
        }
      }

      // Aliases are also identifiers, and specifying an alias in double quotes will make the alias
      // retain its case.
      try (ResultSet singers =
          connection
              .createStatement()
              .executeQuery(
                  "SELECT "
                      + "singerid AS \"SingerId\", "
                      + "\"FirstName\" || ' ' || \"LastName\" AS \"FullName\" "
                      + "FROM Singers")) {
        while (singers.next()) {
          System.out.printf(
              "SingerId: %d, FullName: %s\n",
              // The aliases are double-quoted and therefore retains their mixed case.
              singers.getLong("SingerId"), singers.getString("FullName"));
        }
      }

      // DML statements must also follow the PostgreSQL case rules.
      try (PreparedStatement statement =
          connection.prepareStatement(
              "INSERT INTO Singers (SingerId, \"FirstName\", \"LastName\") "
                  + "VALUES (?, ?, ?)")) {
        statement.setLong(1, 2L);
        statement.setString(2, "Alice");
        statement.setString(3, "Bruxelles");
      }
    }
  }
}