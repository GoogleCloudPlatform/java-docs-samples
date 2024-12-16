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

//  [START cloud_tasks_taskqueues_using_yaml]
import com.google.cloud.tasks.v2.AppEngineRouting;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.LocationName;
import com.google.cloud.tasks.v2.Queue;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.RateLimits;

public class CreateQueue {
  public static void createQueue(
      String projectId, String locationId, String queueBlueName, String queueRedName)
      throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      // TODO(developer): Uncomment these lines and replace with your values.
      // String projectId = "your-project-id";
      // String locationId = "us-central1";
      // String queueBlueName = "queue-blue";
      // String queueRedName = "queue-red";

      LocationName parent = LocationName.of(projectId, locationId);

      Queue queueBlue =
          Queue.newBuilder()
              .setName(QueueName.of(projectId, locationId, queueBlueName).toString())
              .setRateLimits(RateLimits.newBuilder().setMaxDispatchesPerSecond(5.0))
              .setAppEngineRoutingOverride(
                  AppEngineRouting.newBuilder().setVersion("v2").setService("task-module"))
              .build();

      Queue queueRed =
          Queue.newBuilder()
              .setName(QueueName.of(projectId, locationId, queueRedName).toString())
              .setRateLimits(RateLimits.newBuilder().setMaxDispatchesPerSecond(1.0))
              .build();

      Queue[] queues = new Queue[] {queueBlue, queueRed};
      for (Queue queue : queues) {
        Queue response = client.createQueue(parent, queue);
        System.out.println(response);
      }
    }
  }
}
//  [END cloud_tasks_taskqueues_using_yaml]
