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
import io.grpc.Status.Code;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class TransactionWithRetryLoopUsingOnlyJdbcExample {

  static void genericJdbcTransactionWithRetryLoop() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    genericJdbcTransactionWithRetryLoop(projectId, instanceId, databaseId);
  }

  @SuppressFBWarnings(value = "SIL_SQL_IN_LOOP")
  static void genericJdbcTransactionWithRetryLoop(
      String projectId, String instanceId, String databaseId) throws SQLException {
    // Create a connection that has automatic retry for aborted transactions disabled.
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s"
                + ";retryAbortsInternally=false",
            projectId, instanceId, databaseId);
    long singerId = 32;
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      while (true) {
        try {
          connection.setAutoCommit(false);
          try (PreparedStatement ps =
              connection.prepareStatement(
                  "INSERT INTO Singers (SingerId, FirstName, LastName, Revenues)\n"
                      + "VALUES (?, ?, ?, ?)")) {
            ps.setLong(1, singerId);
            ps.setString(2, "Marsha");
            ps.setString(3, "Roberts");
            ps.setBigDecimal(4, new BigDecimal("39148.01"));
            ps.executeUpdate();
          }
          connection.commit();
          try (Statement statement = connection.createStatement();
              ResultSet rs = statement.executeQuery("SHOW VARIABLE COMMIT_TIMESTAMP")) {
            if (rs.next()) {
              System.out.printf(
                  "Transaction committed at [%s]%n",
                  rs.getTimestamp("COMMIT_TIMESTAMP").toString());
            }
          }
          break;
        } catch (SQLException e) {
          // Rollback the current transaction to initiate a new transaction on the next statement.
          connection.rollback();
          if (e.getErrorCode() == Code.ABORTED.value()) {
            // Transaction aborted, retry.
            System.out.println("Transaction aborted, starting retry");
          } else {
            throw e;
          }
        }
      }
    }
  }
}
