/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vtwo;


import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import vtwo.bigquery.CreateBigQueryExport;

@RunWith(JUnit4.class)
public class BigQueryExportIT {

  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);

  // TODO(Developer): Replace the below variables.
  private static final String ORGANIZATION_ID = System.getenv("SCC_PROJECT_ORG_ID");
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION = "global";
  private static final String BQ_DATASET_NAME =
      "sampledataset_" + UUID.randomUUID().toString().split("-")[0];
  private static final String BQ_EXPORT_ID =
      "default-" + UUID.randomUUID().toString().split("-")[0];

  private static ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void setUp() throws IOException {
    final PrintStream out = System.out;
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    // Create a BigQuery dataset.
    createBigQueryDataset(BQ_DATASET_NAME);
    // Create export request.
    String filter = "severity=\"LOW\" OR severity=\"MEDIUM\"";
    CreateBigQueryExport.createBigQueryExport(ORGANIZATION_ID, LOCATION,PROJECT_ID, filter, BQ_DATASET_NAME, BQ_EXPORT_ID);

    stdOut = null;
    System.setOut(out);
  }

  private static void createBigQueryDataset(String datasetName) {
    try {
      BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

      DatasetInfo datasetInfo = DatasetInfo.newBuilder(datasetName).build();

      Dataset newDataset = bigquery.create(datasetInfo);
      String newDatasetName = newDataset.getDatasetId().getDataset();
      System.out.println(newDatasetName + " created successfully");
    } catch (BigQueryException e) {
      if (e.toString().contains("Already Exists: Dataset")) {
        return;
      }
      Assert.fail("Dataset was not created. \n" + e);
    }
}
