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
import static org.junit.Assert.assertTrue;

import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Dialect;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ReadContext;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.TransactionContext;
import com.google.cloud.spanner.TransactionRunner;
import com.google.common.collect.ImmutableList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
public class SpannerGroupWriteIT {

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

  private Path tempPath;
  private Spanner spanner;
  private SpannerOptions spannerOptions;

  @Before
  public void setUp() throws Exception {
    instanceId = System.getProperty("spanner.test.instance");
    databaseId = "df-spanner-gwrite-it-" + random.nextInt(1000000000);

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
      adminClient.updateDatabaseDdl(
          instanceId,
          databaseId,
          Arrays.asList(
              "CREATE TABLE users "
                  + "(id varchar NOT NULL primary key, state varchar NOT NULL)",
              "CREATE TABLE PendingReviews (id bigint primary key, action varchar, "
                  + "note varchar, userId varchar)"),
          null).get();
    } else {
      adminClient
          .createDatabase(
              instanceId,
              databaseId,
              Arrays.asList(
                  "CREATE TABLE users ("
                      + "id STRING(MAX) NOT NULL, state STRING(MAX) NOT NULL) PRIMARY KEY (id)",
                  "CREATE TABLE PendingReviews (id INT64, action STRING(MAX), "
                      + "note STRING(MAX), userId STRING(MAX),) PRIMARY KEY (id)"))
          .get();
    }

    DatabaseClient dbClient = getDbClient();

    List<Mutation> mutations = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      mutations.add(
          Mutation.newInsertBuilder("users")
              .set("id")
              .to(Integer.toString(i))
              .set("state")
              .to("ACTIVE")
              .build());
    }
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

    String content =
        IntStream.range(0, 10).mapToObj(Integer::toString).collect(Collectors.joining("\n"));
    tempPath = Files.createTempFile("suspicious-ids", "txt");
    Files.write(tempPath, content.getBytes());
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
  public void testEndToEnd() {
    SpannerGroupWrite.main(
        new String[] {
          "--instanceId=" + instanceId,
          "--databaseId=" + databaseId,
          "--suspiciousUsersFile=" + tempPath,
          "--runner=DirectRunner",
          "--dialect=" + dialect
        });

    DatabaseClient dbClient = getDbClient();
    Statement countUsersStatement;
    if (dialect == Dialect.POSTGRESQL) {
      countUsersStatement =
          Statement.newBuilder("SELECT COUNT(*) FROM users WHERE STATE = $1")
              .bind("p1")
              .to("BLOCKED")
              .build();
    } else {
      countUsersStatement =
          Statement.newBuilder("SELECT COUNT(*) FROM users WHERE STATE = @state")
              .bind("state")
              .to("BLOCKED")
              .build();
    }
    try (ReadContext context = dbClient.singleUse()) {
      ResultSet rs = context.executeQuery(countUsersStatement);
      assertTrue(rs.next());
      assertEquals(10, rs.getLong(0));
    }
    Statement countPendingReviewsStatement;
    if (dialect == Dialect.POSTGRESQL) {
      countPendingReviewsStatement =
          Statement.newBuilder("SELECT COUNT(*) FROM PendingReviews WHERE ACTION = $1")
              .bind("p1")
              .to("REVIEW ACCOUNT")
              .build();
    } else {
      countPendingReviewsStatement =
          Statement.newBuilder("SELECT COUNT(*) FROM PendingReviews WHERE ACTION = @action")
              .bind("action")
              .to("REVIEW ACCOUNT")
              .build();
    }
    try (ReadContext context = dbClient.singleUse()) {
      ResultSet rs = context.executeQuery(countPendingReviewsStatement);
      assertTrue(rs.next());
      assertEquals(10, rs.getLong(0));
    }
  }

  private DatabaseClient getDbClient() {
    return spanner.getDatabaseClient(
        DatabaseId.of(spannerOptions.getProjectId(), instanceId, databaseId));
  }
}
