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

// [START workflows_api_quickstart]

// Imports the Google Cloud client library

import com.google.cloud.workflows.executions.v1.CreateExecutionRequest;
import com.google.cloud.workflows.executions.v1.Execution;
import com.google.cloud.workflows.executions.v1.ExecutionsClient;
import com.google.cloud.workflows.executions.v1.WorkflowName;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class WorkflowsQuickstart {

  public static void main(String... args)
      throws IOException, InterruptedException, ExecutionException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String location = "us- central1";
    String workflow = "myFirstWorkflow";
    workflowsQuickstart(projectId, location, workflow);
  }

  private static volatile boolean finished;
  private static final long BACKOFF_TIMEOUT = 10 * 60 * 1_000; // Time out at 10 minutes

  public static void workflowsQuickstart(String projectId, String location, String workflow)
      throws IOException, InterruptedException, ExecutionException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests. After completing all of your
    // requests, call the "close" method on the client to safely clean up any remaining background
    // resources.
    try (ExecutionsClient executionsClient = ExecutionsClient.create()) {
      // Construct the fully qualified location path.
      WorkflowName parent = WorkflowName.of(projectId, location, workflow);

      // Creates the execution object.
      CreateExecutionRequest request =
          CreateExecutionRequest.newBuilder()
              .setParent(parent.toString())
              .setExecution(Execution.newBuilder().build())
              .build();
      Execution response = executionsClient.createExecution(request);

      String executionName = response.getName();
      System.out.printf("Created execution: %s%n", executionName);

      long backoffTime = 0;
      long backoffDelay = 1_000; // Start wait with delay of 1,000 ms
      System.out.println("Poll for results...");
      
      // Wait for execution to finish, then print results.
      while (!finished && backoffTime < BACKOFF_TIMEOUT) {
        Execution execution = executionsClient.getExecution(executionName);
        finished = execution.getState() != Execution.State.ACTIVE;

        // If we haven't seen the results yet, wait.
        if (!finished) {
          System.out.println("- Waiting for results");
          Thread.sleep(backoffDelay);
          backoffTime += backoffDelay;
          backoffDelay *= 2; // Double the delay to provide exponential backoff.
        } else {
          System.out.println("Execution finished with state: " + execution.getState().name());
          System.out.println("Execution results: " + execution.getResult());
        }
      }
    }
  }
}
// [END workflows_api_quickstart]
