/*
 * Copyright 2017 Google Inc.
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
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@code SpannerSample}
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class SpannerSampleIT {
  // The instance needs to exist for tests to pass.
  private final String instanceId = System.getProperty("spanner.test.instance");
  private final String databaseId = formatForTest(System.getProperty("spanner.sample.database"));
  DatabaseId dbId;
  DatabaseAdminClient dbClient;
  private long lastUpdateDataTimeInMillis;

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

    out = runSample("delete");
    assertThat(out).contains("Records deleted.");

    runSample("write");

    out = runSample("read");
    assertThat(out).contains("1 1 Total Junk");

    out = runSample("query");
    assertThat(out).contains("1 1 Total Junk");
    runSample("addmarketingbudget");

    // wait for 15 seconds to elapse and then run an update, and query for stale data
    lastUpdateDataTimeInMillis = System.currentTimeMillis();
    while (System.currentTimeMillis() < lastUpdateDataTimeInMillis + 16000) {
      Thread.sleep(1000);
    }
    runSample("update");
    out = runSample("readstaledata");
    assertThat(out).contains("1 1 NULL");
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

    out = runSample("addcommittimestamp");
    assertThat(out).contains("Added LastUpdateTime as a commit timestamp column");

    runSample("updatewithtimestamp");
    out = runSample("querywithtimestamp");
    assertThat(out).contains("1 1 1000000");
    assertThat(out).contains("2 2 750000");

    out = runSample("createtablewithtimestamp");
    assertThat(out).contains("Created Performances table in database");

    runSample("writewithtimestamp");
    out = runSample("queryperformancestable");
    assertThat(out).contains("1 4 2017-10-05 11000");
    assertThat(out).contains("1 19 2017-11-02 15000");
    assertThat(out).contains("2 42 2017-12-23 7000");

    runSample("writestructdata");
    out = runSample("querywithstruct");
    assertThat(out).startsWith("6\n");

    out = runSample("querywitharrayofstruct");
    assertThat(out).startsWith("6\n7");

    out = runSample("querystructfield");
    assertThat(out).startsWith("6\n");

    out = runSample("querynestedstructfield");
    assertThat(out).contains("6 Imagination\n");
    assertThat(out).contains("9 Imagination\n");

    runSample("insertusingdml");
    out = runSample("querysingerstable");
    assertThat(out).contains("Virginia Watson");

    runSample("updateusingdml");
    out = runSample("querymarketingbudget");
    assertThat(out).contains("1 1 2000000");

    runSample("deleteusingdml");
    out = runSample("querysingerstable");
    assertThat(out).doesNotContain("Alice Trentor");

    out = runSample("updateusingdmlwithtimestamp");
    assertThat(out).contains("2 records updated");

    out = runSample("writeandreadusingdml");
    assertThat(out).contains("Timothy Campbell");

    runSample("updateusingdmlwithstruct");
    out = runSample("querysingerstable");
    assertThat(out).contains("Timothy Grant");

    runSample("writeusingdml");
    out = runSample("querysingerstable");
    assertThat(out).contains("Melissa Garcia");
    assertThat(out).contains("Russell Morales");
    assertThat(out).contains("Jacqueline Long");
    assertThat(out).contains("Dylan Shaw");

    runSample("writewithtransactionusingdml");
    out = runSample("querymarketingbudget");    
    assertThat(out).contains("1 1 1800000");
    assertThat(out).contains("2 2 200000");
    
    runSample("updateusingpartitioneddml");
    out = runSample("querymarketingbudget");
    assertThat(out).contains("1 1 1800000");
    assertThat(out).contains("2 2 100000");

    runSample("deleteusingpartitioneddml");
    out = runSample("querysingerstable");
    assertThat(out).doesNotContain("Timothy Grant");
    assertThat(out).doesNotContain("Melissa Garcia");
    assertThat(out).doesNotContain("Russell Morales");
    assertThat(out).doesNotContain("Jacqueline Long");
    assertThat(out).doesNotContain("Dylan Shaw");
  }

  private String formatForTest(String name) {
    return name + "-" + UUID.randomUUID().toString().substring(0, 20);
  }
}
