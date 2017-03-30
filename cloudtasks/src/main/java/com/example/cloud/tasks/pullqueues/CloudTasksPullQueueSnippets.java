/*
 * Copyright (c) 2016 Google Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.cloud.tasks.pullqueues;

import com.example.cloud.tasks.Common;

import com.google.api.client.util.Strings;
import com.google.api.services.cloudtasks.v2beta2.CloudTasks;
import com.google.api.services.cloudtasks.v2beta2.model.AcknowledgeTaskRequest;
import com.google.api.services.cloudtasks.v2beta2.model.CreateTaskRequest;
import com.google.api.services.cloudtasks.v2beta2.model.ListQueuesResponse;
import com.google.api.services.cloudtasks.v2beta2.model.PullTaskTarget;
import com.google.api.services.cloudtasks.v2beta2.model.PullTasksRequest;
import com.google.api.services.cloudtasks.v2beta2.model.PullTasksResponse;
import com.google.api.services.cloudtasks.v2beta2.model.Queue;
import com.google.api.services.cloudtasks.v2beta2.model.Task;
import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/** Sample snippets that demonstrate interacting with the Cloud Tasks API. */
public class CloudTasksPullQueueSnippets {

  // Authenticated HTTP client for the Cloud Task API
  private final CloudTasks client;

  // Google Cloud Project ID
  private final String projectId;

  // Queue location (region)
  private final String locationId;

  // Test payload to attach to the task
  private static final String PAYLOAD = "a message for the recipient";

  /**
   * Construct the snippets object.
   *
   * @param client Authenticated Cloud Tasks API client
   * @param projectId Google Cloud Project ID
   * @param locationId Google Cloud region
   */
  public CloudTasksPullQueueSnippets(CloudTasks client, String projectId, String locationId) {
    this.client = client;
    this.projectId = projectId;
    this.locationId = locationId;
  }

  /**
   * Lists all queues for a location.
   *
   * @return A list of queues
   * @throws IOException On network error
   */
  public List<Queue> listQueues() throws IOException {
    String parent = "projects/" + this.projectId + "/locations/" + this.locationId;

    List<Queue> results = new ArrayList<Queue>();
    String nextPageToken = "";
    do {
      ListQueuesResponse response =
          this.client
              .projects()
              .locations()
              .queues()
              .list(parent)
              .setPageToken(nextPageToken)
              .setPageSize(10)
              .execute();
      if (response.isEmpty()) {
        break;
      }
      results.addAll(response.getQueues());
      nextPageToken = response.getNextPageToken();
    } while (!Strings.isNullOrEmpty(nextPageToken));
    return results;
  }

  /**
   * Creates a new task on a queue.
   *
   * @param queue The queue to create a task for.
   * @return The newly created Task.
   * @throws IOException On network error
   */
  public Task createTask(Queue queue) throws IOException {
    // Payloads must be base64 encoded
    String encodedPayload = BaseEncoding.base64().encode(PAYLOAD.getBytes(Charsets.US_ASCII));
    PullTaskTarget pullTaskTarget = new PullTaskTarget().setPayload(encodedPayload);
    Task task = new Task().setPullTaskTarget(pullTaskTarget);
    CreateTaskRequest taskRequest = new CreateTaskRequest().setTask(task);
    Task result =
        this.client
            .projects()
            .locations()
            .queues()
            .tasks()
            .create(queue.getName(), taskRequest)
            .execute();
    return result;
  }

  /**
   * Pulls a task from a queue which is leased for 10 minutes.
   *
   * @param queue The queue to pull the task from.
   * @return The task.
   * @throws IOException On network error
   */
  public Task pullTask(Queue queue) throws IOException {
    PullTasksRequest pullTasksRequest =
        new PullTasksRequest().setMaxTasks(1).setLeaseDuration("600s");
    PullTasksResponse pullTaskResponse =
        this.client
            .projects()
            .locations()
            .queues()
            .tasks()
            .pull(queue.getName(), pullTasksRequest)
            .execute();
    Task task = pullTaskResponse.getTasks().get(0);
    return task;
  }

  /**
   * Acknowledges a task which removes it from the queue.
   *
   * @param task The task to acknowledge.
   * @throws IOException On network error
   */
  public void acknowledgeTask(Task task) throws IOException {
    AcknowledgeTaskRequest acknowledgeTaskRequest =
        new AcknowledgeTaskRequest().setScheduleTime(task.getScheduleTime());
    client
        .projects()
        .locations()
        .queues()
        .tasks()
        .acknowledge(task.getName(), acknowledgeTaskRequest);
  }

  /**
   * Main runner that prints results to output stream to improve testability.
   *
   * @param os Output stream
   */
  public static void runner(String[] args, PrintStream os) throws Exception {
    if (args.length != 3) {
      System.err.println(
          String.format(
              "Usage: %s <project-name> <location_id> <queue_id>",
              CloudTasksPullQueueSnippets.class.getSimpleName()));
      return;
    }
    String projectId = args[0];
    String locationId = args[1];
    String queueId = args[2];
    String queueName =
            "projects/" + projectId + "/locations/" + locationId + "/queues/" + queueId;

    // Create an authenticated API client
    CloudTasks cloudTasksClient = Common.authenticate();

    CloudTasksPullQueueSnippets example =
        new CloudTasksPullQueueSnippets(cloudTasksClient, projectId, locationId);
    try {
      List<Queue> queues = example.listQueues();
      os.println("Printing queues for location " + locationId);
      for (Queue q : queues) {
        os.println(q.getName());
      }
      Queue queue = new Queue().setName(queueName);
      Task task = example.createTask(queue);
      os.println("Created task " + task.getName());
      task = example.pullTask(queue);
      os.println("Pulled task " + task.getName());
      example.acknowledgeTask(task);
      os.println("Acknowledged task " + task.getName());
    } catch (IOException e) {
      System.err.println("Caught IO exception while running samples " + e.getMessage());
    }
  }

  /**
   * Main function runner.
   *
   * @param args Command line args
   * @throws Exception On network error.
   */
  public static void main(String[] args) throws Exception {
    runner(args, System.out);
  }
}
