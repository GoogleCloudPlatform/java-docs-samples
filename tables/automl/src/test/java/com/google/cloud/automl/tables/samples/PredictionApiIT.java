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
import com.google.protobuf.Value;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for AutoML Tables "Prediction API" sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class PredictionApiIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String MODEL_ID = "TBL5997440105332080640";
  private static final String BQ_INPUT_URI = "bq://automl-tables-bg-input";
  private static final String BQ_OUTPUT_URI_PREFIX = "bq://automl-tables-bg-output";
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
  public void testPredict() throws IOException {
    List<Value> values = new ArrayList<>();
    values.add(Value.newBuilder().setNumberValue(39).build()); // Age
    values.add(Value.newBuilder().setStringValue("technician").build()); // Job
    values.add(Value.newBuilder().setStringValue("married").build()); // MaritalStatus
    values.add(Value.newBuilder().setStringValue("secondary").build()); // Education
    values.add(Value.newBuilder().setStringValue("no").build()); // Default
    values.add(Value.newBuilder().setNumberValue(52).build()); // Balance
    values.add(Value.newBuilder().setStringValue("no").build()); // Housing
    values.add(Value.newBuilder().setStringValue("no").build()); // Loan
    values.add(Value.newBuilder().setStringValue("cellular").build()); // Contact
    values.add(Value.newBuilder().setNumberValue(12).build()); // Day
    values.add(Value.newBuilder().setStringValue("aug").build()); // Month
    values.add(Value.newBuilder().setNumberValue(96).build()); // Duration
    values.add(Value.newBuilder().setNumberValue(2).build()); // Campaign
    values.add(Value.newBuilder().setNumberValue(-1).build()); // PDays
    values.add(Value.newBuilder().setNumberValue(0).build()); // Previous
    values.add(Value.newBuilder().setStringValue("unknown").build()); // POutcome
    // Act
    PredictionApi.predict(PROJECT_ID, MODEL_ID, values);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Prediction results:");
  }

  @Test
  public void testBatchPredictionUsingGcsSourceAndGcsDest()
      throws IOException, InterruptedException, ExecutionException {
    String inputUri = String.format("gs://%s-automl/tables/predictTest.csv", PROJECT_ID);
    String outputUri = String.format("gs://%s-automl/TEST_BATCH_PREDICT/", PROJECT_ID);
    // Act
    PredictionApi.batchPredictionUsingGcs(PROJECT_ID, MODEL_ID, inputUri, outputUri);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Batch Prediction results saved to specified Cloud Storage bucket.");

    Storage storage = StorageOptions.getDefaultInstance().getService();
    Page<Blob> blobs =
        storage.list(
            PROJECT_ID,
            Storage.BlobListOption.currentDirectory(),
            Storage.BlobListOption.prefix("TEST_BATCH_PREDICT/"));

    for (Blob blob : blobs.iterateAll()) {
      Page<Blob> fileBlobs =
          storage.list(
              PROJECT_ID,
              Storage.BlobListOption.currentDirectory(),
              Storage.BlobListOption.prefix(blob.getName()));
      for (Blob fileBlob : fileBlobs.iterateAll()) {
        if (!fileBlob.isDirectory()) {
          fileBlob.delete();
        }
      }
    }
  }

  // TODO: Create BigQuery Dataset for use in samples
  //
  //  @Test
  //  public void testBatchPredictionUsingBqSourceAndBqDest()
  //      throws IOException, InterruptedException, ExecutionException {
  //    // Act
  //    PredictionApi.batchPredictionUsingBigQuery(PROJECT_ID, MODEL_ID, BQ_INPUT_URI,
  // BQ_OUTPUT_URI_PREFIX);
  //
  //    // Assert
  //    String got = bout.toString();
  //    assertThat(got).contains("Operation name:");
  //  }
}
