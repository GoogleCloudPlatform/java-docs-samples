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

/** Tests for AutoML Tables "Model API" sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class ModelApiIT {
  private static final String PROJECT_ID = "java-docs-samples-testing";
  private static final String COMPUTE_REGION = "us-central1";
  private static final String DATASET_ID = "TBL2017172828410871808";
  private static final String MODEL_NAME = "test_tables_model";
  private static final String TABLE_ID = "2071233616125362176";
  private static final String COLUMN_ID = "773141392279994368";
  private static final String TRAIN_BUDGET = "1000";
  private static final String MODEL_ID = "TBL5997440105332080640";
  private static final String BQ_OUTPUT_URI = "bq://automl-tables-bg-output";
  private static final String FILTER = "tablesModelMetadata:*";
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
  public void testModelApi() throws IOException, InterruptedException, ExecutionException {
    //    // Act
    //    ModelApi.createModel(
    //        PROJECT_ID, COMPUTE_REGION, DATASET_ID, TABLE_ID, COLUMN_ID, MODEL_NAME,
    // TRAIN_BUDGET);
    //
    //    // Assert
    //    String got = bout.toString();
    //    assertThat(got).contains("Training started...");

    // Act
    bout.reset();
    ModelApi.listModels(PROJECT_ID, COMPUTE_REGION, FILTER);

    // Assert
    String got = bout.toString();
    String modelId = got.split("\n")[2].split("/")[got.split("\n")[2].split("/").length - 1];
    assertThat(got).contains("Model Id:");

    // Act
    bout.reset();
    ModelApi.getModel(PROJECT_ID, COMPUTE_REGION, modelId);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Model Id:");

    // Act
    bout.reset();
    ModelApi.listModelEvaluations(PROJECT_ID, COMPUTE_REGION, modelId, "");

    // Assert
    got = bout.toString();
    String modelEvaluationId = got.split("Model evaluation name: ")[1].split("/")[7].split("\n")[0];
    assertThat(got).contains("Model evaluation name:");

    // Act
    bout.reset();
    ModelApi.getModelEvaluation(PROJECT_ID, COMPUTE_REGION, modelId, modelEvaluationId);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Model evaluation name:");

    // Act
    bout.reset();
    ModelApi.displayEvaluation(PROJECT_ID, COMPUTE_REGION, modelId, "");

    // Assert
    got = bout.toString();
    assertThat(got).contains("Model Evaluation ID:");

    // Act
    bout.reset();
    ModelApi.exportEvaluatedExamples(PROJECT_ID, COMPUTE_REGION, modelId, BQ_OUTPUT_URI);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Operation name:");

    //    // Act
    //    bout.reset();
    //    ModelApi.deleteModel(PROJECT_ID, COMPUTE_REGION, modelId);
    //
    //    // Assert
    //    got = bout.toString();
    //    assertThat(got).contains("Model deletion started...");
  }

  @Test
  public void testUndeployDeployModel() throws Exception {
    // Act
    ModelApi.undeployModel(PROJECT_ID, COMPUTE_REGION, MODEL_ID);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Model undeployment finished");

    // Act
    ModelApi.deployModel(PROJECT_ID, COMPUTE_REGION, MODEL_ID);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Model deployment finished");
  }

  @Test
  public void testOperationStatus() throws IOException {
    // Act
    ModelApi.listOperationsStatus(PROJECT_ID, COMPUTE_REGION, "");

    // Assert
    String got = bout.toString();
    String operationId = got.split("\n")[1].split(":")[1].trim();
    assertThat(got).contains("Operation details:");

    // Act
    bout.reset();
    ModelApi.getOperationStatus(operationId);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Operation details:");
  }

  @Test
  public void testDeleteModel() {
    // As model creation can take many hours, instead try to delete a
    // nonexistent model and confirm that the model was not found, but other
    // elements of the request were valid.
    try {
      ModelApi.deleteModel(PROJECT_ID, "TRL0000000000000000000");
      String got = bout.toString();
      assertThat(got).contains("The model does not exist");
    } catch (IOException | ExecutionException | InterruptedException e) {
      assertThat(e.getMessage()).contains("The model does not exist");
    }
  }
}
