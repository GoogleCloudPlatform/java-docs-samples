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

import static com.google.cloud.spanner.TransactionRunner.TransactionCallable;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.SpannerExceptionFactory;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.TransactionContext;
import com.google.spanner.admin.database.v1.CreateDatabaseMetadata;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Example code for using the Cloud Spanner API with the Google Cloud Java client library
 * to create a simple leaderboard.
 * 
 * This example demonstrates :
 *
 * <p>
 *
 * <ul>
 *   <li>Creating a Cloud Spanner database.
 *   <li>Inserting data using a read-write transaction.
 *   <li>Execute SQL queries over data, filtering and ordering by timestamp.
 *   <li>Deleting a Cloud Spanner database.
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

  static void insert(DatabaseClient dbClient, String insertType) {
    try {
      insertType = insertType.toLowerCase();
    } catch (Exception e) {
      // Invalid input received, set inserttype to empty string.
      insertType = "";
    }
    if (insertType.equals("players")) {
      // Insert players.
      insertPlayers(dbClient);
    } else if (insertType.equals("scores")) {
      // Insert scores.
      insertScores(dbClient);
    } else {
      // Invalid input.
      System.out.println("Invalid value for 'type of insert'. "
          + "Specify a valid value: 'players' or 'scores'.");
      System.exit(1);
    }
  }

  static void insertPlayers(DatabaseClient dbClient) {
    dbClient
        .readWriteTransaction()
        .run(
            new TransactionCallable<Void>() {
              @Override
              public Void run(TransactionContext transaction) throws Exception {
                // Get the number of players.
                String sql = "SELECT Count(PlayerId) as PlayerCount FROM Players";
                ResultSet resultSet = transaction.executeQuery(Statement.of(sql));
                long numberOfPlayers = 0;
                if (resultSet.next()) {
                  numberOfPlayers = resultSet.getLong("PlayerCount");
                }
                // Insert 100 player records into the Players table.
                long randomId;
                for (int x = 1; x <= 100; x++) {
                  numberOfPlayers++;
                  randomId = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
                  transaction.buffer(
                      Mutation.newInsertBuilder("Players")
                          .set("PlayerId")
                          .to(randomId)
                          .set("PlayerName")
                          .to("Player " + numberOfPlayers)
                          .build());
                }
                return null;
              }
            });
    System.out.println("Done inserting player records...");
  }

  static void insertScores(DatabaseClient dbClient) {
    boolean playerRecordsFound = false;
    ResultSet resultSet =
        dbClient
            .singleUse()
            .executeQuery(Statement.of("SELECT * FROM Players"));
    while (resultSet.next()) {
      playerRecordsFound = true;
      final long playerId = resultSet.getLong("PlayerId");
      dbClient
          .readWriteTransaction()
          .run(
              new TransactionCallable<Void>() {
                @Override
                public Void run(TransactionContext transaction) throws Exception {
                  // Initialize objects for random Score and random Timestamp
                  LocalDate endDate = LocalDate.now();
                  long end = endDate.toEpochDay();
                  int startYear = endDate.getYear() - 2;
                  int startMonth = endDate.getMonthValue();
                  int startDay = endDate.getDayOfMonth();
                  LocalDate startDate = LocalDate.of(startYear, startMonth, startDay);
                  long start = startDate.toEpochDay();
                  Random r = new Random();
                  // Insert 4 score records into the Scores table 
                  // for each player in the Players table.
                  for (int x = 1; x <= 4; x++) {
                    // Generate random score between 1,000,000 and 1,000
                    long randomScore = r.nextInt(1000000 - 1000) + 1000;
                    // Get random day within the past two years.
                    long randomDay = ThreadLocalRandom.current().nextLong(start, end);
                    LocalDate randomDayDate = LocalDate.ofEpochDay(randomDay);
                    LocalTime randomTime = LocalTime.of(
                        r.nextInt(23), r.nextInt(59), r.nextInt(59), r.nextInt(9999));
                    LocalDateTime randomDate = LocalDateTime.of(randomDayDate, randomTime);
                    Instant randomInstant = randomDate.toInstant(ZoneOffset.UTC);
                    transaction.buffer(
                        Mutation.newInsertBuilder("Scores")
                            .set("PlayerId")
                            .to(playerId)
                            .set("Score")
                            .to(randomScore)
                            .set("Timestamp")
                            .to(randomInstant.toString())
                            .build());
                  }
                  return null;
                }
              });

    }
    if (!playerRecordsFound) {
      System.out.println("Parameter 'scores' is invalid since "
          + "no player records currently exist. First insert players "
          + "then insert scores.");
      System.exit(1);
    } else {
      System.out.println("Done inserting score records...");
    }
  }

  static void query(DatabaseClient dbClient) {
    Statement statement = Statement.of(
        "SELECT p.PlayerId, p.PlayerName, s.Score, s.Timestamp "
          + "FROM Players p "
          + "JOIN Scores s ON p.PlayerId = s.PlayerId "
          + "ORDER BY s.Score DESC LIMIT 10");
    ResultSet resultSet = dbClient.singleUse().executeQuery(statement);
    while (resultSet.next()) {
      String scoreDate = String.valueOf(resultSet.getTimestamp("Timestamp"));
      String score = String.format("%,d", resultSet.getLong("Score"));
      System.out.printf(
          "PlayerId: %d  PlayerName: %s  Score: %s  Timestamp: %s\n",
          resultSet.getLong("PlayerId"), resultSet.getString("PlayerName"), score,
          scoreDate.substring(0,10));
    }
  }

  static void query(DatabaseClient dbClient, int timespan) {
    Statement statement =
        Statement
            .newBuilder(
              "SELECT p.PlayerId, p.PlayerName, s.Score, s.Timestamp "
              + "FROM Players p "
              + "JOIN Scores s ON p.PlayerId = s.PlayerId "
              + "WHERE s.Timestamp > "
              + "TIMESTAMP_SUB(CURRENT_TIMESTAMP(), "
              + "    INTERVAL @Timespan HOUR) "
              + "ORDER BY s.Score DESC LIMIT 10")
            .bind("Timespan")
            .to(timespan)
            .build();
    ResultSet resultSet = dbClient.singleUse().executeQuery(statement);
    while (resultSet.next()) {
      String scoreDate = String.valueOf(resultSet.getTimestamp("Timestamp"));
      String score = String.format("%,d", resultSet.getLong("Score"));
      System.out.printf(
          "PlayerId: %d  PlayerName: %s  Score: %s  Timestamp: %s\n",
          resultSet.getLong("PlayerId"), resultSet.getString("PlayerName"), score,
          scoreDate.substring(0,10));
    }
  }

  static void delete(DatabaseAdminClient dbAdminClient, DatabaseId db) {
    try  {
      dbAdminClient.dropDatabase(db.getInstanceId().getInstance(), db.getDatabase());
    } catch (Exception e) {
      System.err.println("Error encountered while deleting database. Error message: " + e);
    }
    System.out.printf("Database deleted.\n");
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
    System.out.println("  java -jar leaderboard.jar insert my-instance example-db players");
    System.out.println("      - Insert 100 sample Player records into the database.\n");
    System.out.println("  java -jar leaderboard.jar insert my-instance example-db scores");
    System.out.println("      - Insert sample score data into Scores sample Cloud Spanner "
        + "database table.\n");
    System.out.println("  java -jar leaderboard.jar query my-instance example-db");
    System.out.println("      - Query players with top ten scores of all time.\n");
    System.out.println("  java -jar leaderboard.jar query my-instance example-db 168");
    System.out.println("      - Query players with top ten scores within a timespan "
        + "specified in hours.\n");
    System.out.println("  java -jar leaderboard.jar delete my-instance example-db");
    System.out.println("      - Delete sample Cloud Spanner database.");
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
        case "insert":
          String insertType;
          try {
            insertType = args[3];
          } catch (ArrayIndexOutOfBoundsException exception) {
            insertType = "";
          }
          insert(dbClient, insertType);
          break;
        case "query":
          if (args.length == 4) {
            int timespan = 0;
            try {
              timespan = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
              System.err.println("query command's 'timespan' parameter must be a valid integer.");
              System.exit(1);
            }
            query(dbClient, timespan);
          } else {
            query(dbClient);
          }
          break;
        case "delete":
          delete(dbAdminClient, db);
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
