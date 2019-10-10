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

package com.google.cloud.translate.automl;

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

/** Tests for Automl translation models. */
@RunWith(JUnit4.class)
public class ModelIT {
  // TODO(developer): Change PROJECT_ID, COMPUTE_REGION, DATASET_ID, DEPLOY_MODEL_ID and
  // UNDEPLOY_MODEL_ID before running the test cases.
  //e PrintStream out;private static final String PROJECT_ID = "java-docs-samples-testing";
  ////  private static final String DATASET_ID = "IOD7155850371984785408";
  ////  private static final String MODEL_NAME = "test_vision_model";
  ////  private static final String DEPLOY_MODEL_ID = "IOD1728502647608049664";
  ////  private static final String UNDEPLOY_MODEL_ID = "IOD3348109663601164288";
  ////  private ByteArrayOutputStream bout;
  ////  private PrintStream out;
  private static final String PROJECT_ID = "java-docs-samples-testing";
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private ModelApi app;
  private String modelId;
  private String modelEvaluationId;

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
  public void testModelApi() {
    // Act
    ListModels.listModels(PROJECT_ID);

    // Assert
    String got = bout.toString();
    modelId = got.split("\n")[1].split("/")[got.split("\n")[1].split("/").length - 1];
    assertThat(got).contains("Model id:");

    // Act
    GetModel.getModel(PROJECT_ID, modelId);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Model name:");

    // Act
    ListModelEvaluations.listModelEvaluations(PROJECT_ID, modelId);

    // Assert
    got = bout.toString();
    modelEvaluationId = got.split("List of model evaluations:")[1].split("\"")[1].split("/")[7];
    assertThat(got).contains("name:");

    // Act
    GetModelEvaluation.getModelEvaluation(PROJECT_ID, modelId, modelEvaluationId);

    // Assert
    got = bout.toString();
    assertThat(got).contains("name:");
  }

  @Test
  public void testDeployModel() {
    // Act
    DeployModel.deployModel(PROJECT_ID, DEPLOY_MODEL_ID);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Name:");
  }

  @Test
  public void testUndeployModel() {
    // Act
    UndeployModel.undeployModel(PROJECT_ID, UNDEPLOY_MODEL_ID);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Name:");
  }

  @Test
  public void testOperationStatus() {
    // Act
    ListOperationStatus.listOperationStatus(PROJECT_ID);

    // Assert
    String got = bout.toString();
    String operationId = got.split("\n")[1].split(":")[1].trim();
    assertThat(got).contains("Operation details:");

    // Act
    bout.reset();
    GetOperationStatus.getOperationStatus(operationId);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Operation details:");
  }
}