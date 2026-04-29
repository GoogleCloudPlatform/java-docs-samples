/*
 * Copyright 2026 Google LLC
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

package com.example.workflows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExecuteWithArgumentsIT {
  private static String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String LOCATION = "us-central1";
  private static String WORKFLOW_ID = "java_myFirstWorkflow_" + UUID.randomUUID();
  private static ByteArrayOutputStream bout;

  private static void requireEnvVar(String varName) {
    assertNotNull(
        "Environment variable " + varName + " is required to perform these tests.",
        System.getenv(varName));
  }

  @BeforeClass
  public static void setUp() throws IOException, InterruptedException, ExecutionException {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    // Create workflow
    Utils.createWorkflow(PROJECT_ID, LOCATION, WORKFLOW_ID);

    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @AfterClass
  public static void tearDown() throws IOException, InterruptedException, ExecutionException {
    // Delete workflow
    Utils.deleteWorkflow(PROJECT_ID, LOCATION, WORKFLOW_ID);

    System.setOut(null);
  }

  @Test
  public void testExecuteWorkflowWithArguments()
      throws IOException, InterruptedException, ExecutionException {
    ExecuteWithArguments.executeWorkflowWithArguments(PROJECT_ID, LOCATION, WORKFLOW_ID);

    String output = bout.toString();
    assertThat(output).contains("Execution finished with state: SUCCEEDED");
    assertThat(output).contains("Execution results: ");
    assertThat(output).contains("Cloud");
  }
}
