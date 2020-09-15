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

import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.jdbc.CloudSpannerJdbcConnection;
import com.google.cloud.spanner.jdbc.JdbcSqlExceptionFactory.JdbcAbortedException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

class TransactionWithRetryLoopExample {

  static void transactionWithRetryLoop() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    transactionWithRetryLoop(projectId, instanceId, databaseId);
  }

  static void transactionWithRetryLoop(String projectId, String instanceId, String databaseId)
      throws SQLException {
    // Create a connection that has automatic retry for aborted transactions disabled.
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s"
                + ";retryAbortsInternally=false",
            projectId, instanceId, databaseId);
    long singerId = 31;
    long albumId = 11;
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      while (true) {
        try {
          CloudSpannerJdbcConnection spannerConnection =
              connection.unwrap(CloudSpannerJdbcConnection.class);
          spannerConnection.setAutoCommit(false);
          Mutation mutationSingers =
              Mutation.newInsertBuilder("Singers")
                  .set("SingerId")
                  .to(singerId)
                  .set("FirstName")
                  .to("Breanna")
                  .set("LastName")
                  .to("Fountain")
                  .set("Revenues")
                  .to(new BigDecimal("29809.93"))
                  .build();
          Mutation mutationAlbums =
              Mutation.newInsertBuilder("Albums")
                  .set("SingerId")
                  .to(singerId)
                  .set("AlbumId")
                  .to(albumId)
                  .set("AlbumTitle")
                  .to("No discounts")
                  .set("MarketingBudget")
                  .to(1000)
                  .build();
          spannerConnection.bufferedWrite(Arrays.asList(mutationSingers, mutationAlbums));
          spannerConnection.commit();
          System.out.printf(
              "Transaction committed at [%s]%n", spannerConnection.getCommitTimestamp().toString());
          break;
        } catch (JdbcAbortedException e) {
          // Rollback the current transaction to initiate a new transaction on the next statement.
          connection.rollback();
          // Transaction aborted, retry.
          System.out.println("Transaction aborted, starting retry");
        }
      }
    }
  }
}
