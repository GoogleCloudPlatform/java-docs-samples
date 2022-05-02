/*
 * Copyright 2022 Google LLC
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
import com.google.cloud.spanner.Value;
import com.google.cloud.spanner.jdbc.CloudSpannerJdbcConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;

class PgNumericDataTypeSample {

  static void pgNumericDataType() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    pgNumericDataType(projectId, instanceId, databaseId);
  }

  static void pgNumericDataType(String projectId, String instanceId, String databaseId)
      throws SQLException {
    // Create a JDBC connection to the database. A connection can be reused to execute multiple
    // statements. After completing all of your statements, call the "close" method on the
    // connection to safely clean up any remaining resources.
    try (Connection connection =
        DriverManager.getConnection(
            String.format(
                "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
                projectId, instanceId, databaseId))) {
      // Create a table that includes a column with data type NUMERIC. As the database has been
      // created with the PostgreSQL dialect, the data type that is used will be the PostgreSQL
      // NUMERIC / DECIMAL data type.
      connection
          .createStatement()
          .execute(
              "CREATE TABLE Venues ("
                  + "  VenueId  bigint NOT NULL PRIMARY KEY,"
                  + "  Name     varchar(1024) NOT NULL,"
                  + "  Revenues numeric"
                  + ")");
      System.out.print("Created Venues table\n");

      // Insert a Venue using DML.
      try (PreparedStatement statement =
          connection.prepareStatement(
              "INSERT INTO Venues (VenueId, Name, Revenues) " + "VALUES (?, ?, ?)")) {
        statement.setLong(1, 1L);
        statement.setString(2, "Venue 1");
        statement.setBigDecimal(3, new BigDecimal("3150.25"));
        int updateCount = statement.executeUpdate();
        System.out.printf("Inserted %d venues\n", updateCount);
      }

      // Insert a Venue with a NULL value for the Revenues column.
      try (PreparedStatement statement =
          connection.prepareStatement(
              "INSERT INTO Venues (VenueId, Name, Revenues) " + "VALUES (?, ?, ?)")) {
        statement.setLong(1, 2L);
        statement.setString(2, "Venue 2");
        statement.setNull(3, Types.NUMERIC);
        int updateCount = statement.executeUpdate();
        System.out.printf("Inserted %d venues with NULL revenues\n", updateCount);
      }

      // Insert a Venue with a NaN (Not a Number) value for the Revenues column.
      try (PreparedStatement statement =
          connection.prepareStatement(
              "INSERT INTO Venues (VenueId, Name, Revenues) " + "VALUES (?, ?, ?)")) {
        statement.setLong(1, 3L);
        statement.setString(2, "Venue 3");
        // Not a Number (NaN) can be set both using the Double.NaN constant or the String 'NaN'.
        statement.setDouble(3, Double.NaN);
        int updateCount = statement.executeUpdate();
        System.out.printf("Inserted %d venues with NaN revenues\n", updateCount);
      }

      // Get all Venues and inspect the Revenues values.
      try (ResultSet venues =
          connection.createStatement().executeQuery("SELECT Name, Revenues FROM Venues")) {
        while (venues.next()) {
          String name = venues.getString("name");
          // Getting a PostgreSQL NUMERIC value as a Value is always supported, regardless whether
          // the value is a number, NULL or NaN.
          Value revenuesAsValue = venues.getObject("revenues", Value.class);
          System.out.printf("Revenues of %s: %s\n", name, revenuesAsValue);

          // Getting a PostgreSQL NUMERIC value as a double is supported for all possible values. If
          // the value is NULL, this method will return 0 and the wasNull() method will return true.
          double revenuesAsDouble = venues.getDouble("revenues");
          boolean wasNull = venues.wasNull();
          if (wasNull) {
            System.out.printf("\tRevenues of %s as double: null\n", name);
          } else {
            System.out.printf("\tRevenues of %s as double: %f\n", name, revenuesAsDouble);
          }

          // Getting a PostgreSQL NUMERIC as a BigDecimal is supported for both NULL and non-NULL
          // values, but not for NaN, as there is no BigDecimal representation of NaN.
          if (!Double.valueOf(revenuesAsDouble).isNaN()) {
            BigDecimal revenuesAsBigDecimal = venues.getBigDecimal("revenues");
            System.out.printf("\tRevenues of %s as BigDecimal: %s\n", name, revenuesAsBigDecimal);
          }

          // A PostgreSQL NUMERIC value may also be retrieved as a String.
          String revenuesAsString = venues.getString("revenues");
          System.out.printf("\tRevenues of %s as String: %s\n", name, revenuesAsString);
        }
      }

      // Mutations can also be used to insert/update NUMERIC values, including NaN values.
      // Mutations can be used with the JDBC driver by unwrapping the
      // com.google.cloud.spanner.jdbc.CloudSpannerJdbcConnection interface from the connection.
      CloudSpannerJdbcConnection cloudSpannerJdbcConnection =
          connection.unwrap(CloudSpannerJdbcConnection.class);
      cloudSpannerJdbcConnection.write(
          Arrays.asList(
              Mutation.newInsertBuilder("Venues")
                  .set("VenueId")
                  .to(4L)
                  .set("Name")
                  .to("Venue 4")
                  .set("Revenues")
                  .to(Value.pgNumeric("125.10"))
                  .build(),
              Mutation.newInsertBuilder("Venues")
                  .set("VenueId")
                  .to(5L)
                  .set("Name")
                  .to("Venue 5")
                  .set("Revenues")
                  .to(Value.pgNumeric(Value.NAN))
                  .build()));
      Timestamp commitTimestamp = cloudSpannerJdbcConnection.getCommitTimestamp();
      System.out.printf("Inserted 2 Venues using mutations at %s\n", commitTimestamp);
    }
  }
}