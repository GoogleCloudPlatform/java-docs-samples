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

// [START taskqueues_purging_tasks]
import com.google.cloud.tasks.v2.AppEngineHttpRequest;
import com.google.cloud.tasks.v2.AppEngineRouting;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.HttpMethod;
import com.google.cloud.tasks.v2.LocationName;
import com.google.cloud.tasks.v2.Queue;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.RateLimits;
import com.google.cloud.tasks.v2.RetryConfig;
import com.google.cloud.tasks.v2.Task;
import com.google.cloud.tasks.v2.TaskName;
import com.google.cloud.tasks.v2.UpdateQueueRequest;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import java.nio.charset.Charset;

public class PurgeQueue {
  public static void purgeQueue(String projectId, String locationId, String queueId)
      throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      // TODO(developer): Uncomment these lines and replace with your values.
      // String projectId = "your-project-id";
      // String locationId = "us-central1";
      // String queueId = "foo";

      // Construct the fully qualified queue name.
      String queueName = QueueName.of(projectId, locationId, queueId).toString();

      client.purgeQueue(queueName);
      System.out.println("Queue Purged.");
    }
  }
}
// [END taskqueues_purging_tasks]
