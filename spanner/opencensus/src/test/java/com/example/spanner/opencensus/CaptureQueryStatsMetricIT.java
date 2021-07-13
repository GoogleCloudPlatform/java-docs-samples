/*
 * Copyright 2021 Google Inc.
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

package com.example.spanner.opencensus;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Integration tests for Cloud Spanner OpenCensus Query Stats metric examples.
 */
@RunWith(JUnit4.class)
public class CaptureQueryStatsMetricIT {

  // The instance needs to exist for tests to pass.
  private static String instanceId = "default-instance";
  private static String databaseId = formatForTest(
      System.getProperty("spanner.sample.database", "mysample"));
  private static DatabaseId dbId;
  private static DatabaseAdminClient dbClient;
  private static Spanner spanner;
  private static PrintStream originalOut;
  private static ByteArrayOutputStream bout;

  @BeforeClass
  public static void createTestDatabase() throws Exception {
    Preconditions.checkState(instanceId != null, "No instance id set");
    final SpannerOptions options =
        SpannerOptions.newBuilder().setAutoThrottleAdministrativeRequests().build();
    spanner = options.getService();
    dbClient = spanner.getDatabaseAdminClient();
    dbId = DatabaseId.of(options.getProjectId(), instanceId, databaseId);
    dbClient.dropDatabase(dbId.getInstanceId().getInstance(), dbId.getDatabase());
    dbClient
        .createDatabase(
            instanceId,
            databaseId,
            ImmutableList.of(
                "CREATE TABLE Albums ("
                    + "  SingerId        INT64 NOT NULL,"
                    + "  AlbumId         INT64 NOT NULL,"
                    + "  AlbumTitle      STRING(MAX),"
                    + "  MarketingBudget INT64"
                    + ") PRIMARY KEY (AlbumId)"))
        .get();
  }

  @AfterClass
  public static void dropTestDatabase() {
    dbClient.dropDatabase(dbId.getInstanceId().getInstance(), dbId.getDatabase());
    spanner.close();
  }

  @Before
  public void captureOutput() {
    originalOut = System.out;
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void resetOutput() {
    System.setOut(originalOut);
    bout.reset();
  }

  @Before
  public void insertTestData() {
    final DatabaseClient client = spanner.getDatabaseClient(dbId);
    client.write(Arrays.asList(
        Mutation.newInsertBuilder("Albums")
            .set("SingerId")
            .to(1L)
            .set("AlbumId")
            .to(1L)
            .set("AlbumTitle")
            .to("Title 1")
            .build()
    ));
  }

  @After
  public void removeTestData() {
    final DatabaseClient client = spanner.getDatabaseClient(dbId);
    client.write(Collections.singletonList(Mutation.delete("Albums", KeySet.all())));
  }

  @Test
  public void testCaptureQueryStatsMetric() {
    final DatabaseClient client = spanner.getDatabaseClient(dbId);
    CaptureQueryStatsMetric.captureQueryStatsMetric(client);
    final String out = bout.toString();

    assertThat(out).contains("1 1 Title 1");
  }

  static String formatForTest(String name) {
    return name + "-" + UUID.randomUUID().toString().substring(0, 20);
  }
}
