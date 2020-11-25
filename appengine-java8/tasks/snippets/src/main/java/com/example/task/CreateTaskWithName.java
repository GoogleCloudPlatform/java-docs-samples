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

// [START cloud_tasks_taskqueues_naming_tasks]
import com.google.cloud.tasks.v2.AppEngineHttpRequest;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.HttpMethod;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.Task;
import com.google.cloud.tasks.v2.TaskName;

public class CreateTaskWithName {
  public static void createTaskWithName(
      String projectId, String locationId, String queueId, String taskId) throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      // TODO(developer): Uncomment these lines and replace with your values.
      // String projectId = "your-project-id";
      // String locationId = "us-central1";
      // String queueId = "default";
      // String taskId = "first-try"

      String queueName = QueueName.of(projectId, locationId, queueId).toString();

      Task.Builder taskBuilder =
          Task.newBuilder()
              .setName(TaskName.of(projectId, locationId, queueId, taskId).toString())
              .setAppEngineHttpRequest(
                  AppEngineHttpRequest.newBuilder()
                      .setRelativeUri("/worker")
                      .setHttpMethod(HttpMethod.GET)
                      .build());

      // Send create task request.
      Task response = client.createTask(queueName, taskBuilder.build());
      System.out.println(response);
    }
  }
}
// [END cloud_tasks_taskqueues_naming_tasks]
