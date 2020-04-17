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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

class BatchDdlExample {

  static void batchDdl() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    batchDdl(projectId, instanceId, databaseId);
  }

  @SuppressFBWarnings(
      value = "OBL_UNSATISFIED_OBLIGATION",
      justification = "https://github.com/spotbugs/spotbugs/issues/293")
  static void batchDdl(String projectId, String instanceId, String databaseId) throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl);
        Statement statement = connection.createStatement()) {
      statement.addBatch(
          "CREATE TABLE Albums (\n"
              + "  SingerId        INT64 NOT NULL,\n"
              + "  AlbumId         INT64 NOT NULL,\n"
              + "  AlbumTitle      STRING(MAX),\n"
              + "  MarketingBudget INT64\n"
              + ") PRIMARY KEY(SingerId, AlbumId),\n"
              + "  INTERLEAVE IN PARENT Singers ON DELETE CASCADE");
      statement.addBatch(
          "CREATE TABLE Songs (\n"
              + "  SingerId  INT64 NOT NULL,\n"
              + "  AlbumId   INT64 NOT NULL,\n"
              + "  TrackId   INT64 NOT NULL,\n"
              + "  SongName  STRING(MAX),\n"
              + "  Duration  INT64,\n"
              + "  SongGenre STRING(25)\n"
              + ") PRIMARY KEY(SingerId, AlbumId, TrackId),\n"
              + "  INTERLEAVE IN PARENT Albums ON DELETE CASCADE");
      int[] updateCounts = statement.executeBatch();
      System.out.printf("DDL update counts: %s%n", Arrays.toString(updateCounts));
    }
  }
}
