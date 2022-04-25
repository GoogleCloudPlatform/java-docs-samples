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
import java.sql.SQLException;

class PgPartitionedDmlSample {

  static void pgPartitionedDml() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    pgPartitionedDml(projectId, instanceId, databaseId);
  }

  static void pgPartitionedDml(String projectId, String instanceId, String databaseId)
      throws SQLException {
    try (Connection connection =
        DriverManager.getConnection(
            String.format(
                "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
                projectId, instanceId, databaseId))) {
      // Spanner PostgreSQL has the same transaction limits as normal Spanner. This includes a
      // maximum of 20,000 mutations in a single read/write transaction. Large update operations can
      // be executed using Partitioned DML. This is also supported on Spanner PostgreSQL.
      // See https://cloud.google.com/spanner/docs/dml-partitioned for more information.

      // Switch to Partitioned DML. Note that we must prefix all Spanner specific session statements
      // with `SPANNER.`.
      connection.createStatement()
          .execute("SET SPANNER.AUTOCOMMIT_DML_MODE='PARTITIONED_NON_ATOMIC'");
      // Execute the DML statement.
      int deletedCount = connection.createStatement().executeUpdate("DELETE FROM Singers");
      // The returned update count is the lower bound of the number of records that was deleted.
      System.out.printf("Deleted at least %d singers\n", deletedCount);
    }
  }
}