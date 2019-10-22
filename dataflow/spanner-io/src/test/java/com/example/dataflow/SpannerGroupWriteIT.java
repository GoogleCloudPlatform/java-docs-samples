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
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Operation;
import com.google.cloud.spanner.ReadContext;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.TransactionContext;
import com.google.cloud.spanner.TransactionRunner;
import com.google.spanner.admin.database.v1.CreateDatabaseMetadata;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class SpannerGroupWriteIT {

  private String instanceId;
  private String databaseId;

  private Path tempPath;
  private Spanner spanner;
  private SpannerOptions spannerOptions;

  @Before
  public void setUp() throws Exception {
    instanceId = System.getProperty("spanner.test.instance");
    databaseId = "df-spanner-groupwrite-it";

    spannerOptions = SpannerOptions.getDefaultInstance();
    spanner = spannerOptions.getService();

    DatabaseAdminClient adminClient = spanner.getDatabaseAdminClient();

    try {
      adminClient.dropDatabase(instanceId, databaseId);
    } catch (SpannerException e) {
      // Does not exist, ignore.
    }

    Operation<Database, CreateDatabaseMetadata> op = adminClient
        .createDatabase(instanceId, databaseId, Arrays.asList("CREATE TABLE users ("
                + "id STRING(MAX) NOT NULL, state STRING(MAX) NOT NULL) PRIMARY KEY (id)",
            "CREATE TABLE PendingReviews (id INT64, action STRING(MAX), "
                + "note STRING(MAX), userId STRING(MAX),) PRIMARY KEY (id)"));

    op.waitFor();

    DatabaseClient dbClient = getDbClient();

    List<Mutation> mutations = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      mutations.add(
          Mutation.newInsertBuilder("users").set("id").to(Integer.toString(i)).set("state")
              .to("ACTIVE").build());
    }
    TransactionRunner runner = dbClient.readWriteTransaction();
    runner.run(new TransactionRunner.TransactionCallable<Void>() {

      @Nullable
      @Override
      public Void run(TransactionContext tx) {
        tx.buffer(mutations);
        return null;
      }
    });

    String content = IntStream.range(0, 10).mapToObj(Integer::toString)
        .collect(Collectors.joining("\n"));
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
        new String[] { "--instanceId=" + instanceId, "--databaseId=" + databaseId,
            "--suspiciousUsersFile=" + tempPath, "--runner=DirectRunner" });

    DatabaseClient dbClient = getDbClient();
    try (ReadContext context = dbClient.singleUse()) {
      ResultSet rs = context.executeQuery(
          Statement.newBuilder("SELECT COUNT(*) FROM users WHERE STATE = @state").bind("state")
              .to("BLOCKED").build());
      assertTrue(rs.next());
      assertEquals(10, rs.getLong(0));

    }
    try (ReadContext context = dbClient.singleUse()) {
      ResultSet rs = context.executeQuery(
          Statement.newBuilder("SELECT COUNT(*) FROM PendingReviews WHERE ACTION = @action")
              .bind("action").to("REVIEW ACCOUNT").build());
      assertTrue(rs.next());
      assertEquals(10, rs.getLong(0));
    }
  }

  private DatabaseClient getDbClient() {
    return spanner
        .getDatabaseClient(DatabaseId.of(spannerOptions.getProjectId(), instanceId, databaseId));
  }

}
