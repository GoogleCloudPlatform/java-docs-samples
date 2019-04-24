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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for AutoML Natural Language Entity Extraction Model operations. */
@RunWith(JUnit4.class)
public class ModelIT {

  private static final String PROJECT_ID = System.getenv("PROJECT_ID");
  private static final String COMPUTE_REGION = "us-central1";
  private static final String FILTER = "textExtractionModelMetadata:*";
  private static final String MODEL_ID = "TEN1974951581904273408";

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
  public void testModelOperations() throws Exception {

    // Act
    ListModels.listModels(PROJECT_ID, COMPUTE_REGION, FILTER);

    // Assert
    String got = bout.toString();
    assertThat(got).contains(MODEL_ID);

    // Act
    bout.reset();
    GetModel.getModel(PROJECT_ID, COMPUTE_REGION, MODEL_ID);

    // Assert
    got = bout.toString();
    assertThat(got).contains(MODEL_ID);

    // Act
    bout.reset();
    ListModelEvaluations.listModelEvaluations(PROJECT_ID, COMPUTE_REGION, MODEL_ID, "");

    // Assert
    got = bout.toString();
    String modelEvaluationId = got.split("Model evaluation name: ")[1].split("/")[7].split("\n")[0];
    assertThat(got).contains("1195072363916475857");

    // Act
    bout.reset();
    GetModelEvaluation.getModelEvaluation(PROJECT_ID, COMPUTE_REGION, MODEL_ID, modelEvaluationId);

    // Assert
    got = bout.toString();
    assertThat(got).contains(modelEvaluationId);

    // Act
    bout.reset();
    DisplayEvaluation.displayEvaluation(PROJECT_ID, COMPUTE_REGION, MODEL_ID, "");

    // Assert
    got = bout.toString();
    assertThat(got).contains("Model Evaluation ID:6184534264814397965");

  }

  @Test
  public void testOperationStatus() throws IOException {
    // Act
    ListOperationsStatus.listOperationsStatus(PROJECT_ID, COMPUTE_REGION, "");

    // Assert
    String got = bout.toString();
    String operationId = got.split("\n")[1].split(":")[1].trim();
    assertThat(got).contains("Operation details:");

    // Act
    bout.reset();
    GetOperationStatus.getOperationStatus(operationId);

    // Assert
    got = bout.toString();
    assertThat(got).contains(operationId);
  }
}
