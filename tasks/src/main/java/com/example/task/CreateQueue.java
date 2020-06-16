// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.task;

// [START cloud_tasks_create_queue]
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.LocationName;
import com.google.cloud.tasks.v2.Queue;
import com.google.cloud.tasks.v2.QueueName;

public class CreateQueue {
  /**
   * Create a queue using the Cloud Tasks client.
   *
   * @param projectId the Id of the project.
   * @param queueId the name of your Queue.
   * @param locationId the GCP region of your queue.
   * @throws Exception on Cloud Tasks Client errors.
   */
  public static void createQueue(String projectId, String locationId, String queueId)
      throws Exception {

    // Instantiates a client.
    try (CloudTasksClient client = CloudTasksClient.create()) {

      // Construct the fully qualified location.
      String parent = LocationName.of(projectId, locationId).toString();

      // Construct the fully qualified queue path.
      String queuePath = QueueName.of(projectId, locationId, queueId).toString();

      // Send create queue request.
      Queue queue = client.createQueue(parent, Queue.newBuilder().setName(queuePath).build());

      System.out.println("Queue created: " + queue.getName());
    }
  }
}
// [END cloud_tasks_create_queue]
