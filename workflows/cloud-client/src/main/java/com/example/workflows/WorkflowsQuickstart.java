/*
 * Copyright 2021 Google Inc.
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

import com.google.cloud.workflows.executions.v1.Execution;
import com.google.cloud.workflows.executions.v1.ExecutionsClient;
import com.google.cloud.workflows.executions.v1.WorkflowName;

/**
 * Cloud Workflows API sample. Executes a workflow and waits for results.
 * Example usage:
 * GOOGLE_CLOUD_PROJECT=myProject \
 * mvn package exec:java -Dexec.mainClass='com.example.workflows.WorkflowsQuickstart'
 */
public class WorkflowsQuickstart {
  private static final String GOOGLE_CLOUD_PROJECT = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String LOCATION = System.getenv("LOCATION");
  private static String WORKFLOW = System.getenv("WORKFLOW");

  public static String workflowsQuickstart(String projectId, String location, String workflow) {
    // Execute workflow
    try (ExecutionsClient workflowExecutionsClient = ExecutionsClient.create()) {
      WorkflowName parent = WorkflowName.of(projectId, location, workflow);
      Execution initialExecution = Execution.newBuilder().build();
      Execution createExecutionRes = workflowExecutionsClient
          .createExecution(parent, initialExecution);

      String executionName = createExecutionRes.getName();
      System.out.printf("Created execution: %s%n", executionName);

      // Wait for execution to finish, then print results.
      boolean executionFinished = false;
      long backoffDelay = 1_000; // Start wait with delay of 1,000 ms
      System.out.println("Poll for results...");
      while (!executionFinished) {
        Execution execution = workflowExecutionsClient.getExecution(executionName);
        executionFinished = execution.getState() != Execution.State.ACTIVE;

        // If we haven't seen the results yet, wait.
        if (!executionFinished) {
          System.out.println("- Waiting for results");
          Thread.sleep(backoffDelay);
          backoffDelay *= 2; // Double the delay to provide exponential backoff.
        } else {
          System.out.printf("Execution finished with state: %s%n", execution.getState().name());
          System.out.println(execution.getResult());
          return execution.getResult();
        }
      }
      // This return is never reached.
      return "";
    } catch (Exception e) {
      System.out.printf("Error executing workflow: %s%n", e);
      return "";
    }
  }

  /**
   * Demonstrates using the Workflows API.
   */
  public static void main(String... args) {
    if (GOOGLE_CLOUD_PROJECT.isEmpty()) System.out.println("GOOGLE_CLOUD_PROJECT is empty");
    if (LOCATION == null || LOCATION.isEmpty()) {
      LOCATION = "us-central1";
    }
    if (WORKFLOW == null || WORKFLOW.isEmpty()) {
      WORKFLOW = "myFirstWorkflow";
    }
    workflowsQuickstart(GOOGLE_CLOUD_PROJECT, LOCATION, WORKFLOW);
  }
}
// [END workflows_api_quickstart]
