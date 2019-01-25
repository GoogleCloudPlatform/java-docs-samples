/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appengine.spanner;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Operation;
import com.google.cloud.spanner.ReadOnlyTransaction;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.Struct;
import com.google.common.base.Stopwatch;
import com.google.spanner.admin.database.v1.UpdateDatabaseDdlMetadata;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

class SpannerTasks {

  enum Task {
    createDatabase,
    writeExampleData,
    query,
    read,
    addMarketingBudget,
    updateMarketingBudget,
    queryMarketingBudget,
    addIndex,
    readUsingIndex,
    queryUsingIndex,
    addStoringIndex,
    readStoringIndex,
    writeTransaction,
    readOnlyTransaction
  }

  /**
   * Class to contain singer sample data.
   */
  static class Singer {

    final long singerId;
    final String firstName;
    final String lastName;

    Singer(long singerId, String firstName, String lastName) {
      this.singerId = singerId;
      this.firstName = firstName;
      this.lastName = lastName;
    }
  }

  /**
   * Class to contain album sample data.
   */
  static class Album {

    final long singerId;
    final long albumId;
    final String albumTitle;

    Album(long singerId, long albumId, String albumTitle) {
      this.singerId = singerId;
      this.albumId = albumId;
      this.albumTitle = albumTitle;
    }
  }

  private static final List<Singer> SINGERS =
      Arrays.asList(
          new Singer(1, "Marc", "Richards"),
          new Singer(2, "Catalina", "Smith"),
          new Singer(3, "Alice", "Trentor"),
          new Singer(4, "Lea", "Martin"),
          new Singer(5, "David", "Lomond"));

  private static final List<Album> ALBUMS =
      Arrays.asList(
          new Album(1, 1, "Total Junk"),
          new Album(1, 2, "Go, Go, Go"),
          new Album(2, 1, "Green"),
          new Album(2, 2, "Forever Hold Your Peace"),
          new Album(2, 3, "Terrified"));

  private static DatabaseClient databaseClient = null;

  private static void createDatabase(PrintWriter pw) throws InterruptedException,
      ExecutionException {
    Iterable<String> statements =
        Arrays.asList(
            "CREATE TABLE Singers (\n"
                + "  SingerId   INT64 NOT NULL,\n"
                + "  FirstName  STRING(1024),\n"
                + "  LastName   STRING(1024),\n"
                + "  SingerInfo BYTES(MAX)\n"
                + ") PRIMARY KEY (SingerId)",
            "CREATE TABLE Albums (\n"
                + "  SingerId     INT64 NOT NULL,\n"
                + "  AlbumId      INT64 NOT NULL,\n"
                + "  AlbumTitle   STRING(MAX)\n"
                + ") PRIMARY KEY (SingerId, AlbumId),\n"
                + "  INTERLEAVE IN PARENT Singers ON DELETE CASCADE");
    Database db =
        SpannerClient.getDatabaseAdminClient()
            .createDatabase(
                SpannerClient.getInstanceId(), SpannerClient.getDatabaseId(), statements)
            .get();
    pw.println("Created database [" + db.getId() + "]");
  }

  private static void writeExampleData(PrintWriter pw) {
    List<Mutation> mutations = new ArrayList<>();
    for (Singer singer : SINGERS) {
      mutations.add(
          Mutation.newInsertBuilder("Singers")
              .set("SingerId")
              .to(singer.singerId)
              .set("FirstName")
              .to(singer.firstName)
              .set("LastName")
              .to(singer.lastName)
              .build());
    }
    for (Album album : ALBUMS) {
      mutations.add(
          Mutation.newInsertBuilder("Albums")
              .set("SingerId")
              .to(album.singerId)
              .set("AlbumId")
              .to(album.albumId)
              .set("AlbumTitle")
              .to(album.albumTitle)
              .build());
    }
    SpannerClient.getDatabaseClient().write(mutations);
  }

  private static void query(PrintWriter pw) {
    // singleUse() can be used to execute a single read or query against Cloud Spanner.
    ResultSet resultSet =
        SpannerClient.getDatabaseClient()
            .singleUse()
            .executeQuery(Statement.of("SELECT SingerId, AlbumId, AlbumTitle FROM Albums"));
    while (resultSet.next()) {
      pw.printf("%d %d %s\n", resultSet.getLong(0), resultSet.getLong(1), resultSet.getString(2));
    }
  }

  private static void read(PrintWriter pw) {
    ResultSet resultSet =
        SpannerClient.getDatabaseClient()
            .singleUse()
            .read(
                "Albums",
                // KeySet.all() can be used to read all rows in a table. KeySet exposes other
                // methods to read only a subset of the table.
                KeySet.all(),
                Arrays.asList("SingerId", "AlbumId", "AlbumTitle"));
    while (resultSet.next()) {
      pw.printf("%d %d %s\n", resultSet.getLong(0), resultSet.getLong(1), resultSet.getString(2));
    }
  }

  private static void addMarketingBudgetColumnToAlbums(PrintWriter pw) throws ExecutionException,
      InterruptedException {
    SpannerClient.getDatabaseAdminClient()
        .updateDatabaseDdl(
            SpannerClient.getInstanceId(),
            SpannerClient.getDatabaseId(),
            Collections.singletonList("ALTER TABLE Albums ADD COLUMN MarketingBudget INT64"),
            null)
        .get();
  }

  // Before executing this method, a new column MarketingBudget has to be added to the Albums
  // table by applying the DDL statement "ALTER TABLE Albums ADD COLUMN MarketingBudget INT64".
  private static void updateMarketingBudgetData() {
    // Mutation can be used to update/insert/delete a single row in a table. Here we use
    // newUpdateBuilder to create update mutations.
    List<Mutation> mutations =
        Arrays.asList(
            Mutation.newUpdateBuilder("Albums")
                .set("SingerId")
                .to(1)
                .set("AlbumId")
                .to(1)
                .set("MarketingBudget")
                .to(100000)
                .build(),
            Mutation.newUpdateBuilder("Albums")
                .set("SingerId")
                .to(2)
                .set("AlbumId")
                .to(2)
                .set("MarketingBudget")
                .to(500000)
                .build());
    // This writes all the mutations to Cloud Spanner atomically.
    SpannerClient.getDatabaseClient().write(mutations);
  }

  private static void writeWithTransaction() {
    SpannerClient.getDatabaseClient()
        .readWriteTransaction()
        .run(
            (transactionContext -> {
              // Transfer marketing budget from one album to another. We do it in a transaction to
              // ensure that the transfer is atomic.
              Struct row =
                  transactionContext.readRow(
                      "Albums", Key.of(2, 2), Arrays.asList("MarketingBudget"));
              long album2Budget = row.getLong(0);
              // Transaction will only be committed if this condition still holds at the time of
              // commit. Otherwise it will be aborted and the callable will be rerun by the
              // client library.
              if (album2Budget >= 300000) {
                long album1Budget =
                    transactionContext
                        .readRow("Albums", Key.of(1, 1), Arrays.asList("MarketingBudget"))
                        .getLong(0);
                long transfer = 200000;
                album1Budget += transfer;
                album2Budget -= transfer;
                transactionContext.buffer(
                    Mutation.newUpdateBuilder("Albums")
                        .set("SingerId")
                        .to(1)
                        .set("AlbumId")
                        .to(1)
                        .set("MarketingBudget")
                        .to(album1Budget)
                        .build());
                transactionContext.buffer(
                    Mutation.newUpdateBuilder("Albums")
                        .set("SingerId")
                        .to(2)
                        .set("AlbumId")
                        .to(2)
                        .set("MarketingBudget")
                        .to(album2Budget)
                        .build());
              }
              return null;
            }));
  }

  private static void queryMarketingBudget(PrintWriter pw) {
    // Rows without an explicit value for MarketingBudget will have a MarketingBudget equal to
    // null.
    ResultSet resultSet =
        SpannerClient.getDatabaseClient()
            .singleUse()
            .executeQuery(Statement.of("SELECT SingerId, AlbumId, MarketingBudget FROM Albums"));
    while (resultSet.next()) {
      pw.printf(
          "%d %d %s\n",
          resultSet.getLong("SingerId"),
          resultSet.getLong("AlbumId"),
          // We check that the value is non null. ResultSet getters can only be used to retrieve
          // non null values.
          resultSet.isNull("MarketingBudget") ? "NULL" : resultSet.getLong("MarketingBudget"));
    }
  }

  private static void addIndex() throws ExecutionException, InterruptedException {
    SpannerClient.getDatabaseAdminClient()
        .updateDatabaseDdl(
            SpannerClient.getInstanceId(),
            SpannerClient.getDatabaseId(),
            Arrays.asList("CREATE INDEX AlbumsByAlbumTitle ON Albums(AlbumTitle)"),
            null)
        .get();
  }

  // Before running this example, add the index AlbumsByAlbumTitle by applying the DDL statement
  // "CREATE INDEX AlbumsByAlbumTitle ON Albums(AlbumTitle)".
  private static void queryUsingIndex(PrintWriter pw) {
    ResultSet resultSet =
        SpannerClient.getDatabaseClient()
            .singleUse()
            .executeQuery(
                // We use FORCE_INDEX hint to specify which index to use. For more details see
                // https://cloud.google.com/spanner/docs/query-syntax#from-clause
                Statement.of(
                    "SELECT AlbumId, AlbumTitle, MarketingBudget\n"
                        + "FROM Albums@{FORCE_INDEX=AlbumsByAlbumTitle}\n"
                        + "WHERE AlbumTitle >= 'Aardvark' AND AlbumTitle < 'Goo'"));
    while (resultSet.next()) {
      pw.printf(
          "%d %s %s\n",
          resultSet.getLong("AlbumId"),
          resultSet.getString("AlbumTitle"),
          resultSet.isNull("MarketingBudget") ? "NULL" : resultSet.getLong("MarketingBudget"));
    }
  }

  private static void readUsingIndex(PrintWriter pw) {
    ResultSet resultSet =
        SpannerClient.getDatabaseClient()
            .singleUse()
            .readUsingIndex(
                "Albums",
                "AlbumsByAlbumTitle",
                KeySet.all(),
                Arrays.asList("AlbumId", "AlbumTitle"));
    while (resultSet.next()) {
      pw.printf("%d %s\n", resultSet.getLong(0), resultSet.getString(1));
    }
  }

  private static void addStoringIndex() throws ExecutionException, InterruptedException {
    SpannerClient.getDatabaseAdminClient()
        .updateDatabaseDdl(
            SpannerClient.getInstanceId(),
            SpannerClient.getDatabaseId(),
            Arrays.asList(
                "CREATE INDEX AlbumsByAlbumTitle2 "
                    + "ON Albums(AlbumTitle) STORING (MarketingBudget)"),
            null)
        .get();
  }

  // Before running this example, create a storing index AlbumsByAlbumTitle2 by applying the DDL
  // statement "CREATE INDEX AlbumsByAlbumTitle2 ON Albums(AlbumTitle) STORING (MarketingBudget)".
  private static void readStoringIndex(PrintWriter pw) {
    // We can read MarketingBudget also from the index since it stores a copy of MarketingBudget.
    ResultSet resultSet =
        SpannerClient.getDatabaseClient()
            .singleUse()
            .readUsingIndex(
                "Albums",
                "AlbumsByAlbumTitle2",
                KeySet.all(),
                Arrays.asList("AlbumId", "AlbumTitle", "MarketingBudget"));
    while (resultSet.next()) {
      pw.printf(
          "%d %s %s\n",
          resultSet.getLong(0),
          resultSet.getString(1),
          resultSet.isNull("MarketingBudget") ? "NULL" : resultSet.getLong("MarketingBudget"));
    }
  }

  private static void readOnlyTransaction(PrintWriter pw) {
    // ReadOnlyTransaction must be closed by calling close() on it to release resources held by it.
    // We use a try-with-resource block to automatically do so.
    try (ReadOnlyTransaction transaction =
             SpannerClient.getDatabaseClient().readOnlyTransaction()) {
      ResultSet queryResultSet =
          transaction.executeQuery(
              Statement.of("SELECT SingerId, AlbumId, AlbumTitle FROM Albums"));
      while (queryResultSet.next()) {
        pw.printf(
            "%d %d %s\n",
            queryResultSet.getLong(0), queryResultSet.getLong(1), queryResultSet.getString(2));
      }
      ResultSet readResultSet =
          transaction.read(
              "Albums", KeySet.all(), Arrays.asList("SingerId", "AlbumId", "AlbumTitle"));
      while (readResultSet.next()) {
        pw.printf(
            "%d %d %s\n",
            readResultSet.getLong(0), readResultSet.getLong(1), readResultSet.getString(2));
      }
    }
  }

  static void runTask(Task task, PrintWriter pw) throws ExecutionException, InterruptedException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    switch (task) {
      case createDatabase:
        createDatabase(pw);
        break;
      case writeExampleData:
        writeExampleData(pw);
        break;
      case query:
        query(pw);
        break;
      case read:
        read(pw);
        break;
      case addMarketingBudget:
        addMarketingBudgetColumnToAlbums(pw);
        break;
      case updateMarketingBudget:
        updateMarketingBudgetData();
        break;
      case queryMarketingBudget:
        queryMarketingBudget(pw);
        break;
      case addIndex:
        addIndex();
        break;
      case readUsingIndex:
        readUsingIndex(pw);
        break;
      case queryUsingIndex:
        queryUsingIndex(pw);
        break;
      case addStoringIndex:
        addStoringIndex();
        break;
      case readStoringIndex:
        readStoringIndex(pw);
        break;
      case readOnlyTransaction:
        readOnlyTransaction(pw);
        break;
      case writeTransaction:
        writeWithTransaction();
        break;
      default:
        break;
    }
    stopwatch.stop();
    pw.println(task + " in milliseconds : " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
    pw.println("====================================================================");
  }
}
