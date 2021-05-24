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
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for load csv example.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class LoadCsvSampleIT {
  private String instanceId = System.getProperty("spanner.test.instance");
  private String databaseId = System.getProperty("spanner.sample.database");
  private String tableName = "example-table";

  static Spanner spanner;
  static DatabaseAdminClient dbClient;
  private ByteArrayOutputStream bout;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
    SpannerOptions options =
        SpannerOptions.newBuilder().setAutoThrottleAdministrativeRequests().build();
    spanner = options.getService();
    dbClient = spanner.getDatabaseAdminClient();
  }

  @After
  public void tearDown() {
    System.setOut(System.out);
  }

  @Test
  public void testLoadCSV() {
    assertThat(instanceId).isNotNull();
    assertThat(databaseId).isNotNull();
    assertThat(tableName).isNotNull();
  }

  @Test
  public void testNoHeader() throws Exception {
    String noHeaderPath = "src/test/resources/noHeader.csv";
    String[] testNoHeadersArgs = new String[] {
        instanceId, databaseId, tableName, noHeaderPath,
    };
    LoadCsvSample.main(testNoHeadersArgs);
    String out = bout.toString();
    assertThat(out).contains("Data successfully written into table.");
  }

  @Test
  public void testHeaderFail() throws Exception {
    String headerFailPath = "src/test/resources/headerFail.csv";
    String[] testHeadersFailArgs = new String[] {
        instanceId, databaseId, tableName, headerFailPath, "-h", "true",
    };
    LoadCsvSample.main(testHeadersFailArgs);
    String out = bout.toString();
    assertThat(out).contains("does not match any database table column name");
  }

  @Test
  public void testHeader() throws Exception {
    String headerPath = "src/test/resources/header.csv";
    String[] testHeadersArgs = new String[]{
        instanceId, databaseId, tableName, headerPath, "-h", "true",
    };
    LoadCsvSample.main(testHeadersArgs);
    String out = bout.toString();
    assertThat(out).contains("Data successfully written into table.");
  }

  @Test
  public void testSubsetHeader() throws Exception {
    String subsetHeaderPath = "src/test/resources/subsetHeader.csv";
    String[] testSubsetHeaderArgs = new String[]{
        instanceId, databaseId, tableName, subsetHeaderPath, "-h", "true",
    };
    LoadCsvSample.main(testSubsetHeaderArgs);
    String out = bout.toString();
    assertThat(out).contains("Data successfully written into table.");
  }

  @Test
  public void testDelimiterCharacter() throws Exception {
    String delimiterPath = "src/test/resources/delimiter.csv";
    String[] testDelimiterArgs = new String[]{
        instanceId, databaseId, tableName, delimiterPath, "-d", ";",
    };
    LoadCsvSample.main(testDelimiterArgs);
    String out = bout.toString();
    assertThat(out).contains("Data successfully written into table.");
  }

  @Test
  public void testEscapeCharacter() throws Exception {
    String escapePath = "src/test/resources/escape.csv";
    String[] testEscapeArgs = new String[]{
        instanceId, databaseId, tableName, escapePath, "-d", ";", "-e", ",",
    };
    LoadCsvSample.main(testEscapeArgs);
    String out = bout.toString();
    assertThat(out).contains("Data successfully written into table.");
  }

  @Test
  public void testNullString() throws Exception {
    String nullPath = "src/test/resources/null.csv";
    String[] testNullStringArgs = new String[] {
        instanceId, databaseId, tableName, nullPath, "-n", "nil",
    };
    LoadCsvSample.main(testNullStringArgs);
    String out = bout.toString();
    assertThat(out).contains("Data successfully written into table.");
  }

  @Test
  public void testNewsComments() throws Exception {
    String newsCommentsPath = "src/test/resources/hnewscomments.csv";
    String[] testNewsCommentsArgs = new String[] {
        instanceId, databaseId, tableName, newsCommentsPath, "-h", "false",
    };
    LoadCsvSample.main(testNewsCommentsArgs);
    String out = bout.toString();
    assertThat(out).contains("Data successfully written into table.");
  }

  @Test
  public void testNewsStories() throws Exception {
    String newsStoriesPath = "src/test/resources/hnewsstories.csv";
    String[] testNewsStoriesArgs = new String[] {
        instanceId, databaseId, tableName, newsStoriesPath, "-h", "false",
    };
    LoadCsvSample.main(testNewsStoriesArgs);
    String out = bout.toString();
    assertThat(out).contains("Data successfully written into table.");
  }
}
