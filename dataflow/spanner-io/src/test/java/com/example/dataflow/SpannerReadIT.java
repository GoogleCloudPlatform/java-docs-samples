/*
 * Copyright 2018 Google Inc.
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

package com.example.dataflow;

import static org.junit.Assert.assertEquals;

import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Dialect;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.TransactionContext;
import com.google.cloud.spanner.TransactionRunner;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@SuppressWarnings("checkstyle:abbreviationaswordinname")
@RunWith(Parameterized.class)
public class SpannerReadIT {

  @Parameter
  public Dialect dialect;

  @Parameters(name = "dialect = {0}")
  public static List<Object[]> data() {
    List<Object[]> parameters = new ArrayList<>();
    for (Dialect dialect : Dialect.values()) {
      parameters.add(new Object[] {dialect});
    }
    return parameters;
  }

  private final Random random = new Random();
  private String instanceId;
  private String databaseId;

  private Spanner spanner;
  private SpannerOptions spannerOptions;

  @Before
  public void setUp() throws InterruptedException, ExecutionException {
    instanceId = System.getProperty("spanner.test.instance");
    databaseId = "df-spanner-read-it-" + random.nextInt(1000000000);

    spannerOptions = SpannerOptions.getDefaultInstance();
    spanner = spannerOptions.getService();

    DatabaseAdminClient adminClient = spanner.getDatabaseAdminClient();

    try {
      adminClient.dropDatabase(instanceId, databaseId);
    } catch (SpannerException e) {
      // Does not exist, ignore.
    }

    if (dialect == Dialect.POSTGRESQL) {
      Database database =
          adminClient
              .newDatabaseBuilder(
                  DatabaseId.of(spannerOptions.getProjectId(), instanceId, databaseId))
              .setDialect(Dialect.POSTGRESQL)
              .build();
      adminClient.createDatabase(database, ImmutableList.of()).get();
      adminClient
          .updateDatabaseDdl(
              instanceId,
              databaseId,
              Arrays.asList(
                  "CREATE TABLE Singers "
                      + "(singer_id bigint NOT NULL primary key, first_name varchar NOT NULL, "
                      + "last_name varchar NOT NULL)",
                  "CREATE TABLE Albums (singer_id bigint NOT NULL, album_id bigint NOT NULL, "
                      + "album_title varchar NOT NULL, PRIMARY KEY (singer_id, album_id))"),
              null)
          .get();
    } else {
      adminClient
          .createDatabase(
              instanceId,
              databaseId,
              Arrays.asList(
                  "CREATE TABLE Singers "
                      + "(SingerId INT64 NOT NULL, FirstName STRING(MAX) NOT NULL, "
                      + "LastName STRING(MAX) NOT NULL,) PRIMARY KEY (SingerId)",
                  "CREATE TABLE Albums (SingerId INT64 NOT NULL, AlbumId INT64 NOT NULL, "
                      + "AlbumTitle STRING(MAX) NOT NULL,) PRIMARY KEY (SingerId, AlbumId)"))
          .get();
    }

    List<Mutation> mutations =
        Arrays.asList(
            Mutation.newInsertBuilder("Singers")
                .set(formatColumnName("SingerId", dialect))
                .to(1L)
                .set(formatColumnName("FirstName", dialect))
                .to("John")
                .set(formatColumnName("LastName", dialect))
                .to("Lennon")
                .build(),
            Mutation.newInsertBuilder("Singers")
                .set(formatColumnName("SingerId", dialect))
                .to(2L)
                .set(formatColumnName("FirstName", dialect))
                .to("Paul")
                .set(formatColumnName("LastName", dialect))
                .to("Mccartney")
                .build(),
            Mutation.newInsertBuilder("Singers")
                .set(formatColumnName("SingerId", dialect))
                .to(3L)
                .set(formatColumnName("FirstName", dialect))
                .to("George")
                .set(formatColumnName("LastName", dialect))
                .to("Harrison")
                .build(),
            Mutation.newInsertBuilder("Singers")
                .set(formatColumnName("SingerId", dialect))
                .to(4L)
                .set(formatColumnName("FirstName", dialect))
                .to("Ringo")
                .set(formatColumnName("LastName", dialect))
                .to("Starr")
                .build(),
            Mutation.newInsertBuilder("Albums")
                .set(formatColumnName("SingerId", dialect))
                .to(1L)
                .set(formatColumnName("AlbumId", dialect))
                .to(1L)
                .set(formatColumnName("AlbumTitle", dialect))
                .to("Imagine")
                .build(),
            Mutation.newInsertBuilder("Albums")
                .set(formatColumnName("SingerId", dialect))
                .to(2L)
                .set(formatColumnName("AlbumId", dialect))
                .to(1L)
                .set(formatColumnName("AlbumTitle", dialect))
                .to("Pipes of Peace")
                .build());

    DatabaseClient dbClient = getDbClient();

    TransactionRunner runner = dbClient.readWriteTransaction();
    runner.run(
        new TransactionRunner.TransactionCallable<Void>() {
          @Nullable
          @Override
          public Void run(TransactionContext tx) {
            tx.buffer(mutations);
            return null;
          }
        });
  }

  /**
   * Format the column name to use the idiomatic form for the given dialect. That is; Camel-case for
   * GoogleSQL and lower_underscore for PostgreSQL.
   */
  static String formatColumnName(String column, Dialect dialect) {
    return dialect == Dialect.POSTGRESQL
        ? CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column)
        : column;
  }

  @After
  public void tearDown() {
    DatabaseAdminClient adminClient = spanner.getDatabaseAdminClient();
    try {
      adminClient.dropDatabase(instanceId, databaseId);
    } catch (SpannerException e) {
      // Failed to cleanup.
    }

    spanner.close();
  }

  @Test
  public void readDbEndToEnd() throws Exception {
    Path outPath = Files.createTempFile("out", "txt");
    SpannerReadAll.main(
        new String[] {
          "--instanceId=" + instanceId,
          "--databaseId=" + databaseId,
          "--output=" + outPath,
          "--runner=DirectRunner",
          "--dialect=" + dialect
        });

    String content = Files.readAllLines(outPath).stream().collect(Collectors.joining("\n"));

    assertEquals("132", content);
  }

  @Test
  public void readTableEndToEnd() throws Exception {
    Path outPath = Files.createTempFile("out", "txt");
    SpannerRead.main(
        new String[] {
          "--instanceId=" + instanceId,
          "--databaseId=" + databaseId,
          "--output=" + outPath,
          "--table=albums",
          "--runner=DirectRunner"
        });

    String content = Files.readAllLines(outPath).stream().collect(Collectors.joining("\n"));

    assertEquals("53", content);
  }

  @Test
  public void readApiEndToEnd() throws Exception {
    Path outPath = Files.createTempFile("out", "txt");
    SpannerReadApi.main(
        new String[] {
          "--instanceId=" + instanceId,
          "--databaseId=" + databaseId,
          "--output=" + outPath,
          "--runner=DirectRunner",
          "--dialect=" + dialect
        });

    String content = Files.readAllLines(outPath).stream().collect(Collectors.joining("\n"));

    assertEquals("79", content);
  }

  @Test
  public void readTransactionalReadEndToEnd() throws Exception {
    Path singersPath = Files.createTempFile("singers", "txt");
    Path albumsPath = Files.createTempFile("albums", "txt");
    TransactionalRead.main(
        new String[] {
          "--instanceId=" + instanceId,
          "--databaseId=" + databaseId,
          "--singersFilename=" + singersPath,
          "--albumsFilename=" + albumsPath,
          "--runner=DirectRunner",
          "--dialect=" + dialect
        });

    assertEquals(4, Files.readAllLines(singersPath).size());
    assertEquals(2, Files.readAllLines(albumsPath).size());
  }

  private DatabaseClient getDbClient() {
    return spanner.getDatabaseClient(
        DatabaseId.of(spannerOptions.getProjectId(), instanceId, databaseId));
  }
}
