/*
 * Copyright 2019 Google LLC
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

package com.google.cloud.language.automl.entity.extraction.samples;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for AutoML Natural Language Entity Extraction "Dataset API" sample. */
@RunWith(JUnit4.class)
public class DatasetApiIT {
  // TODO(developer): Change PROJECT_ID, COMPUTE_REGION, DATASET_ID and IMPORT_DATA_CSV before
  // running the test cases.
  private static final String PROJECT_ID = "java-docs-samples-testing";
  private static final String BUCKET = PROJECT_ID + "-entity";
  private static final String COMPUTE_REGION = "us-central1";
  private static final String DATASET_NAME = "test_language_dataset";
  private static final String FILTER = "textExtractionDatasetMetadata:*";
  private static final String DATASET_ID = "TEN8051890775971069952";
  private static final String IMPORT_DATA_CSV = "gs://cloud-ml-data/NL-entity/dataset.csv";
  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testCreateImportDeleteDataset()
      throws IOException, InterruptedException, ExecutionException {
    // Act
    DatasetApi.createDataset(PROJECT_ID, COMPUTE_REGION, DATASET_NAME);

    // Assert
    String got = bout.toString();
    String datasetId = got.split("\n")[0].split("/")[(got.split("\n")[0]).split("/").length - 1];
    assertThat(got).contains("Dataset name:");

    // Act
    bout.reset();
    DatasetApi.importData(PROJECT_ID, COMPUTE_REGION, datasetId, IMPORT_DATA_CSV);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Processing import...");

    // Act
    bout.reset();
    DatasetApi.deleteDataset(PROJECT_ID, COMPUTE_REGION, datasetId);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Dataset deleted.");
  }

  @Test
  public void testListDatasets() throws IOException {
    // Act
    DatasetApi.listDatasets(PROJECT_ID, COMPUTE_REGION, FILTER);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Dataset Id:");
  }

  @Test
  public void testGetDataset() throws IOException {
    // Act
    DatasetApi.getDataset(PROJECT_ID, COMPUTE_REGION, DATASET_ID);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Dataset Id:");
  }

  @Test
  public void testExportDataset() throws IOException, InterruptedException, ExecutionException {
    String outputURI = "gs://" + BUCKET + "/" + DATASET_ID;

    // Act
    DatasetApi.exportData(PROJECT_ID, COMPUTE_REGION, DATASET_ID, outputURI);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Processing export...");
  }
}
