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

package com.example.appengine;

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

public class Snippets {
  //  [START taskqueues_using_yaml]
  public static void createQueues(
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
  //  [END taskqueues_using_yaml]

  // [START taskqueues_processing_rate]
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
  // [END taskqueues_processing_rate]

  // [START taskqueues_new_task]
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
  // [END taskqueues_new_task]

  // [START taskqueues_naming_tasks]
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
  // [END taskqueues_naming_tasks]

  // [START taskqueues_deleting_tasks]
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
  // [END taskqueues_deleting_tasks]

  // [START taskqueues_purging_tasks]
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
  // [END taskqueues_purging_tasks]

  // [START taskqueues_pause_queue]
  public static void pauseQueue(String projectId, String locationId, String queueId)
      throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      // TODO(developer): Uncomment these lines and replace with your values.
      // String projectId = "your-project-id";
      // String locationId = "us-central1";
      // String queueId = "foo";

      // Construct the fully qualified queue name.
      String queueName = QueueName.of(projectId, locationId, queueId).toString();

      client.pauseQueue(queueName);
      System.out.println("Queue Paused.");
    }
  }
  // [END taskqueues_pause_queue]

  // [START taskqueues_pause_queue]
  public static void deleteQueue(String projectId, String locationId, String queueId)
      throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      // TODO(developer): Uncomment these lines and replace with your values.
      // String projectId = "your-project-id";
      // String locationId = "us-central1";
      // String queueId = "foo";

      // Construct the fully qualified queue name.
      String queueName = QueueName.of(projectId, locationId, queueId).toString();

      client.deleteQueue(queueName);
      System.out.println("Queue Deleted.");
    }
  }
  // [END taskqueues_pause_queue]

  // [START taskqueues_retrying_tasks]
  public static void retryTasks(
      String projectId, String locationId, String fooqueue, String barqueue, String bazqueue)
      throws Exception {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      // TODO(developer): Uncomment these lines and replace with your values.
      // String projectId = "your-project-id";
      // String locationId = "us-central1";
      // String fooqueue = "fooqueue";
      // String barqueue = "barqueue";
      // String bazqueue = "bazqueue"

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
  // [END taskqueues_retrying_tasks]

  // Run sample: mvn clean package exec:java -Dexec.mainClass="com.example.appengine.Snippets"
  public static void main(String[] args) throws Exception {
    // createQueues();
    // updateQueue();
    // createTask();
    // createTaskWithName();
    // deleteTask();
    // purgeQueue();
    // pauseQueue();
    // deleteQueue();
    // retryTasks();
  }
}
