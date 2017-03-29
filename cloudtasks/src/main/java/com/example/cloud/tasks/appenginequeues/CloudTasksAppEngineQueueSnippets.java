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

package com.example.cloud.tasks.appenginequeues;

import com.example.cloud.tasks.Common;

import com.google.api.services.cloudtasks.v2beta2.CloudTasks;
import com.google.api.services.cloudtasks.v2beta2.model.AppEngineTaskTarget;
import com.google.api.services.cloudtasks.v2beta2.model.CreateTaskRequest;
import com.google.api.services.cloudtasks.v2beta2.model.Task;
import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;

import java.io.IOException;
import java.io.PrintStream;

/** Snippets for creating Tasks with App Engine queue targets. */
public class CloudTasksAppEngineQueueSnippets {

  /** Authenticated HTTP client for the Cloud Task API . */
  private final CloudTasks client;

  /**
   * Construct the snippets object.
   *
   * @param client Authenticated Cloud Tasks API client
   */
  public CloudTasksAppEngineQueueSnippets(CloudTasks client) {
    this.client = client;
  }

  /**
   * Create a Task with an App Engine target and a payload.
   *
   * <p>This creates an App Engine task that will target the queue provided by the queueName
   * argument, targeting the `/payload` URL, and attaching the `payload` argument as HTTP post data.
   *
   * @param queueName The fully qualified queue name to create the task on.
   * @param payload The payload to attach as HTTP post data.
   */
  public Task createAppEngineTask(String queueName, String payload) throws IOException {
    String encodedPayload = BaseEncoding.base64().encode(payload.getBytes(Charsets.US_ASCII));

    AppEngineTaskTarget aeTarget =
        new AppEngineTaskTarget()
            .setHttpMethod("POST")
            .setRelativeUrl("/payload")
            .setPayload(encodedPayload);
    Task task = new Task().setAppEngineTaskTarget(aeTarget);
    CreateTaskRequest taskRequest = new CreateTaskRequest().setTask(task);
    Task result =
        this.client
            .projects()
            .locations()
            .queues()
            .tasks()
            .create(queueName, taskRequest)
            .execute();
    return result;
  }

  /**
   * Main runner that prints results to output stream to improve testability.
   *
   * @param os Output stream
   */
  public static void runner(String[] args, PrintStream os) throws Exception {
    if (args.length != 2) {
      System.err.println(
          String.format(
              "Usage: %s <queue_name> <payload>",
              CloudTasksAppEngineQueueSnippets.class.getSimpleName()));
      return;
    }
    String queueName = args[0];
    String payload = args[1];

    // Create an authenticated API client
    CloudTasks cloudTasksClient = Common.authenticate();

    CloudTasksAppEngineQueueSnippets example =
        new CloudTasksAppEngineQueueSnippets(cloudTasksClient);
    try {
      Task task = example.createAppEngineTask(queueName, payload);
      os.println("Created task " + task);
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
