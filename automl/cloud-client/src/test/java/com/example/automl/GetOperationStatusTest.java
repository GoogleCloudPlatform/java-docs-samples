/*
 * Copyright 2020 Google LLC
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

package com.example.automl;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1.AutoMlClient;
import com.google.cloud.automl.v1.DatasetName;
import com.google.cloud.automl.v1.GcsSource;
import com.google.cloud.automl.v1.InputConfig;
import com.google.cloud.automl.v1.OperationMetadata;
import com.google.protobuf.Empty;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GetOperationStatusTest {
  private static final String PROJECT_ID = System.getenv("AUTOML_PROJECT_ID");
  private static final String DATASET_ID = "TRL5394674636845744128";
  private static final String BUCKET = "gs://translate_data_exported/translation.csv";

  private String operationFullName;
  private ByteArrayOutputStream bout;
  private PrintStream out;

  private static void requireEnvVar(String varName) {
    assertNotNull(
        System.getenv(varName),
        "Environment variable '%s' is required to perform these tests.".format(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("AUTOML_PROJECT_ID");
  }

  @Before
  public void setUp() throws IOException, ExecutionException, InterruptedException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    // start a export data into dataset and cancel it before it finishes.
    try (AutoMlClient client = AutoMlClient.create()) {
      // Get the complete path of the dataset.
      DatasetName datasetFullId = DatasetName.of(PROJECT_ID, "us-central1", DATASET_ID);

      GcsSource gcsSource =
          GcsSource.newBuilder().addAllInputUris(Arrays.asList(BUCKET.split(","))).build();

      InputConfig inputConfig = InputConfig.newBuilder().setGcsSource(gcsSource).build();

      // Start the import LRO job
      OperationFuture<Empty, OperationMetadata> operation =
          client.importDataAsync(datasetFullId, inputConfig);

      operationFullName = operation.getName();
    }

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @Test
  public void testGetOperationStatus() throws IOException {
    GetOperationStatus.getOperationStatus(operationFullName);
    String got = bout.toString();
    assertThat(got).contains("Operation details:");
  }

  @After
  public void tearDown() throws IOException, InterruptedException {
    try (AutoMlClient client = AutoMlClient.create()) {
      // terminate export data LRO.
      client.getOperationsClient().cancelOperation(operationFullName);
    }

    System.setOut(null);
  }
}
