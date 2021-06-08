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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.workflows.v1.OperationMetadata;
import com.google.cloud.workflows.v1.Workflow;
import com.google.cloud.workflows.v1.WorkflowName;
import com.google.cloud.workflows.v1.WorkflowsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.BeforeClass;
import org.junit.Test;

public class WorkflowsQuickstartTest {

  private static String projectId;
  private static final String LOCATION_ID = "us-central1";
  private static final String WORKFLOW_ID = "myFirstWorkflow";

  // Workflow source copied from:
  // https://github.com/GoogleCloudPlatform/workflows-samples/blob/main/src/myFirstWorkflow.workflows.yaml
  private static final String WORKFLOW_SOURCE = "# Copyright 2020 Google LLC\n#\n# Licensed under the Apache License, Version 2.0 (the \"License\");\n# you may not use this file except in compliance with the License.\n# You may obtain a copy of the License at\n#\n# http://www.apache.org/licenses/LICENSE-2.0\n#\n# Unless required by applicable law or agreed to in writing, software\n# distributed under the License is distributed on an \"AS IS\" BASIS,\n# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n# See the License for the specific language governing permissions and\n# limitations under the License.\n\n# [START workflows_myfirstworkflow]\n- getCurrentTime:\n call: http.get\n args:\n url: https://us-central1-workflowsample.cloudfunctions.net/datetime\n result: currentTime\n- readWikipedia:\n call: http.get\n args:\n url: https://en.wikipedia.org/w/api.php\n query:\n action: opensearch\n search: ${currentTime.body.dayOfTheWeek}\n result: wikiResult\n- returnResult:\n return: ${wikiResult.body[1]}\n# [END workflows_myfirstworkflow]\n";

  @BeforeClass
  public static void beforeClass() {
    final String ENV_GOOGLE_CLOUD_PROJECT = "GOOGLE_CLOUD_PROJECT";
    projectId = System.getenv(ENV_GOOGLE_CLOUD_PROJECT);
    assertNotNull(
        String.format("Environment variable '%s' is required to perform these tests.",
                ENV_GOOGLE_CLOUD_PROJECT),
        projectId);
  }

  @Test
  public void testQuickstart() throws IOException, InterruptedException, ExecutionException {
    // Deploy the workflow
    deployWorkflow(projectId, LOCATION_ID, WORKFLOW_ID);

    // Run the workflow we deployed
    String res = workflowsQuickstart(projectId, LOCATION_ID, WORKFLOW_ID);

    // A very basic assertion that we have some result.
    assertNotNull("Result should not be null", res);
    assertNotEquals("Result should not be empty", res, "");
  }

  private boolean deployWorkflow(String projectId, String location, String workflowId)
          throws IOException, InterruptedException, ExecutionException {
    // Create a new workflow if it doesn't exist
//    if (!workflowExists(projectId, location, workflowId)) {
      System.out.println("START DEPLOY");
      WorkflowsClient workflowsClient = WorkflowsClient.create();
      // Deploy workflow
      Workflow workflow = Workflow.newBuilder()
          .setName(workflowId)
          .setSourceContents(WORKFLOW_SOURCE)
          .build();

      // Wait until workflow is active
      Workflow deployedWorkflow = null;

      System.out.println("DEPLOY START");
      // Wait for the deployment to finish
      do {
        System.out.println("SLEEP");
        deployedWorkflow = workflowsClient.getWorkflow(WorkflowName.newBuilder()
            .setProject(projectId)
            .setLocation(location)
            .setWorkflow(workflowId)
            .build());
        Thread.sleep(2_000);
      } while (deployedWorkflow == null
              || deployedWorkflow.getState().equals(Workflow.State.ACTIVE));

      // Return true if the workflow is now active
      return deployedWorkflow.getState() != Workflow.State.ACTIVE;
  }

  /**
   * Returns true if the workflow exists.
   *
   * @return {boolean} True if the workflow exists already.
   */
  private boolean workflowExists(String projectId, String location, String workflow) {
    try (WorkflowsClient workflowsClient = WorkflowsClient.create()) {
      WorkflowName parent = WorkflowName.of(projectId, location, workflow);
      return workflowsClient.getWorkflow(parent).getState() == Workflow.State.ACTIVE;
    } catch (Exception e) {
      return false;
    }
  }
}
