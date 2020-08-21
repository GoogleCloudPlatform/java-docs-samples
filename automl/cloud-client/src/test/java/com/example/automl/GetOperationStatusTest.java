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
import com.google.cloud.automl.v1.GcsDestination;
import com.google.cloud.automl.v1.OperationMetadata;
import com.google.cloud.automl.v1.OutputConfig;
import com.google.protobuf.Empty;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
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
  private static final String GCS_OUTPUT_PREFIX =
      String.format(
          "gs://translate_data_exported/TEST_OUTPUT_%s/",
          UUID.randomUUID().toString().replace("-", "_").substring(0, 26));

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

      GcsDestination gcsSource =
          GcsDestination.newBuilder().setOutputUriPrefix(GCS_OUTPUT_PREFIX).build();

      OutputConfig outputConfig = OutputConfig.newBuilder().setGcsDestination(gcsSource).build();

      // Start the export job
      OperationFuture<Empty, OperationMetadata> operation =
          client.exportDataAsync(datasetFullId, outputConfig);

      operationFullName = operation.getName();
    }

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  @Test(timeout = 180000)
  public void tearDown() throws IOException, InterruptedException {
    // delete the cancelled operation
    try (AutoMlClient client = AutoMlClient.create()) {
      // wait for the operation to be cancelled
      while (!client.getOperationsClient().getOperation(operationFullName).getDone()) {
        Thread.sleep(1000);
      }
      client.getOperationsClient().deleteOperation(operationFullName);
    }

    System.setOut(null);
  }

  @Test
  public void testGetOperationStatus() throws IOException {
    GetOperationStatus.getOperationStatus(operationFullName);
    String got = bout.toString();
    assertThat(got).contains("Operation details:");
  }
}
