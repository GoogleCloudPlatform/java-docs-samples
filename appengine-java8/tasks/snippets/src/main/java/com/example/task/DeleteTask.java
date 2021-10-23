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

// [START cloud_tasks_taskqueues_deleting_tasks]
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.TaskName;

public class DeleteTask {
  public static void deleteTask(String projectId, String locationId, String queueId, String taskId)
      throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      // TODO(developer): Uncomment these lines and replace with your values.
      // String projectId = "your-project-id";
      // String locationId = "us-central1";
      // String queueId = "queue1";
      // String taskId = "foo";

      // Construct the fully qualified queue name.
      String taskName = TaskName.of(projectId, locationId, queueId, taskId).toString();

      client.deleteTask(taskName);
      System.out.println("Task Deleted.");
    }
  }
}
// [END cloud_tasks_taskqueues_deleting_tasks]
