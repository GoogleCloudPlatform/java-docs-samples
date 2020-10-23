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

// [START cloud_tasks_taskqueues_processing_rate]
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.LocationName;
import com.google.cloud.tasks.v2.Queue;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.RateLimits;
import com.google.cloud.tasks.v2.UpdateQueueRequest;

public class UpdateQueue {
  public static void updateQueue(String projectId, String locationId, String queueId)
      throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      // TODO(developer): Uncomment these lines and replace with your values.
      // String projectId = "your-project-id";
      // String locationId = "us-central1";
      // String queueId = "queue-blue";

      LocationName parent = LocationName.of(projectId, locationId);

      Queue queueBlue =
          Queue.newBuilder()
              .setName(QueueName.of(projectId, locationId, queueId).toString())
              .setRateLimits(
                  RateLimits.newBuilder()
                      .setMaxDispatchesPerSecond(20.0)
                      .setMaxConcurrentDispatches(10))
              .build();

      UpdateQueueRequest request = UpdateQueueRequest.newBuilder().setQueue(queueBlue).build();

      Queue response = client.updateQueue(request);
      System.out.println(response);
    }
  }
}
// [END cloud_tasks_taskqueues_processing_rate]
