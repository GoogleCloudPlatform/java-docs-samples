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

import com.google.cloud.workflows.executions.v1.CreateExecutionRequest;
import com.google.cloud.workflows.executions.v1.Execution;
import com.google.cloud.workflows.executions.v1.ExecutionsClient;
import com.google.cloud.workflows.executions.v1.WorkflowName;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

// [START workflows_execute_with_arguments]

public class ExecuteWithArguments {
  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {
    String projectId = "your-project-id";
    String location = "your-location"; // For example: us-central1
    String workflowId = "your-workflow-id";

    executeWorkflowWithArguments(projectId, location, workflowId);
  }

  public static void executeWorkflowWithArguments(
      String projectId, String location, String workflowId)
      throws IOException, InterruptedException, ExecutionException {
    try (ExecutionsClient executionsClient = ExecutionsClient.create()) {
      WorkflowName parent = WorkflowName.of(projectId, location, workflowId);

      CreateExecutionRequest executionRequest =
          CreateExecutionRequest.newBuilder()
              .setParent(parent.toString())
              .setExecution(
                  Execution.newBuilder().setArgument("{\"searchTerm\":\"Cloud\"}").build())
              .build();

      Execution executionCreated = executionsClient.createExecution(executionRequest);

      System.out.println("Created execution: " + executionCreated.getName());

      // Wait for execution to finish using exponential backoff
      long backoffDelay = 1_000;
      Execution execution;
      System.out.println("Poll for result...");
      while (true) {
        execution = executionsClient.getExecution(executionCreated.getName());

        // Check if execution finished
        if (execution.getState() != Execution.State.ACTIVE) {
          break;
        }

        // Wait using exponential backoff
        System.out.println("- Waiting for results...");
        Thread.sleep(backoffDelay);
        backoffDelay *= 2;
      }

      System.out.println("Execution finished with state: " + execution.getState().name());
      System.out.println("Execution results: " + execution.getResult());
    }
  }
}
// [END workflows_execute_with_arguments]
