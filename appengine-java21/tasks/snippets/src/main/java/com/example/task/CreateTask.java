/*
 * Copyright 2019 Google LLC
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

package com.example.task;

// [START cloud_tasks_taskqueues_new_task]
import com.google.cloud.tasks.v2.AppEngineHttpRequest;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.HttpMethod;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.Task;
import com.google.protobuf.ByteString;
import java.nio.charset.Charset;

public class CreateTask {
  public static void createTask(String projectId, String locationId, String queueId)
      throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      // TODO(developer): Uncomment these lines and replace with your values.
      // String projectId = "your-project-id";
      // String locationId = "us-central1";
      // String queueId = "default";
      String key = "key";

      // Construct the fully qualified queue name.
      String queueName = QueueName.of(projectId, locationId, queueId).toString();

      // Construct the task body.
      Task taskParam =
          Task.newBuilder()
              .setAppEngineHttpRequest(
                  AppEngineHttpRequest.newBuilder()
                      .setRelativeUri("/worker?key=" + key)
                      .setHttpMethod(HttpMethod.GET)
                      .build())
              .build();

      Task taskPayload =
          Task.newBuilder()
              .setAppEngineHttpRequest(
                  AppEngineHttpRequest.newBuilder()
                      .setBody(ByteString.copyFrom(key, Charset.defaultCharset()))
                      .setRelativeUri("/worker")
                      .setHttpMethod(HttpMethod.POST)
                      .build())
              .build();

      // Send create task request.
      Task[] tasks = new Task[] {taskParam, taskPayload};
      for (Task task : tasks) {
        Task response = client.createTask(queueName, task);
        System.out.println(response);
      }
    }
  }
}
// [END cloud_tasks_taskqueues_new_task]
