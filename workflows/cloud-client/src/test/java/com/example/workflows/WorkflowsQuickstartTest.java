/*
 * Copyright 2021 Google LLC
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

import static com.example.workflows.WorkflowsQuickstart.workflowsQuickstart;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;

import com.google.api.client.util.Strings;
import com.google.cloud.workflows.v1.LocationName;
import com.google.cloud.workflows.v1.Workflow;
import com.google.cloud.workflows.v1.WorkflowName;
import com.google.cloud.workflows.v1.WorkflowsClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class WorkflowsQuickstartTest {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION_ID = "us-central1";
  private static final String WORKFLOW_ID = "java-quickstart-" + UUID.randomUUID().toString();
  private static WorkflowsClient workflowsClient;
  private static ByteArrayOutputStream stdOut;

  @BeforeClass
  public static void beforeAll() throws Exception {
    assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));
    // Deploy the workflow
    deployWorkflow(PROJECT_ID, LOCATION_ID, WORKFLOW_ID);
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @AfterClass
  public static void afterAll() throws Exception {
    deleteWorkflow(PROJECT_ID, LOCATION_ID, WORKFLOW_ID);

    stdOut = null;
    System.setOut(null);
  }

  @Test
  public void testQuickstart() throws IOException, InterruptedException, ExecutionException {
    // Run the workflow we deployed
    workflowsQuickstart(PROJECT_ID, LOCATION_ID, WORKFLOW_ID);
    assertThat(stdOut.toString()).contains("Execution results:");
    assertThat(stdOut.toString()).contains("Execution finished with state: SUCCEEDED");
  }

  private static void deployWorkflow(String projectId, String location, String workflowId)
      throws IOException, InterruptedException, ExecutionException {

    if (workflowsClient == null) {
      workflowsClient = WorkflowsClient.create();
    }
    LocationName parent = LocationName.of(projectId, location);

    String source =
        new String(
            Files.readAllBytes(
                Paths.get("src/test/java/com/example/workflows/resources/source.yaml")));
    Workflow workflow = Workflow.newBuilder().setSourceContents(source).build();

    // Deploy workflow
    workflowsClient.createWorkflowAsync(parent, workflow, workflowId).get();
  }

  public static void deleteWorkflow(String projectId, String location, String workflowId)
      throws IOException {
    if (workflowsClient == null) {
      workflowsClient = WorkflowsClient.create();
    }
    workflowsClient.deleteWorkflowAsync(WorkflowName.of(PROJECT_ID, LOCATION_ID, WORKFLOW_ID));
  }
}
