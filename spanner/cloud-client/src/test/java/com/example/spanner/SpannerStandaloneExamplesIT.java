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
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Instance;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration tests for Cloud Spanner cloud client examples. */
@RunWith(JUnit4.class)
public class SpannerStandaloneExamplesIT {
  // The instance needs to exist for tests to pass.
  private static String instanceId = System.getProperty("spanner.test.instance");
  private static String databaseId =
      formatForTest(System.getProperty("spanner.sample.database", "mysample"));
  private static DatabaseId dbId;
  private static DatabaseAdminClient dbClient;
  private static Spanner spanner;

  private String runExample(Runnable example) {
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
        SpannerOptions.newBuilder()
            .setHost("https://staging-wrenchworks.sandbox.googleapis.com")
            .build();
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
                    + ") PRIMARY KEY (SingerId)"))
        .get();
  }

  @AfterClass
  public static void dropTestDatabase() throws Exception {
    dbClient.dropDatabase(dbId.getInstanceId().getInstance(), dbId.getDatabase());
    spanner.close();
  }

  @Test
  public void executeSqlWithCustomTimeoutAndRetrySettings_shouldWriteData() {
    String projectId = spanner.getOptions().getProjectId();
    String out =
        runExample(
            () ->
                CustomTimeoutAndRetrySettingsExample.executeSqlWithCustomTimeoutAndRetrySettings(
                    projectId, instanceId, databaseId));
    assertThat(out).contains("1 record inserted.");
  }

  @Test
  public void numericDataType_shouldCreateTableAndInsertData() {
    String projectId = spanner.getOptions().getProjectId();
    String out =
        runExample(
            () ->
                NumericDataTypeExample.numericDataType(
                    spanner, DatabaseId.of(projectId, instanceId, databaseId)));
    assertThat(out).contains("Created SingerRevenues table.");
    assertThat(out).contains("1 2020 148143.18");
    assertThat(out).contains("3 2020 101002");
    assertThat(out).contains("Total revenues: 1 168443.18");
    assertThat(out).contains("Total revenues: 2 87003.81");
    assertThat(out).contains("Total revenues: 3 199764.44");
    assertThat(out).contains("Total revenues: 4 139608.41");
    assertThat(out).contains("Total revenues: 5 20111.75");
  }

  static String formatForTest(String name) {
    return name + "-" + UUID.randomUUID().toString().substring(0, 20);
  }
}
