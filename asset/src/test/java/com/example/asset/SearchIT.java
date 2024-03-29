/*
 * Copyright 2020 Google LLC
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

package com.example.asset;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.DatasetDeleteOption;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.testing.RemoteBigQueryHelper;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for search samples. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class SearchIT {

  private static final String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String datasetName = RemoteBigQueryHelper.generateDatasetName();
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;
  private BigQuery bigquery;

  @Before
  public void setUp() {
    bigquery = BigQueryOptions.getDefaultInstance().getService();
    if (bigquery.getDataset(datasetName) == null) {
      bigquery.create(DatasetInfo.newBuilder(datasetName).build());
    }
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown() {
    // restores print statements in the original method
    System.out.flush();
    System.setOut(originalPrintStream);
    DatasetId datasetId = DatasetId.of(bigquery.getOptions().getProjectId(), datasetName);
    bigquery.delete(datasetId, DatasetDeleteOption.deleteContents());
  }

  @Ignore("Blocked on https://github.com/GoogleCloudPlatform/java-docs-samples/issues/8798")
  @Test
  public void testSearchAllResourcesExample() throws Exception {
    // Wait 120 seconds to let dataset creation event go to CAI
    TimeUnit.SECONDS.sleep(120);
    String scope = "projects/" + projectId;
    String query = "name:" + datasetName;
    SearchAllResourcesExample.searchAllResources(scope, query);
    String got = bout.toString();
    assertThat(got).contains(datasetName);
  }

  @Test
  public void testSearchAllIamPoliciesExample() throws Exception {
    TimeUnit.SECONDS.sleep(60);
    String scope = "projects/" + projectId;
    String query = "policy:roles/owner";
    SearchAllIamPoliciesExample.searchAllIamPolicies(scope, query);
    String got = bout.toString();
    assertThat(got).contains("roles/owner");
  }
}
