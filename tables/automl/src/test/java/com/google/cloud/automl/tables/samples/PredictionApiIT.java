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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for AutoML Tables "Prediction API" sample. */
@RunWith(JUnit4.class)
public class PredictionApiIT {
  // TODO(developer): Change PROJECT_ID,COMPUTE_REGION, MODEL_ID, GCS_INPUT_URI,
  // GCS_OUTPUT_URI_PREFIX, BQ_INPUT_URI and BQ_OUTPUT_URI_PREFIX before running the test cases.
  private static final String PROJECT_ID = "java-docs-samples-testing";
  private static final String COMPUTE_REGION = "us-central1";
  private static final String MODEL_ID = "TBL3613734080685801472";
  private static final String FILE_PATH = "./resource/predictTest.csv";
  private static final String GCS_INPUT_URI = "gs://automl-tables/input/test.csv";
  private static final String GCS_OUTPUT_URI_PREFIX = "gs://automl-tables/output";
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
  public void testPredict() throws IOException, InterruptedException, ExecutionException {
    // Act
    PredictionApi.predict(PROJECT_ID, COMPUTE_REGION, MODEL_ID, FILE_PATH);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Prediction results:");
  }

  @Test
  public void testBatchPredictionUsingGcsSourceAndGcsDest()
      throws IOException, InterruptedException, ExecutionException {
    // Act
    PredictionApi.batchPredictionUsingGcsSourceAndGcsDest(
        PROJECT_ID, COMPUTE_REGION, MODEL_ID, GCS_INPUT_URI, GCS_OUTPUT_URI_PREFIX);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Operation name:");
  }

  @Test
  public void testBatchPredictionUsingBqSourceAndGcsDest()
      throws IOException, InterruptedException, ExecutionException {
    // Act
    PredictionApi.batchPredictionUsingBqSourceAndGcsDest(
        PROJECT_ID, COMPUTE_REGION, MODEL_ID, BQ_INPUT_URI, GCS_OUTPUT_URI_PREFIX);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Operation name:");
  }

  @Test
  public void testBatchPredictionUsingGcsSourceAndBqDest()
      throws IOException, InterruptedException, ExecutionException {
    // Act
    PredictionApi.batchPredictionUsingGcsSourceAndBqDest(
        PROJECT_ID, COMPUTE_REGION, MODEL_ID, GCS_INPUT_URI, BQ_OUTPUT_URI_PREFIX);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Operation name:");
  }

  @Test
  public void testBatchPredictionUsingBqSourceAndBqDest()
      throws IOException, InterruptedException, ExecutionException {
    // Act
    PredictionApi.batchPredictionUsingBqSourceAndBqDest(
        PROJECT_ID, COMPUTE_REGION, MODEL_ID, BQ_INPUT_URI, BQ_OUTPUT_URI_PREFIX);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Operation name:");
  }
}
