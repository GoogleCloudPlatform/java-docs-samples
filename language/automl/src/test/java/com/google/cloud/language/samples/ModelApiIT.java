/*
 * Copyright 2018 Google Inc.
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

package com.google.cloud.language.samples;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for vision "Model API" sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class ModelApiIT {
  private static final String PROJECT_ID = "java-docs-samples-testing";
  private static final String COMPUTE_REGION = "us-central1";
  private static final String MODEL_ID = "TCN342705131419266916";
  private static final String MODEL_EVALUATION_ID = "3666189665418739402";
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
  public void testModelApi() throws Exception {
    // Act
    ModelApi.listModels(PROJECT_ID, COMPUTE_REGION, "");

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Model id:");

    // Act
    ModelApi.getModel(PROJECT_ID, COMPUTE_REGION, MODEL_ID);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Model name:");

    // Act
    ModelApi.listModelEvaluations(PROJECT_ID, COMPUTE_REGION, MODEL_ID, "");

    // Assert
    got = bout.toString();
    assertThat(got).contains("name:");
  }

  @Test
  public void testGetModelEvaluation() throws Exception {

    // Act
    ModelApi.getModelEvaluation(
        PROJECT_ID, COMPUTE_REGION, MODEL_ID, MODEL_EVALUATION_ID);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("name:");
  }
}
