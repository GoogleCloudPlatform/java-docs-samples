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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class PgQueryParameterSample {

  static void pgQueryParameter() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    pgQueryParameter(projectId, instanceId, databaseId);
  }

  static void pgQueryParameter(String projectId, String instanceId, String databaseId)
      throws SQLException {
    // Create a JDBC connection to the database. A connection can be reused to execute multiple
    // statements. After completing all of your statements, call the "close" method on the
    // connection to safely clean up any remaining resources.
    try (Connection connection =
        DriverManager.getConnection(
            String.format(
                "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
                projectId, instanceId, databaseId))) {
      try (PreparedStatement statement =
          connection.prepareStatement(
              "SELECT SingerId, FirstName, LastName "
                  + "FROM Singers "
                  + "WHERE LastName LIKE ?")) {
        statement.setString(1, "A%");
        System.out.print("Listing all singers with a last name that starts with 'A'\n");
        try (ResultSet resultSet = statement.executeQuery()) {
          while (resultSet.next()) {
            // Note that the PostgreSQL dialect will return all column names in lower case, unless
            // the
            // columns have been created with case-sensitive column names. See
            // https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS
            // for more information on PostgreSQL identifiers.
            System.out.printf(
                "%d %s %s%n",
                resultSet.getLong("singerid"),
                resultSet.getString("firstname"),
                resultSet.getString("lastname"));
          }
        }
      }
    }
  }
}