/*
 * Copyright 2018 Google LLC
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

public class Migration {
  //  [START taskqueues_using_yaml]
  private static void createQueues() throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      String projectId = "your-project-id";
      String locationId = "us-central1";

      LocationName parent = LocationName.of(projectId, locationId);

      Queue queueBlue =
          Queue.newBuilder()
              .setName(QueueName.of(projectId, locationId, "queue-blue").toString())
              .setRateLimits(RateLimits.newBuilder().setMaxDispatchesPerSecond(5.0))
              .setAppEngineRoutingOverride(
                  AppEngineRouting.newBuilder().setVersion("v2").setService("task-module"))
              .build();

      Queue queueRed =
          Queue.newBuilder()
              .setName(QueueName.of(projectId, locationId, "queue-red").toString())
              .setRateLimits(RateLimits.newBuilder().setMaxDispatchesPerSecond(1.0))
              .build();

      Queue[] queues = new Queue[] {queueBlue, queueRed};
      for (Queue queue : queues) {
        Queue response = client.createQueue(parent, queue);
        System.out.println(response);
      }
    }
  }
  //  [END taskqueues_using_yaml]

  // [START taskqueues_processing_rate]
  private static void updateQueue() throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      String projectId = "your-project-id";
      String locationId = "us-central1";

      LocationName parent = LocationName.of(projectId, locationId);

      Queue queueBlue =
          Queue.newBuilder()
              .setName(QueueName.of(projectId, locationId, "queue-blue").toString())
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
  // [END taskqueues_processing_rate]

  // [START taskqueues_new_task]
  private static void createTask() throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      String projectId = "your-project-id";
      String locationId = "us-central1";
      String key = "key";

      // Construct the fully qualified queue name.
      String queueName = QueueName.of(projectId, locationId, "default").toString();

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
  // [END taskqueues_new_task]

  // [START taskqueues_naming_tasks]
  private static void createTaskWithName() throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      String projectId = "your-project-id";
      String locationId = "us-central1";
      String queue = "default";
      String key = "key";

      // Construct the fully qualified queue name.
      String queueName = QueueName.of(projectId, locationId, queue).toString();

      // Construct the task body.
      Task.Builder taskBuilder =
          Task.newBuilder()
              .setName(TaskName.of(projectId, locationId, queue, "first-try").toString())
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
  // [END taskqueues_naming_tasks]

  // [START taskqueues_deleting_tasks]
  private static void deleteTask() throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      String projectId = "your-project-id";
      String locationId = "us-central1";
      String queue = "queue1";
      String task = "foo";

      // Construct the fully qualified queue name.
      String taskName = TaskName.of(projectId, locationId, queue, task).toString();

      client.deleteTask(taskName);
    }
  }
  // [END taskqueues_deleting_tasks]

  // [START taskqueues_purging_tasks]
  private static void purgeQueue() throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      String projectId = "your-project-id";
      String locationId = "us-central1";
      String queue = "foo";

      // Construct the fully qualified queue name.
      String queueName = QueueName.of(projectId, locationId, queue).toString();

      client.purgeQueue(queueName);
    }
  }
  // [END taskqueues_purging_tasks]

  // [START taskqueues_pause_queue]
  private static void pauseQueue() throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      String projectId = "your-project-id";
      String locationId = "us-central1";
      String queue = "foo";

      // Construct the fully qualified queue name.
      String queueName = QueueName.of(projectId, locationId, queue).toString();

      client.pauseQueue(queueName);
    }
  }
  // [END taskqueues_pause_queue]

  // [START taskqueues_pause_queue]
  private static void deleteQueue() throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      String projectId = "your-project-id";
      String locationId = "us-central1";
      String queue = "foo";

      // Construct the fully qualified queue name.
      String queueName = QueueName.of(projectId, locationId, queue).toString();

      client.pauseQueue(queueName);
    }
  }
  // [END taskqueues_pause_queue]

  // [START taskqueues_retrying_tasks]
  private static void retryTasks() throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      String projectId = "your-project-id";
      String locationId = "us-central1";

      LocationName parent = LocationName.of(projectId, locationId);

      Duration retryDuration = Duration.newBuilder().setSeconds(2 * 60 * 60 * 24).build();
      Duration min = Duration.newBuilder().setSeconds(10).build();
      Duration max1 = Duration.newBuilder().setSeconds(200).build();
      Duration max2 = Duration.newBuilder().setSeconds(300).build();

      Queue fooqueue =
          Queue.newBuilder()
              .setName(QueueName.of(projectId, locationId, "fooqueue").toString())
              .setRateLimits(RateLimits.newBuilder().setMaxDispatchesPerSecond(1.0))
              .setRetryConfig(
                  RetryConfig.newBuilder().setMaxAttempts(7).setMaxRetryDuration(retryDuration))
              .build();

      Queue barqueue =
          Queue.newBuilder()
              .setName(QueueName.of(projectId, locationId, "barqueue").toString())
              .setRateLimits(RateLimits.newBuilder().setMaxDispatchesPerSecond(1.0))
              .setRetryConfig(
                  RetryConfig.newBuilder()
                      .setMinBackoff(min)
                      .setMaxBackoff(max1)
                      .setMaxDoublings(0))
              .build();

      Queue bazqueue =
          Queue.newBuilder()
              .setName(QueueName.of(projectId, locationId, "bazqueue").toString())
              .setRateLimits(RateLimits.newBuilder().setMaxDispatchesPerSecond(1.0))
              .setRetryConfig(
                  RetryConfig.newBuilder()
                      .setMinBackoff(min)
                      .setMaxBackoff(max2)
                      .setMaxDoublings(3))
              .build();

      Queue[] queues = new Queue[] {fooqueue, barqueue, bazqueue};
      for (Queue queue : queues) {
        Queue response = client.createQueue(parent, queue);
        System.out.println(response);
      }
    }
  }
  // [END taskqueues_retrying_tasks]

  // Run sample: mvn clean package exec:java -Dexec.mainClass="com.example.task.Migration"
  public static void main(String[] args) throws Exception {
    createQueues();
    updateQueue();
    createTask();
    createTaskWithName();
    deleteTask();
    purgeQueue();
    pauseQueue();
    deleteQueue();
    retryTasks();
  }
}
