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

package com.google.cloud.automl.tables.samples;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for AutoML Tables "Dataset API" sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class DatasetApiIT {
  private static final String PROJECT_ID = "java-docs-samples-testing";
  private static final String COMPUTE_REGION = "us-central1";
  private static final String DATASET_NAME = "test_table_dataset";
  private static final String DATASET_ID = "TBL2017172828410871808";
  private static final String BIGQUERY_DATASET_ID = "TBL5314616996204118016";
  private static final String PATH = "gs://cloud-ml-tables-data/bank-marketing.csv";
  private static final String OUTPUT_GCS_URI = "gs://automl-tables/export-data";
  private static final String OUTPUT_BIGQUERY_URI = "bq://automl-tables-bg-output";
  private static final String UPDATE_DATASET_DISPLAY_NAME = "test_table_dataset_01";
  private static final String UPDATE_DATA_TYPE_CODE = "CATEGORY";
  private static final String FILTER = "tablesDatasetMetadata:*";
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
  public void testCreateImportUpdateDeleteDataset()
      throws IOException, InterruptedException, ExecutionException {
    // Act
    DatasetApi.createDataset(PROJECT_ID, COMPUTE_REGION, DATASET_NAME);

    // Assert
    String got = bout.toString();
    String datasetId = got.split("\n")[0].split("/")[(got.split("\n")[0]).split("/").length - 1];
    assertThat(got).contains("Dataset name:");

    // Act
    bout.reset();
    DatasetApi.importData(PROJECT_ID, COMPUTE_REGION, datasetId, PATH);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Processing import...");

    // Act
    bout.reset();
    DatasetApi.updateDataset(PROJECT_ID, COMPUTE_REGION, datasetId, UPDATE_DATASET_DISPLAY_NAME);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Dataset Id:");

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
  public void testGetDatasets() throws IOException {
    // Act
    DatasetApi.getDataset(PROJECT_ID, COMPUTE_REGION, DATASET_ID);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Dataset Id:");
  }

  @Test
  public void testListGetUpdateTableSpec() throws IOException {
    // Act
    DatasetApi.listTableSpecs(PROJECT_ID, COMPUTE_REGION, DATASET_ID, FILTER);

    // Assert
    String got = bout.toString();
    String tableId = got.split("\n")[1].split("/")[(got.split("\n")[1]).split("/").length - 1];
    assertThat(got).contains("Table Id:");

    // Act
    bout.reset();
    DatasetApi.getTableSpec(PROJECT_ID, COMPUTE_REGION, DATASET_ID, tableId);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Table Id:");

    // Act
    bout.reset();
    DatasetApi.listColumnSpecs(PROJECT_ID, COMPUTE_REGION, DATASET_ID, tableId, FILTER);

    // Assert
    got = bout.toString();
    String columnId = got.split("\n")[2].split("/")[(got.split("\n")[2]).split("/").length - 1];
    assertThat(got).contains("Column Id:");

    // Act
    bout.reset();
    DatasetApi.getColumnSpec(PROJECT_ID, COMPUTE_REGION, DATASET_ID, tableId, columnId);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Column Id:");

    // Act
    bout.reset();
    DatasetApi.updateColumnSpec(
        PROJECT_ID, COMPUTE_REGION, DATASET_ID, tableId, columnId, UPDATE_DATA_TYPE_CODE);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Column Id:");
  }

  //  @Test
  //  public void testExportData() throws IOException, InterruptedException, ExecutionException {
  //    // Act
  //    DatasetApi.exportDataToCsv(PROJECT_ID, COMPUTE_REGION, DATASET_ID, OUTPUT_GCS_URI);
  //
  //    // Assert
  //    String got = bout.toString();
  //    assertThat(got).contains("Processing export...");
  //
  //    Storage storage = StorageOptions.getDefaultInstance().getService();
  //    Page<Blob> blobs =
  //            storage.list(
  //                    BUCKET_ID,
  //                    Storage.BlobListOption.currentDirectory(),
  //                    Storage.BlobListOption.prefix("TEST_EXPORT_OUTPUT/"));
  //
  //    for (Blob blob : blobs.iterateAll()) {
  //      Page<Blob> fileBlobs =
  //              storage.list(
  //                      BUCKET_ID,
  //                      Storage.BlobListOption.currentDirectory(),
  //                      Storage.BlobListOption.prefix(blob.getName()));
  //      for (Blob fileBlob : fileBlobs.iterateAll()) {
  //        if (!fileBlob.isDirectory()) {
  //          fileBlob.delete();
  //        }
  //      }
  //    }
  //
  //    // Act
  //    bout.reset();
  //    DatasetApi.exportDataToBigQuery(
  //        PROJECT_ID, COMPUTE_REGION, BIGQUERY_DATASET_ID, OUTPUT_BIGQUERY_URI);
  //
  //    // Assert
  //    got = bout.toString();
  //    assertThat(got).contains("Processing export...");
  //  }
}
