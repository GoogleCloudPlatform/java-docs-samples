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
import java.sql.SQLException;
import java.sql.Statement;

class PgCreateInterleavedTableSample {

  static void pgCreateInterleavedTable() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    pgCreateInterleavedTable(projectId, instanceId, databaseId);
  }

  static void pgCreateInterleavedTable(String projectId, String instanceId, String databaseId)
      throws SQLException {
    // Create a JDBC connection to the database. A connection can be reused to execute multiple
    // statements. After completing all of your statements, call the "close" method on the
    // connection to safely clean up any remaining resources.
    try (Connection connection =
        DriverManager.getConnection(
            String.format(
                "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
                projectId, instanceId, databaseId))) {
      try (Statement statement = connection.createStatement()) {
        // The Spanner PostgreSQL dialect extends the PostgreSQL dialect with certain Spanner
        // specific features, such as interleaved tables.
        // See
        // https://cloud.google.com/spanner/docs/postgresql/data-definition-language#create_table
        // for the full CREATE TABLE syntax. The tables are created in one batch by adding the
        // individual DDL statements to a JDBC batch and then executed as a single batch.
        statement.addBatch(
            "CREATE TABLE Singers ("
                + "  SingerId  bigint NOT NULL PRIMARY KEY,"
                + "  FirstName varchar(1024) NOT NULL,"
                + "  LastName  varchar(1024) NOT NULL"
                + ")");
        statement.addBatch(
            "CREATE TABLE Albums ("
                + "  SingerId bigint NOT NULL,"
                + "  AlbumId  bigint NOT NULL,"
                + "  Title    varchar(1024) NOT NULL,"
                + "  PRIMARY KEY (SingerId, AlbumId)"
                + ") INTERLEAVE IN PARENT Singers ON DELETE CASCADE");
        statement.executeBatch();
        System.out.println("Created Singers and Albums tables");
      }
    }
  }
}