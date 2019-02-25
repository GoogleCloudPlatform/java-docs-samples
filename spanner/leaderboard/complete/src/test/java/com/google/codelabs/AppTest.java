/*
 * Copyright 2019 Google Inc.
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

package com.google.codelabs;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@code Leaderboard}
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class AppTest {
  // The instance needs to exist for tests to pass.
  private final String instanceId = System.getProperty("spanner.test.instance");
  private final String databaseId = formatForTest(System.getProperty("spanner.sample.database"));
  DatabaseId dbId;
  DatabaseAdminClient dbClient;

  private String runSample(String command) throws Exception {
    PrintStream stdOut = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
    App.main(new String[]{command, instanceId, databaseId});
    System.setOut(stdOut);
    return bout.toString();
  }

  private String runSample(String command, String commandOption) throws Exception {
    PrintStream stdOut = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
    App.main(new String[]{command, instanceId, databaseId, commandOption});
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
    String out = runSample("create");
    assertThat(out).contains("Created database");
    assertThat(out).contains(dbId.getName());

    out = runSample("insert", "players");
    assertThat(out).contains("Done inserting player records");

    out = runSample("insert", "scores");
    assertThat(out).contains("Done inserting score records");

    // Query Top Ten Players of all time.
    out = runSample("query");
    assertThat(out).contains("PlayerId: ");
    // Confirm output includes valid timestamps.
    String columnText = "Timestamp: ";
    String[] lines = out.split("\\r?\\n");
    String valueToTest = lines[0].substring(lines[0].indexOf(columnText) + columnText.length());
    DateTimeFormatter formatPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate ld = LocalDate.parse(valueToTest, formatPattern);
    String result = ld.format(formatPattern);
    assertThat(result.equals(valueToTest)).isTrue();

    // Test that Top Ten Players of the Year (within past 8760 hours) runs successfully.
    out = runSample("query", "8760");
    assertThat(out).contains("PlayerId: ");

    // Test that Top Ten Players of the Month (within past 730 hours) runs successfully.
    out = runSample("query", "730");
    assertThat(out).contains("PlayerId: ");

    // Test that Top Ten Players of the Week (within past 168 hours) runs successfully.
    out = runSample("query", "168");
    assertThat(out).contains("PlayerId: ");


  }

  private String formatForTest(String name) {
    return name + "-" + UUID.randomUUID().toString().substring(0, 20);
  }
}
