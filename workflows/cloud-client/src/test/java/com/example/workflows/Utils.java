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

import com.google.cloud.workflows.v1.LocationName;
import com.google.cloud.workflows.v1.Workflow;
import com.google.cloud.workflows.v1.WorkflowName;
import com.google.cloud.workflows.v1.WorkflowsClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

public class Utils {
  public static void createWorkflow(String projectId, String location, String workflowId)
      throws IOException, InterruptedException, ExecutionException {

    try (WorkflowsClient workflowsClient = WorkflowsClient.create()) {
      LocationName parent = LocationName.of(projectId, location);

      String source =
          new String(Files.readAllBytes(Paths.get("resources/myFirstWorkflow.workflows.yaml")));
      Workflow workflow = Workflow.newBuilder().setSourceContents(source).build();

      workflowsClient.createWorkflowAsync(parent, workflow, workflowId).get();
    }
  }

  public static void deleteWorkflow(String projectId, String location, String workflowId)
      throws IOException, InterruptedException, ExecutionException {
    try (WorkflowsClient workflowsClient = WorkflowsClient.create()) {
      workflowsClient.deleteWorkflowAsync(WorkflowName.of(projectId, location, workflowId)).get();
    }
  }
}
