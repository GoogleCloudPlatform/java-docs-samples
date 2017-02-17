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

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Unit tests for {@code SpannerSample}
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class SpannerSampleIT {
  // The instance needs to exist for tests to pass.
  private final String instanceId = System.getProperty("spanner.test.instance");
  private final String databaseId = System.getProperty("spanner.sample.database");
  DatabaseId dbId;
  DatabaseAdminClient dbClient;

  private String runSample(String command) throws Exception {
    PrintStream stdOut = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
    SpannerSample.main(new String[]{command, instanceId, databaseId});
    System.setOut(stdOut);
    return bout.toString();
  }

  @Before
  public void setUp() throws Exception {
    SpannerOptions options = SpannerOptions.newBuilder().build();
    Spanner spanner = options.getService();
    dbClient = spanner.getDatabaseAdminClient();
    dbId = DatabaseId.of(options.getProjectId(), instanceId, databaseId);
    dbClient.dropDatabase(dbId.getInstanceId().getInstance(), dbId.getDatabase());
  }

  @After
  public void tearDown() throws Exception {
    dbClient.dropDatabase(dbId.getInstanceId().getInstance(), dbId.getDatabase());
  }

  @Test
  public void testSample() throws Exception {
    assertThat(instanceId).isNotNull();
    assertThat(databaseId).isNotNull();
    String out = runSample("createdatabase");
    assertThat(out).contains("Created database");
    assertThat(out).contains(dbId.getName());

    runSample("write");

    out = runSample("read");
    assertThat(out).contains("1 1 Total Junk");

    out = runSample("query");
    assertThat(out).contains("1 1 Total Junk");

    runSample("addmarketingbudget");
    runSample("update");

    runSample("writetransaction");

    out = runSample("querymarketingbudget");
    assertThat(out).contains("1 1 300000");
    assertThat(out).contains("2 2 300000");

    runSample("addindex");
    out = runSample("queryindex");
    assertThat(out).contains("Go, Go, Go");
    assertThat(out).contains("Forever Hold Your Peace");
    assertThat(out).doesNotContain("Green");

    out = runSample("readindex");
    assertThat(out).contains("Go, Go, Go");
    assertThat(out).contains("Forever Hold Your Peace");
    assertThat(out).contains("Green");

    runSample("addstoringindex");
    out = runSample("readstoringindex");
    assertThat(out).contains("300000");

    out = runSample("readonlytransaction");
    assertThat(out.replaceAll("[\r\n]+", " ")).containsMatch("(Total Junk.*){2}");
  }
}
