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

// [START cloud_tasks_taskqueues_retrying_tasks]
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.LocationName;
import com.google.cloud.tasks.v2.Queue;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.RateLimits;
import com.google.cloud.tasks.v2.RetryConfig;
import com.google.protobuf.Duration;

public class RetryTask {
  public static void retryTask(
      String projectId, String locationId, String fooqueue, String barqueue, String bazqueue)
      throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      // TODO(developer): Uncomment these lines and replace with your values.
      // String projectId = "your-project-id";
      // String locationId = "us-central1";
      // String fooqueue = "fooqueue";
      // String barqueue = "barqueue";
      // String bazqueue = "bazqueue";

      LocationName parent = LocationName.of(projectId, locationId);

      Duration retryDuration = Duration.newBuilder().setSeconds(2 * 60 * 60 * 24).build();
      Duration min = Duration.newBuilder().setSeconds(10).build();
      Duration max1 = Duration.newBuilder().setSeconds(200).build();
      Duration max2 = Duration.newBuilder().setSeconds(300).build();

      Queue foo =
          Queue.newBuilder()
              .setName(QueueName.of(projectId, locationId, fooqueue).toString())
              .setRateLimits(RateLimits.newBuilder().setMaxDispatchesPerSecond(1.0))
              .setRetryConfig(
                  RetryConfig.newBuilder().setMaxAttempts(7).setMaxRetryDuration(retryDuration))
              .build();

      Queue bar =
          Queue.newBuilder()
              .setName(QueueName.of(projectId, locationId, barqueue).toString())
              .setRateLimits(RateLimits.newBuilder().setMaxDispatchesPerSecond(1.0))
              .setRetryConfig(
                  RetryConfig.newBuilder()
                      .setMinBackoff(min)
                      .setMaxBackoff(max1)
                      .setMaxDoublings(0))
              .build();

      Queue baz =
          Queue.newBuilder()
              .setName(QueueName.of(projectId, locationId, bazqueue).toString())
              .setRateLimits(RateLimits.newBuilder().setMaxDispatchesPerSecond(1.0))
              .setRetryConfig(
                  RetryConfig.newBuilder()
                      .setMinBackoff(min)
                      .setMaxBackoff(max2)
                      .setMaxDoublings(3))
              .build();

      Queue[] queues = new Queue[] {foo, bar, baz};
      for (Queue queue : queues) {
        Queue response = client.createQueue(parent, queue);
        System.out.println(response);
      }
    }
  }
}
// [END cloud_tasks_taskqueues_retrying_tasks]
