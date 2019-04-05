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

package com.google.cloud.vision.automl.object.detection.samples;

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

/** Tests for Automl Vision Object Detection "Model API" sample. */
@RunWith(JUnit4.class)
public class ModelApiIT {
  // TODO(developer): Change PROJECT_ID, COMPUTE_REGION, DATASET_ID, DEPLOY_MODEL_ID and
  // UNDEPLOY_MODEL_ID before running the test cases.
  private static final String PROJECT_ID = "java-docs-samples-testing";
  private static final String COMPUTE_REGION = "us-central1";
  private static final String DATASET_ID = "IOD7155850371984785408";
  private static final String FILTER = "imageObjectDetectionModelMetadata:*";
  private static final String MODEL_NAME = "test_vision_model";
  private static final String DEPLOY_MODEL_ID = "IOD1728502647608049664";
  private static final String UNDEPLOY_MODEL_ID = "IOD3348109663601164288";
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
    // Act
    ModelApi.createModel(PROJECT_ID, COMPUTE_REGION, DATASET_ID, MODEL_NAME);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Training started...");

    // Act
    bout.reset();
    ModelApi.listModels(PROJECT_ID, COMPUTE_REGION, FILTER);

    // Assert
    got = bout.toString();
    String modelId = got.split("\n")[2].split("/")[got.split("\n")[2].split("/").length - 1];
    assertThat(got).contains("Model Id:");

    // Act
    bout.reset();
    ModelApi.getModel(PROJECT_ID, COMPUTE_REGION, modelId);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Model name:");

    // Act
    bout.reset();
    ModelApi.listModelEvaluations(PROJECT_ID, COMPUTE_REGION, modelId, "");

    // Assert
    got = bout.toString();
    String modelEvaluationId = got.split("Model evaluation name: ")[1].split("/")[7].split("\n")[0];
    assertThat(got).contains("name:");

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
    ModelApi.deleteModel(PROJECT_ID, COMPUTE_REGION, modelId);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Model deletion started...");
  }

  @Test
  public void testDeployModel() throws IOException, InterruptedException, ExecutionException {
    // Act
    ModelApi.deployModel(PROJECT_ID, COMPUTE_REGION, DEPLOY_MODEL_ID);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Name:");
  }

  @Test
  public void testUndeployModel() throws IOException, InterruptedException, ExecutionException {
    // Act
    ModelApi.undeployModel(PROJECT_ID, COMPUTE_REGION, UNDEPLOY_MODEL_ID);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Name:");
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
}
