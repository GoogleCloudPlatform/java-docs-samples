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

import com.google.cloud.spanner.jdbc.CloudSpannerJdbcConnection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

class ReadWriteTransactionExample {

  static void readWriteTransaction() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    readWriteTransaction(projectId, instanceId, databaseId);
  }

  @SuppressFBWarnings(
      value = "OBL_UNSATISFIED_OBLIGATION",
      justification = "https://github.com/spotbugs/spotbugs/issues/293")
  static void readWriteTransaction(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl);
        Statement statement = connection.createStatement()) {
      // Explicitly begin a transaction. If the connection is in autocommit mode, this will
      // create a temporary transaction. Otherwise, this is a no-op.
      statement.execute("BEGIN TRANSACTION");
      // This statement will set this transaction to be a read/write transaction, regardless of
      // the read/write / read-only state of the connection.
      statement.execute("SET TRANSACTION READ WRITE");
      statement.execute(
          "INSERT INTO Singers (SingerId, FirstName, LastName, Revenues)\n"
              + "VALUES (17, 'Aqib', 'Currie', 9812.10)");
      statement.execute(
          "INSERT INTO Singers (SingerId, FirstName, LastName, Revenues)\n"
              + "VALUES (18, 'Chaya', 'Best', 38800)");
      statement.execute(
          "INSERT INTO Singers (SingerId, FirstName, LastName, Revenues)\n"
              + "VALUES (19, 'Om', 'Marks', 99999.99)");
      connection.commit();
      Timestamp commitTimestamp =
          connection.unwrap(CloudSpannerJdbcConnection.class).getCommitTimestamp();
      System.out.printf(
          "Transaction committed with commit timestamp [%s]%n", commitTimestamp.toString());
    }
  }
}
