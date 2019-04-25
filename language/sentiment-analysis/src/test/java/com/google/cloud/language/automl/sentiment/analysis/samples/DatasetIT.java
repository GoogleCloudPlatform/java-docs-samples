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

package com.google.cloud.language.automl.sentiment.analysis.samples;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for AutoML Natural Language Sentiment Analysis Dataset operations.
 */
@RunWith(JUnit4.class)
public class DatasetIT {

  private static final String PROJECT_ID = System.getenv().get("GOOGLE_CLOUD_PROJECT");
  private static final String EXPORT_PROJECT_ID = "java-docs-samples-testing-lcm";
  private static final String OUTPUT_PREFIX = "AUTOML_LANGUAGE_SENTIMENT_TEST_OUTPUT";
  private static final String COMPUTE_REGION = "us-central1";
  private static final String DATASET_NAME = "test_language_sentiment_dataset";
  private static final String FILTER = "textSentimentDatasetMetadata:*";
  private static final String DATASET_ID = "TST3960250460385409610";
  private static final String SENTIMENT_MAX = "4";
  private static final String IMPORT_DATA_CSV =
      "gs://" + PROJECT_ID + "-lcm/automl-sentiment/sentiment_dataset.csv";
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
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Act
    CreateDataset.createDataset(PROJECT_ID, COMPUTE_REGION, DATASET_NAME, SENTIMENT_MAX);

    // Assert
    String got = bout.toString();
    String datasetId =
        got.split("\n")[0].split("/")[(got.split("\n")[0]).split("/").length - 1].trim();
    assertThat(got).contains(datasetId);

    // Act
    bout.reset();
    ImportData.importData(PROJECT_ID, COMPUTE_REGION, datasetId, IMPORT_DATA_CSV);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Processing import...");

    // Act
    bout.reset();
    DeleteDataset.deleteDataset(PROJECT_ID, COMPUTE_REGION, datasetId);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Dataset deleted.");
  }

  @Test
  public void testListDatasets() throws IOException {
    // Act
    ListDatasets.listDatasets(PROJECT_ID, COMPUTE_REGION, FILTER);

    // Assert
    String got = bout.toString();
    assertThat(got).contains(DATASET_ID);
  }

  @Test
  public void testGetDataset() throws IOException {
    // Act
    GetDataset.getDataset(PROJECT_ID, COMPUTE_REGION, DATASET_ID);

    // Assert
    String got = bout.toString();
    assertThat(got).contains(DATASET_ID);
  }

  @Test
  public void testExportDataset() throws IOException, InterruptedException, ExecutionException {
    String outputUri = String.format("gs://%s/%s/%s", EXPORT_PROJECT_ID, OUTPUT_PREFIX, DATASET_ID);

    // Act
    ExportData.exportData(PROJECT_ID, COMPUTE_REGION, DATASET_ID, outputUri);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Processing export...");

    Storage storage = StorageOptions.getDefaultInstance().getService();

    Page<Blob> blobs = storage.list(EXPORT_PROJECT_ID, BlobListOption.currentDirectory(),
        BlobListOption.prefix(OUTPUT_PREFIX + "/"));

    deleteDirectory(storage, blobs);
  }

  private void deleteDirectory(Storage storage, Page<Blob> blobs) {
    for (Blob blob : blobs.iterateAll()) {
      System.out.println(blob.getName());
      if (!blob.delete()) {
        Page<Blob> subBlobs = storage.list(EXPORT_PROJECT_ID, BlobListOption.currentDirectory(),
            BlobListOption.prefix(blob.getName()));

        deleteDirectory(storage, subBlobs);
      }
    }
  }
}
