/*
 * Copyright 2019 Google LLC
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

package com.google.codelabs;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.SpannerExceptionFactory;
import com.google.cloud.spanner.SpannerOptions;
import com.google.spanner.admin.database.v1.CreateDatabaseMetadata;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * Example code for using the Cloud Spanner API with the Google Cloud Java client library
 * to create a simple leaderboard.
 * 
 * This example demonstrates:
 *
 * <p>
 *
 * <ul>
 *   <li>Creating a Cloud Spanner database.
 * </ul>
 */
public class App {

  static void create(DatabaseAdminClient dbAdminClient, DatabaseId db) {
    OperationFuture<Database, CreateDatabaseMetadata> op =
        dbAdminClient.createDatabase(
            db.getInstanceId().getInstance(),
            db.getDatabase(),
            Arrays.asList(
                "CREATE TABLE Players(\n"
                    + "  PlayerId INT64 NOT NULL,\n"
                    + "  PlayerName STRING(2048) NOT NULL\n"
                    + ") PRIMARY KEY(PlayerId)",
                "CREATE TABLE Scores(\n"
                    + "  PlayerId INT64 NOT NULL,\n"
                    + "  Score INT64 NOT NULL,\n"
                    + "  Timestamp TIMESTAMP NOT NULL\n"
                    + "  OPTIONS(allow_commit_timestamp=true)\n"
                    + ") PRIMARY KEY(PlayerId, Timestamp),\n"
                    + "INTERLEAVE IN PARENT Players ON DELETE NO ACTION"));
    try {
      // Initiate the request which returns an OperationFuture.
      Database dbOperation = op.get();
      System.out.println("Created database [" + dbOperation.getId() + "]");
    } catch (ExecutionException e) {
      // If the operation failed during execution, expose the cause.
      throw (SpannerException) e.getCause();
    } catch (InterruptedException e) {
      // Throw when a thread is waiting, sleeping, or otherwise occupied,
      // and the thread is interrupted, either before or during the activity.
      throw SpannerExceptionFactory.propagateInterrupt(e);
    }
  }

  static void printUsageAndExit() {
    System.out.println("Leaderboard 1.0.0");
    System.out.println("Usage:");
    System.out.println("  java -jar leaderboard.jar "
        + "<command> <instance_id> <database_id> [command_option]");
    System.out.println("");
    System.out.println("Examples:");
    System.out.println("  java -jar leaderboard.jar create my-instance example-db");
    System.out.println("      - Create a sample Cloud Spanner database along with "
        + "sample tables in your project.\n");
    System.exit(1);
  }

  public static void main(String[] args) throws Exception {
    if (!(args.length == 3 || args.length == 4)) {
      printUsageAndExit();
    }
    SpannerOptions options = SpannerOptions.newBuilder().build();
    Spanner spanner = options.getService();
    try {
      String command = args[0];
      DatabaseId db = DatabaseId.of(options.getProjectId(), args[1], args[2]);
      DatabaseClient dbClient = spanner.getDatabaseClient(db);
      DatabaseAdminClient dbAdminClient = spanner.getDatabaseAdminClient();
      switch (command) {
        case "create":
          create(dbAdminClient, db);
          break;
        default:
          printUsageAndExit();
      }
    } finally {
      spanner.close();
    }
    System.out.println("Closed client");
  }
}
