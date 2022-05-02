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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

class PgInformationSchemaSample {

  static void pgInformationSchema() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    pgInformationSchema(projectId, instanceId, databaseId);
  }

  static void pgInformationSchema(String projectId, String instanceId, String databaseId)
      throws SQLException {
    // Create a JDBC connection to the database. A connection can be reused to execute multiple
    // statements. After completing all of your statements, call the "close" method on the
    // connection to safely clean up any remaining resources.
    try (Connection connection =
        DriverManager.getConnection(
            String.format(
                "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
                projectId, instanceId, databaseId))) {
      connection
          .createStatement()
          .execute(
              "CREATE TABLE Venues ("
                  + "  VenueId  bigint NOT NULL PRIMARY KEY,"
                  + "  Name     varchar(1024) NOT NULL,"
                  + "  Revenues numeric,"
                  + "  Picture  bytea"
                  + ")");

      // The Spanner INFORMATION_SCHEMA tables can be used to query the metadata of tables and
      // columns of PostgreSQL databases. The returned results will include additional PostgreSQL
      // metadata columns.

      // Get all the user tables in the database. PostgreSQL uses the `public` schema for user
      // tables.
      try (ResultSet tables =
          connection
              .createStatement()
              .executeQuery(
                  "SELECT table_catalog, table_schema, table_name, "
                      // The following columns are only available for PostgreSQL databases.
                      + "user_defined_type_catalog, "
                      + "user_defined_type_schema, "
                      + "user_defined_type_name "
                      + "FROM INFORMATION_SCHEMA.tables "
                      + "WHERE table_schema='public'")) {
        while (tables.next()) {
          String catalog = tables.getString("table_catalog");
          String schema = tables.getString("table_schema");
          String table = tables.getString("table_name");
          String userDefinedTypeCatalog = tables.getString("user_defined_type_catalog");
          String userDefinedTypeSchema = tables.getString("user_defined_type_schema");
          String userDefinedTypeName = tables.getString("user_defined_type_name");
          String userDefinedType =
              userDefinedTypeName == null
                  ? null
                  : String.format(
                      "%s.%s.%s",
                      userDefinedTypeCatalog, userDefinedTypeSchema, userDefinedTypeName);
          System.out.printf(
              "Table: %s.%s.%s (User defined type: %s)\n", catalog, schema, table, userDefinedType);
        }
      }

      // The java.sql.DatabaseMetaData of the JDBC connection can also be used to retrieve
      // information about tables, columns, indexes etc. These methods return the metadata as if it
      // was a normal Spanner database.
      try (ResultSet tables =
          connection.getMetaData().getTables(null, null, null, new String[] {"TABLE"})) {
        while (tables.next()) {
          // Catalog and schema are empty.
          String catalog = tables.getString("TABLE_CAT");
          String schema = tables.getString("TABLE_SCHEM");
          String table = tables.getString("TABLE_NAME");
          System.out.printf("Table in JDBC metadata: %s.%s.%s\n", catalog, schema, table);
        }
      }
    }
  }
}