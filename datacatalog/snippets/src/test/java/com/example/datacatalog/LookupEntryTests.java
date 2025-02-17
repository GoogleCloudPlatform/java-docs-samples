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

package com.example.datacatalog;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Ignore("TODO: remove after fixing https://github.com/GoogleCloudPlatform/java-docs-samples/issues/9244")
public class LookupEntryTests {
  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  private static final String BIGQUERY_PROJECT = "bigquery-public-data";
  private static final String BIGQUERY_DATASET = "new_york_taxi_trips";
  private static final String BIGQUERY_TABLE = "taxi_zone_geom";

  private static final String PUBSUB_PROJECT = "pubsub-public-data";
  private static final String PUBSUB_TOPIC = "taxirides-realtime";

  private ByteArrayOutputStream bout;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() throws IOException {
    bout.close();
    System.setOut(null);
  }

  @Ignore
  @Test
  public void testLookupEntryBigQueryDataset() {
    LookupEntryBigQueryDataset.lookupEntry(BIGQUERY_PROJECT, BIGQUERY_DATASET);
    String got = bout.toString();
    assertThat(got).containsMatch(
        "projects/" + BIGQUERY_PROJECT + "/locations/.+?/entryGroups/@bigquery/entries/.+?$");
  }

  @Ignore
  @Test
  public void testLookupEntryBigQueryTable() {
    LookupEntryBigQueryTable.lookupEntry(BIGQUERY_PROJECT, BIGQUERY_DATASET, BIGQUERY_TABLE);
    String got = bout.toString();
    assertThat(got).containsMatch(
        "projects/" + BIGQUERY_PROJECT + "/locations/.+?/entryGroups/@bigquery/entries/.+?$");
  }

  @Ignore
  @Test
  public void testLookupPubSubTopic() {
    LookupEntryPubSubTopic.lookupEntry(PUBSUB_PROJECT, PUBSUB_TOPIC);
    String got = bout.toString();
    assertThat(got).containsMatch(
        "projects/" + PUBSUB_PROJECT + "/locations/.+?/entryGroups/@pubsub/entries/.+?$");
  }
}
