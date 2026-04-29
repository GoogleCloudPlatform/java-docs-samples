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
      long backoffDelay = 1_000;

      while (!workflowExists(workflowsClient, projectId, location, workflowId)) {
        Thread.sleep(backoffDelay);
        backoffDelay *= 2;
      }
    }
  }

  private static boolean workflowExists(
      WorkflowsClient workflowsClient, String projectId, String location, String workflowId) {
    try {
      WorkflowName workflowName = WorkflowName.of(projectId, location, workflowId);
      workflowsClient.getWorkflow(workflowName);
      return true;
    } catch (Exception e) {
      System.out.println("Workflow doesn't exist");
      return false;
    }
  }

  public static void deleteWorkflow(String projectId, String location, String workflowId)
      throws IOException, InterruptedException {
    try (WorkflowsClient workflowsClient = WorkflowsClient.create()) {
      workflowsClient.deleteWorkflowAsync(WorkflowName.of(projectId, location, workflowId));

      long backoffDelay = 1_000;
      while (workflowExists(workflowsClient, projectId, location, workflowId)) {
        Thread.sleep(backoffDelay);
        backoffDelay *= 2;
      }
    }
  }
}
