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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.cloud.language.automl.sentiment.analysis.samples.PredictionApi;

/** Tests for AutoML Natural Language Sentiment Analysis "PredictionAPI" sample. */
@RunWith(JUnit4.class)
public class PredictionApiIT {
  // TODO(developer): Change PROJECT_ID, COMPUTE_REGION and MODEL_ID before
  // running the test cases.
  private static final String PROJECT_ID = "java-docs-samples-testing";
  private static final String COMPUTE_REGION = "us-central1";
  private static final String MODEL_ID = "TST1520889285651364720";
  private static final String FILE_PATH = "./resource/sentimentInput.txt";
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
    // Act
    PredictionApi.predict(PROJECT_ID, COMPUTE_REGION, MODEL_ID, FILE_PATH);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Prediction results:");
  }
}
