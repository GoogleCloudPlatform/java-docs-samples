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

class BatchDdlUsingSqlStatementsExample {

  static void batchDdlUsingSqlStatements() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    batchDdlUsingSqlStatements(projectId, instanceId, databaseId);
  }

  @SuppressFBWarnings(
      value = "OBL_UNSATISFIED_OBLIGATION",
      justification = "https://github.com/spotbugs/spotbugs/issues/293")
  static void batchDdlUsingSqlStatements(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl);
        Statement statement = connection.createStatement()) {
      // Start a DDL batch.
      statement.execute("START BATCH DDL");
      // Execute the DDL statements.
      statement.execute(
          "CREATE TABLE Concerts (\n"
              + "  VenueId      INT64 NOT NULL,\n"
              + "  SingerId     INT64 NOT NULL,\n"
              + "  ConcertDate  DATE NOT NULL,\n"
              + "  BeginTime    TIMESTAMP,\n"
              + "  EndTime      TIMESTAMP,\n"
              + "  TicketPrices ARRAY<INT64>,\n"
              + "  CONSTRAINT Fk_Concerts_Singer FOREIGN KEY (SingerId)\n"
              + "                                REFERENCES Singers (SingerId)\n"
              + ") PRIMARY KEY(VenueId, SingerId, ConcertDate)");
      // Update count for a DDL statement will always be JdbcConstants#STATEMENT_NO_RESULT.
      System.out.printf("Update count for CREATE TABLE Concerts: %d%n", statement.getUpdateCount());

      statement.execute("CREATE INDEX SingersByFirstLastName ON Singers(FirstName, LastName)");
      System.out.printf(
          "Update count for CREATE INDEX SingersByFirstLastName: %d%n", statement.getUpdateCount());

      // The 'RUN BATCH' statement will not return any values for a DDL batch.
      statement.execute("RUN BATCH");
      System.out.println("Executed DDL batch");
    }
  }
}
