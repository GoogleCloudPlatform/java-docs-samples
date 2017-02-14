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

package com.example.spanner;

// [START transaction_import]
import static com.google.cloud.spanner.TransactionRunner.TransactionCallable;
// [END transaction_import]

import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
// [START transaction_import]
import com.google.cloud.spanner.Key;
// [END transaction_import]
// [START read_import]
import com.google.cloud.spanner.KeySet;
// [END read_import]
// [START write_import]
import com.google.cloud.spanner.Mutation;
// [END write_import]
import com.google.cloud.spanner.Operation;
// [START read_only_transaction_import]
import com.google.cloud.spanner.ReadOnlyTransaction;
// [END read_only_transaction_import]
// [START query_import]
import com.google.cloud.spanner.ResultSet;
// [END query_import]
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
// [START query_import]
import com.google.cloud.spanner.Statement;
// [END query_import]
// [START transaction_import]
import com.google.cloud.spanner.Struct;
import com.google.cloud.spanner.TransactionContext;
// [END transaction_import]
import com.google.spanner.admin.database.v1.CreateDatabaseMetadata;

// [START write_import]

import java.util.ArrayList;
// [END write_import]
import java.util.Arrays;
import java.util.List;

/**
 * Example code for using the Cloud Spanner API. This example demonstrates all the common
 * operations that can be done on Cloud Spanner. These are: <p>
 * <ul>
 * <li> Creating a Cloud Spanner database.
 * <li> Writing, reading and executing SQL queries.
 * <li> Writing data using a read-write transaction.
 * <li> Using an index to read and execute SQL queries over data.
 * </ul>
 *
 */
public class SpannerSample {
  /** Class to contain singer sample data. */
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

  /** Class to contain album sample data. */
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

  // [START write]
  static final List<Singer> SINGERS =
      Arrays.asList(
          new Singer(1, "Marc", "Richards"),
          new Singer(2, "Catalina", "Smith"),
          new Singer(3, "Alice", "Trentor"),
          new Singer(4, "Lea", "Martin"),
          new Singer(5, "David", "Lomond"));

  static final List<Album> ALBUMS =
      Arrays.asList(
          new Album(1, 1, "Total Junk"),
          new Album(1, 2, "Go, Go, Go"),
          new Album(2, 1, "Green"),
          new Album(2, 2, "Forever Hold Your Peace"),
          new Album(2, 3, "Terrified"));
  // [END write]

  static void createDatabase(DatabaseAdminClient dbAdminClient, DatabaseId id) {
    Operation<Database, CreateDatabaseMetadata> op = dbAdminClient
            .createDatabase(
                id.getInstanceId().getInstance(),
                id.getDatabase(),
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
                        + "  INTERLEAVE IN PARENT Singers ON DELETE CASCADE"));
    Database db = op.waitFor().getResult();
    System.out.println("Created database [" + db.getId() + "]");
  }

  // [START write]
  static void writeExampleData(DatabaseClient dbClient) {
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
    dbClient.write(mutations);
  }
  // [END write]

  // [START query]
  static void query(DatabaseClient dbClient) {
    // singleUse() can be used to execute a single read or query against Cloud Spanner.
    ResultSet resultSet =
        dbClient
            .singleUse()
            .executeQuery(Statement.of("SELECT SingerId, AlbumId, AlbumTitle FROM Albums"));
    while (resultSet.next()) {
      System.out.printf(
          "%d %d %s\n", resultSet.getLong(0), resultSet.getLong(1), resultSet.getString(2));
    }
  }
  // [END query]

  // [START read]
  static void read(DatabaseClient dbClient) {
    ResultSet resultSet =
        dbClient
            .singleUse()
            .read("Albums",
                // KeySet.all() can be used to read all rows in a table. KeySet exposes other
                // methods to read only a subset of the table.
                KeySet.all(),
                Arrays.asList("SingerId", "AlbumId", "AlbumTitle"));
    while (resultSet.next()) {
      System.out.printf(
          "%d %d %s\n", resultSet.getLong(0), resultSet.getLong(1), resultSet.getString(2));
    }
  }
  // [END read]

  // [START add_marketing_budget]
  static void addMarketingBudget(DatabaseAdminClient adminClient, DatabaseId dbId) {
    adminClient.updateDatabaseDdl(dbId.getInstanceId().getInstance(),
        dbId.getDatabase(),
        Arrays.asList("ALTER TABLE Albums ADD COLUMN MarketingBudget INT64"),
        null).waitFor();
    System.out.println("Added MarketingBudget column");
  }
  // [END add_marketing_budget]

  // Before executing this method, a new column MarketingBudget has to be added to the Albums
  // table by applying the DDL statement "ALTER TABLE Albums ADD COLUMN MarketingBudget INT64".
  // [START update]
  static void update(DatabaseClient dbClient) {
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
    dbClient.write(mutations);
  }
  // [END update]

  // [START transaction]
  static void writeWithTransaction(DatabaseClient dbClient) {
    dbClient
        .readWriteTransaction()
        .run(
            new TransactionCallable<Void>() {
              @Override
              public Void run(TransactionContext transaction) throws Exception {
                // Transfer marketing budget from one album to another. We do it in a transaction to
                // ensure that the transfer is atomic.
                Struct row =
                    transaction.readRow("Albums", Key.of(2, 2), Arrays.asList("MarketingBudget"));
                long album2Budget = row.getLong(0);
                // Transaction will only be committed if this condition still holds at the time of
                // commit. Otherwise it will be aborted and the callable will be rerun by the
                // client library.
                if (album2Budget >= 300000) {
                  long album1Budget =
                      transaction
                          .readRow("Albums", Key.of(1, 1), Arrays.asList("MarketingBudget"))
                          .getLong(0);
                  long transfer = 200000;
                  album1Budget += transfer;
                  album2Budget -= transfer;
                  transaction.buffer(
                      Mutation.newUpdateBuilder("Albums")
                          .set("SingerId")
                          .to(1)
                          .set("AlbumId")
                          .to(1)
                          .set("MarketingBudget")
                          .to(album1Budget)
                          .build());
                  transaction.buffer(
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
              }
            });
  }
  // [END transaction]

  // [START query_new_column]
  static void queryMarketingBudget(DatabaseClient dbClient) {
    // Rows without an explicit value for MarketingBudget will have a MarketingBudget equal to
    // null.
    ResultSet resultSet =
        dbClient
            .singleUse()
            .executeQuery(Statement.of("SELECT SingerId, AlbumId, MarketingBudget FROM Albums"));
    while (resultSet.next()) {
      System.out.printf(
          "%d %d %s\n",
          resultSet.getLong("SingerId"),
          resultSet.getLong("AlbumId"),
          // We check that the value is non null. ResultSet getters can only be used to retrieve
          // non null values.
          resultSet.isNull("MarketingBudget") ? "NULL" : resultSet.getLong("MarketingBudget"));
    }
  }
  // [END query_new_column]

  // [START add_index]
  static void addIndex(DatabaseAdminClient adminClient, DatabaseId dbId) {
    adminClient.updateDatabaseDdl(dbId.getInstanceId().getInstance(),
        dbId.getDatabase(),
        Arrays.asList("CREATE INDEX AlbumsByAlbumTitle ON Albums(AlbumTitle)"),
        null).waitFor();
    System.out.println("Added AlbumsByAlbumTitle index");
  }
  // [END add_index]

  // Before running this example, add the index AlbumsByAlbumTitle by applying the DDL statement
  // "CREATE INDEX AlbumsByAlbumTitle ON Albums(AlbumTitle)".
  // [START query_index]
  static void queryUsingIndex(DatabaseClient dbClient) {
    ResultSet resultSet =
        dbClient
            .singleUse()
            .executeQuery(
                // We use FORCE_INDEX hint to specify which index to use. For more details see
                // https://cloud.google.com/spanner/docs/query-syntax#from-clause
                Statement.of(
                    "SELECT AlbumId, AlbumTitle, MarketingBudget\n"
                        + "FROM Albums@{FORCE_INDEX=AlbumsByAlbumTitle}\n"
                        + "WHERE AlbumTitle >= 'Aardvark' AND AlbumTitle < 'Goo'"));
    while (resultSet.next()) {
      System.out.printf(
          "%d %s %s\n",
          resultSet.getLong("AlbumId"),
          resultSet.getString("AlbumTitle"),
          resultSet.isNull("MarketingBudget") ? "NULL" : resultSet.getLong("MarketingBudget"));
    }
  }
  // [END query_index]

  // [START read_index]
  static void readUsingIndex(DatabaseClient dbClient) {
    ResultSet resultSet =
        dbClient
            .singleUse()
            .readUsingIndex(
                "Albums",
                "AlbumsByAlbumTitle",
                KeySet.all(),
                Arrays.asList("AlbumId", "AlbumTitle"));
    while (resultSet.next()) {
      System.out.printf("%d %s\n", resultSet.getLong(0), resultSet.getString(1));
    }
  }
  // [END read_index]

  // [START add_storing_index]
  static void addStoringIndex(DatabaseAdminClient adminClient, DatabaseId dbId) {
    adminClient.updateDatabaseDdl(dbId.getInstanceId().getInstance(),
        dbId.getDatabase(),
        Arrays.asList(
            "CREATE INDEX AlbumsByAlbumTitle2 ON Albums(AlbumTitle) STORING (MarketingBudget)"),
        null).waitFor();
    System.out.println("Added AlbumsByAlbumTitle2 index");
  }
  // [END add_storing_index]

  // Before running this example, create a storing index AlbumsByAlbumTitle2 by applying the DDL
  // statement "CREATE INDEX AlbumsByAlbumTitle2 ON Albums(AlbumTitle) STORING (MarketingBudget)".
  // [START read_storing_index]
  static void readStoringIndex(DatabaseClient dbClient) {
    // We can read MarketingBudget also from the index since it stores a copy of MarketingBudget.
    ResultSet resultSet =
        dbClient
            .singleUse()
            .readUsingIndex(
                "Albums",
                "AlbumsByAlbumTitle2",
                KeySet.all(),
                Arrays.asList("AlbumId", "AlbumTitle", "MarketingBudget"));
    while (resultSet.next()) {
      System.out.printf(
          "%d %s %s\n",
          resultSet.getLong(0),
          resultSet.getString(1),
          resultSet.isNull("MarketingBudget") ? "NULL" : resultSet.getLong("MarketingBudget"));
    }
  }
  // [END read_storing_index]

  // [START read_only_transaction]
  static void readOnlyTransaction(DatabaseClient dbClient) {
    // ReadOnlyTransaction must be closed by calling close() on it to release resources held by it.
    // We use a try-with-resource block to automatically do so.
    try (ReadOnlyTransaction transaction = dbClient.readOnlyTransaction()) {
      ResultSet queryResultSet =
          transaction.executeQuery(
              Statement.of("SELECT SingerId, AlbumId, AlbumTitle FROM Albums"));
      while (queryResultSet.next()) {
        System.out.printf(
            "%d %d %s\n",
            queryResultSet.getLong(0), queryResultSet.getLong(1), queryResultSet.getString(2));
      }
      ResultSet readResultSet =
          transaction.read(
              "Albums", KeySet.all(), Arrays.asList("SingerId", "AlbumId", "AlbumTitle"));
      while (readResultSet.next()) {
        System.out.printf(
            "%d %d %s\n",
            readResultSet.getLong(0), readResultSet.getLong(1), readResultSet.getString(2));
      }
    }
  }
  // [END read_only_transaction]

  static void run(DatabaseClient dbClient, DatabaseAdminClient dbAdminClient, String command,
      DatabaseId database) {
    switch (command) {
      case "createdatabase":
        createDatabase(dbAdminClient, database);
        break;
      case "write":
        writeExampleData(dbClient);
        break;
      case "query":
        query(dbClient);
        break;
      case "read":
        read(dbClient);
        break;
      case "addmarketingbudget":
        addMarketingBudget(dbAdminClient, database);
        break;
      case "update":
        update(dbClient);
        break;
      case "writetransaction":
        writeWithTransaction(dbClient);
        break;
      case "querymarketingbudget":
        queryMarketingBudget(dbClient);
        break;
      case "addindex":
        addIndex(dbAdminClient, database);
        break;
      case "readindex":
        readUsingIndex(dbClient);
        break;
      case "queryindex":
        queryUsingIndex(dbClient);
        break;
      case "addstoringindex":
        addStoringIndex(dbAdminClient, database);
        break;
      case "readstoringindex":
        readStoringIndex(dbClient);
        break;
      case "readonlytransaction":
        readOnlyTransaction(dbClient);
        break;
      default:
        printUsageAndExit();
    }
  }

  static void printUsageAndExit() {
    System.err.println("Usage:");
    System.err.println("    SpannerExample <command> <instance_id> <database_id>");
    System.err.println("");
    System.err.println("Examples:");
    System.err.println(
        "    SpannerExample createdatabase my-instance example-db");
    System.err.println(
        "    SpannerExample write my-instance example-db");
    System.exit(1);
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      printUsageAndExit();
    }
    // [START init_client]
    SpannerOptions options = SpannerOptions.newBuilder().build();
    Spanner spanner = options.getService();
    try {
      String command = args[0];
      DatabaseId db = DatabaseId.of(options.getProjectId(), args[1], args[2]);
      // [END init_client]
      // This will return the default project id based on the environment.
      String clientProject = spanner.getOptions().getProjectId();
      if (!db.getInstanceId().getProject().equals(clientProject)) {
        System.err.println("Invalid project specified. Project in the database id should match"
            + "the project name set in the environment variable GCLOUD_PROJECT. Expected: "
            + clientProject);
        printUsageAndExit();
      }
      // [START init_client]
      DatabaseClient dbClient = spanner.getDatabaseClient(db);
      DatabaseAdminClient dbAdminClient = spanner.getDatabaseAdminClient();
      // [END init_client]
      run(dbClient, dbAdminClient, command, db);
    } finally {
      spanner.closeAsync().get();
    }
    System.out.println("Closed client");
  }
}
