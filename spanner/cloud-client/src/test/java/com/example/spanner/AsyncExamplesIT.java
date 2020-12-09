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

package com.example.spanner;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Instance;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration tests for Cloud Spanner Async API examples. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class AsyncExamplesIT {
  // The instance needs to exist for tests to pass.
  private static String instanceId = System.getProperty("spanner.test.instance");
  private static String databaseId =
      formatForTest(System.getProperty("spanner.sample.database", "mysample"));
  private static DatabaseId dbId;
  private static DatabaseAdminClient dbClient;
  private static Spanner spanner;

  private interface AsyncRunnable {
    public void run() throws InterruptedException, ExecutionException, TimeoutException;
  }

  private String runExample(AsyncRunnable example)
      throws InterruptedException, ExecutionException, TimeoutException {
    PrintStream stdOut = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
    example.run();
    System.setOut(stdOut);
    return bout.toString();
  }

  @BeforeClass
  public static void createTestDatabase() throws Exception {
    SpannerOptions options =
        SpannerOptions.newBuilder().setAutoThrottleAdministrativeRequests().build();
    spanner = options.getService();
    dbClient = spanner.getDatabaseAdminClient();
    if (instanceId == null) {
      Iterator<Instance> iterator =
          spanner.getInstanceAdminClient().listInstances().iterateAll().iterator();
      if (iterator.hasNext()) {
        instanceId = iterator.next().getId().getInstance();
      }
    }
    dbId = DatabaseId.of(options.getProjectId(), instanceId, databaseId);
    dbClient.dropDatabase(dbId.getInstanceId().getInstance(), dbId.getDatabase());
    dbClient
        .createDatabase(
            instanceId,
            databaseId,
            ImmutableList.of(
                "CREATE TABLE Singers ("
                    + "  SingerId   INT64 NOT NULL,"
                    + "  FirstName  STRING(1024),"
                    + "  LastName   STRING(1024),"
                    + "  SingerInfo BYTES(MAX)"
                    + ") PRIMARY KEY (SingerId)",
                "CREATE TABLE Albums ("
                    + "  SingerId        INT64 NOT NULL,"
                    + "  AlbumId         INT64 NOT NULL,"
                    + "  AlbumTitle      STRING(MAX),"
                    + "  MarketingBudget INT64"
                    + ") PRIMARY KEY (SingerId, AlbumId),"
                    + "  INTERLEAVE IN PARENT Singers ON DELETE CASCADE",
                "CREATE INDEX AlbumsByAlbumTitle ON Albums(AlbumTitle)"))
        .get();
  }

  @AfterClass
  public static void dropTestDatabase() throws Exception {
    dbClient.dropDatabase(dbId.getInstanceId().getInstance(), dbId.getDatabase());
    spanner.close();
  }

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

  static class Album {
    final long singerId;
    final long albumId;
    final String albumTitle;
    final Long marketingBudget;

    Album(long singerId, long albumId, String albumTitle, Long marketingBudget) {
      this.singerId = singerId;
      this.albumId = albumId;
      this.albumTitle = albumTitle;
      this.marketingBudget = marketingBudget;
    }
  }

  private static final List<Singer> TEST_SINGERS =
      Arrays.asList(
          new Singer(1, "Marc", "Richards"),
          new Singer(2, "Catalina", "Smith"),
          new Singer(3, "Alice", "Trentor"),
          new Singer(4, "Lea", "Martin"),
          new Singer(5, "David", "Lomond"));
  private static final List<Album> ALBUMS =
      Arrays.asList(
          new Album(1, 1, "Total Junk", 300_000L),
          new Album(1, 2, "Go, Go, Go", 400_000L),
          new Album(2, 1, "Green", 150_000L),
          new Album(2, 2, "Forever Hold Your Peace", 350_000L),
          new Album(2, 3, "Terrified", null));

  @Before
  public void insertTestData() {
    DatabaseClient client = spanner.getDatabaseClient(dbId);
    ImmutableList.Builder<Mutation> mutations =
        ImmutableList.builderWithExpectedSize(TEST_SINGERS.size());
    for (Singer singer : TEST_SINGERS) {
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
              .set("MarketingBudget")
              .to(album.marketingBudget)
              .build());
    }
    client.write(mutations.build());
  }

  private void assertSingersOutput(String out) {
    assertThat(out).contains("1 Marc Richard");
    assertThat(out).contains("2 Catalina Smith");
    assertThat(out).contains("3 Alice Trentor");
    assertThat(out).contains("4 Lea Martin");
    assertThat(out).contains("5 David Lomond");
  }

  private void assertAlbumsOutput(String out) {
    assertThat(out).contains("1 1 Total Junk");
    assertThat(out).contains("1 2 Go, Go, Go");
    assertThat(out).contains("2 1 Green");
    assertThat(out).contains("2 2 Forever Hold Your Peace");
    assertThat(out).contains("2 3 Terrified");
  }

  @After
  public void removeTestData() {
    DatabaseClient client = spanner.getDatabaseClient(dbId);
    client.write(Arrays.asList(Mutation.delete("Singers", KeySet.all())));
  }

  @Test
  public void asyncQuery_shouldReturnData()
      throws InterruptedException, ExecutionException, TimeoutException {
    String out = runExample(() -> AsyncQueryExample.asyncQuery(spanner.getDatabaseClient(dbId)));
    assertAlbumsOutput(out);
  }

  @Test
  public void asyncQueryToListAsync_shouldReturnData()
      throws InterruptedException, ExecutionException, TimeoutException {
    String out =
        runExample(
            () -> AsyncQueryToListAsyncExample.asyncQueryToList(spanner.getDatabaseClient(dbId)));
    assertAlbumsOutput(out);
  }

  @Test
  public void asyncRead_shouldReturnData()
      throws InterruptedException, ExecutionException, TimeoutException {
    String out = runExample(() -> AsyncReadExample.asyncRead(spanner.getDatabaseClient(dbId)));
    assertAlbumsOutput(out);
  }

  @Test
  public void asyncReadUsingIndex_shouldReturnDataInCorrectOrder()
      throws InterruptedException, ExecutionException, TimeoutException {
    String out =
        runExample(
            () -> AsyncReadUsingIndexExample.asyncReadUsingIndex(spanner.getDatabaseClient(dbId)));
    assertThat(out)
        .contains(
            "2 Forever Hold Your Peace\n"
                + "2 Go, Go, Go\n"
                + "1 Green\n"
                + "3 Terrified\n"
                + "1 Total Junk");
  }

  @Test
  public void asyncReadOnlyTransaction_shouldReturnData()
      throws InterruptedException, ExecutionException, TimeoutException {
    String out =
        runExample(
            () ->
                AsyncReadOnlyTransactionExample.asyncReadOnlyTransaction(
                    spanner.getDatabaseClient(dbId)));
    assertAlbumsOutput(out);
    assertSingersOutput(out);
  }

  @Test
  public void asyncDml_shouldInsertRows()
      throws InterruptedException, ExecutionException, TimeoutException {
    String out = runExample(() -> AsyncDmlExample.asyncDml(spanner.getDatabaseClient(dbId)));
    assertThat(out).contains("4 records inserted.");
  }

  @Test
  public void asyncRunner_shouldUpdateRows()
      throws InterruptedException, ExecutionException, TimeoutException {
    String out = runExample(() -> AsyncRunnerExample.asyncRunner(spanner.getDatabaseClient(dbId)));
    assertThat(out).contains("2 records updated.");
  }

  @Test
  public void asyncTransactionManager_shouldUpdateRows()
      throws InterruptedException, ExecutionException, TimeoutException {
    String out =
        runExample(
            () ->
                AsyncTransactionManagerExample.asyncTransactionManager(
                    spanner.getDatabaseClient(dbId)));
    assertThat(out).contains("2 records updated.");
  }

  @Test
  public void asyncReadRow_shouldPrintRow()
      throws InterruptedException, ExecutionException, TimeoutException {
    String out =
        runExample(() -> AsyncReadRowExample.asyncReadRow(spanner.getDatabaseClient(dbId)));
    assertThat(out).contains("1 1 Total Junk");
    assertThat(out).doesNotContain("1 2 Go, Go, Go");
    assertThat(out).doesNotContain("2 1 Green");
    assertThat(out).doesNotContain("2 2 Forever Hold Your Peace");
    assertThat(out).doesNotContain("2 3 Terrified");
  }

  static String formatForTest(String name) {
    return name + "-" + UUID.randomUUID().toString().substring(0, 20);
  }
}
