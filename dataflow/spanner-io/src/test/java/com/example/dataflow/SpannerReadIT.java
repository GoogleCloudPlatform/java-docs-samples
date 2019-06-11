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
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Operation;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.TransactionContext;
import com.google.cloud.spanner.TransactionRunner;
import com.google.spanner.admin.database.v1.CreateDatabaseMetadata;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class SpannerReadIT {

  private String instanceId;
  private String databaseId;

  private Spanner spanner;
  private SpannerOptions spannerOptions;

  @Before
  public void setUp() {
    instanceId = System.getProperty("spanner.test.instance");
    databaseId = "df-spanner-read-it";

    spannerOptions = SpannerOptions.getDefaultInstance();
    spanner = spannerOptions.getService();

    DatabaseAdminClient adminClient = spanner.getDatabaseAdminClient();

    try {
      adminClient.dropDatabase(instanceId, databaseId);
    } catch (SpannerException e) {
      // Does not exist, ignore.
    }

    Operation<Database, CreateDatabaseMetadata> op = adminClient
        .createDatabase(instanceId, databaseId, Arrays.asList("CREATE TABLE Singers "
                + "(singerId INT64 NOT NULL, firstName STRING(MAX) NOT NULL, "
                + "lastName STRING(MAX) NOT NULL,) PRIMARY KEY (singerId)",
            "CREATE TABLE Albums (singerId INT64 NOT NULL, albumId INT64 NOT NULL, "
                + "albumTitle STRING(MAX) NOT NULL,) PRIMARY KEY (singerId, albumId)"));

    op.waitFor();

    List<Mutation> mutations = Arrays.asList(
        Mutation.newInsertBuilder("singers")
            .set("singerId").to(1L)
            .set("firstName").to("John")
            .set("lastName").to("Lennon")
            .build(),
        Mutation.newInsertBuilder("singers")
            .set("singerId").to(2L)
            .set("firstName").to("Paul")
            .set("lastName").to("Mccartney")
            .build(),
        Mutation.newInsertBuilder("singers")
            .set("singerId").to(3L)
            .set("firstName").to("George")
            .set("lastName").to("Harrison")
            .build(),
        Mutation.newInsertBuilder("singers")
            .set("singerId").to(4L)
            .set("firstName").to("Ringo")
            .set("lastName").to("Starr")
            .build(),

        Mutation.newInsertBuilder("albums")
            .set("singerId").to(1L)
            .set("albumId").to(1L)
            .set("albumTitle").to("Imagine")
            .build(),
        Mutation.newInsertBuilder("albums")
            .set("singerId").to(2L)
            .set("albumId").to(1L)
            .set("albumTitle").to("Pipes of Peace")
            .build()
    );


    DatabaseClient dbClient = getDbClient();

    TransactionRunner runner = dbClient.readWriteTransaction();
    runner.run(new TransactionRunner.TransactionCallable<Void>() {
      @Nullable
      @Override
      public Void run(TransactionContext tx) {
        tx.buffer(mutations);
        return null;
      }
    });
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
    SpannerReadAll.main(new String[] { "--instanceId=" + instanceId, "--databaseId=" + databaseId,
        "--output=" + outPath, "--runner=DirectRunner" });

    String content = Files.readAllLines(outPath).stream().collect(Collectors.joining("\n"));

    assertEquals("132", content);
  }

  @Test
  public void readTableEndToEnd() throws Exception {
    Path outPath = Files.createTempFile("out", "txt");
    SpannerRead.main(new String[] { "--instanceId=" + instanceId, "--databaseId=" + databaseId,
        "--output=" + outPath, "--table=albums", "--runner=DirectRunner" });

    String content = Files.readAllLines(outPath).stream().collect(Collectors.joining("\n"));

    assertEquals("53", content);
  }

  @Test
  public void readApiEndToEnd() throws Exception {
    Path outPath = Files.createTempFile("out", "txt");
    SpannerReadApi.main(new String[] { "--instanceId=" + instanceId, "--databaseId=" + databaseId,
        "--output=" + outPath, "--runner=DirectRunner" });

    String content = Files.readAllLines(outPath).stream().collect(Collectors.joining("\n"));

    assertEquals("79", content);
  }

  @Test
  public void reaTransactionalReadEndToEnd() throws Exception {
    Path singersPath = Files.createTempFile("singers", "txt");
    Path albumsPath = Files.createTempFile("albums", "txt");
    TransactionalRead.main(
        new String[] { "--instanceId=" + instanceId, "--databaseId=" + databaseId,
            "--singersFilename=" + singersPath, "--albumsFilename=" + albumsPath,
            "--runner=DirectRunner" });

    assertEquals(4, Files.readAllLines(singersPath).size());
    assertEquals(2, Files.readAllLines(albumsPath).size());
  }

  private DatabaseClient getDbClient() {
    return spanner
        .getDatabaseClient(DatabaseId.of(spannerOptions.getProjectId(), instanceId, databaseId));
  }

}