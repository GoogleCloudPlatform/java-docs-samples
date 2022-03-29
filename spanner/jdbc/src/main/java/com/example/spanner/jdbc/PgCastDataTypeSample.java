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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Base64;

class PgCastDataTypeSample {

  static void pgCastDataType() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    pgCastDataType(projectId, instanceId, databaseId);
  }

  static void pgCastDataType(String projectId, String instanceId, String databaseId)
      throws SQLException {
    try (Connection connection =
        DriverManager.getConnection(
            String.format(
                "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
                projectId, instanceId, databaseId))) {
      // The `::` cast operator can be used to cast from one data type to another.
      try (ResultSet resultSet =
          connection
              .createStatement()
              .executeQuery(
                  "select 1::varchar as str, '2'::int as int, 3::decimal as dec,"
                      + "'4'::bytea as bytes, 5::float as float, 'true'::bool as bool, "
                      + "'2021-11-03T09:35:01UTC'::timestamptz as timestamp")) {
        while (resultSet.next()) {
          System.out.printf("String: %s\n", resultSet.getString("str"));
          System.out.printf("Int: %d\n", resultSet.getLong("int"));
          System.out.printf("Decimal: %s\n", resultSet.getBigDecimal("dec"));
          System.out.printf(
              "Bytes: %s\n", Base64.getEncoder().encodeToString(resultSet.getBytes("bytes")));
          System.out.printf("Float: %f\n", resultSet.getDouble("float"));
          System.out.printf("Bool: %s\n", resultSet.getBoolean("bool"));
          System.out.printf(
              "Timestamp: %s\n",
              OffsetDateTime.ofInstant(
                  Instant.ofEpochMilli(resultSet.getTimestamp("timestamp").getTime()),
                  ZoneId.of("UTC")));
        }
      }
    }
  }
}