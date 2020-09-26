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

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/** Tests for creating Tasks with HTTP targets. */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class SnippetsIT {
  private static final String PROJECT_ID = "java-docs-samples-testing";
  private static final String LOCATION_ID = "us-east1";
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private static final String QUEUE_NAME_1 = UUID.randomUUID().toString();
  private static final String QUEUE_NAME_2 = UUID.randomUUID().toString();

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void test1CreateQueues() throws Exception {
    String name =
        String.format("projects/%s/locations/%s/queues/%s", PROJECT_ID, LOCATION_ID, QUEUE_NAME_2);
    System.out.println(QUEUE_NAME_1);
    CreateQueue.createQueue(PROJECT_ID, LOCATION_ID, QUEUE_NAME_1, QUEUE_NAME_2);
    String got = bout.toString();
    assertThat(got).contains(name);
  }

  @Test
  public void test2UpdateQueue() throws Exception {
    String name =
        String.format("projects/%s/locations/%s/queues/%s", PROJECT_ID, LOCATION_ID, QUEUE_NAME_1);
    UpdateQueue.updateQueue(PROJECT_ID, LOCATION_ID, QUEUE_NAME_1);
    String got = bout.toString();
    assertThat(got).contains(name);
  }

  @Test
  public void test3CreateTask() throws Exception {
    String name =
        String.format("projects/%s/locations/%s/queues/%s", PROJECT_ID, LOCATION_ID, QUEUE_NAME_1);
    System.out.println(QUEUE_NAME_1);
    CreateTask.createTask(PROJECT_ID, LOCATION_ID, QUEUE_NAME_1);
    String got = bout.toString();
    assertThat(got).contains(name);
  }

  @Test
  public void test4CreateTaskWithName() throws Exception {
    String name =
        String.format("projects/%s/locations/%s/queues/%s", PROJECT_ID, LOCATION_ID, QUEUE_NAME_1);
    CreateTaskWithName.createTaskWithName(PROJECT_ID, LOCATION_ID, QUEUE_NAME_1, "foo");
    String got = bout.toString();
    assertThat(got).contains(name);
  }

  @Test
  public void test5DeleteTask() throws Exception {
    String name =
        String.format("projects/%s/locations/%s/queues/%s", PROJECT_ID, LOCATION_ID, QUEUE_NAME_1);
    DeleteTask.deleteTask(PROJECT_ID, LOCATION_ID, QUEUE_NAME_1, "foo");
    String got = bout.toString();
    assertThat(got).contains("Task Deleted.");
  }

  @Test
  public void test6PurgeQueue() throws Exception {
    PurgeQueue.purgeQueue(PROJECT_ID, LOCATION_ID, QUEUE_NAME_1);
    String got = bout.toString();
    assertThat(got).contains("Queue Purged.");
  }

  @Test
  public void test7PauseQueue() throws Exception {
    String name =
        String.format("projects/%s/locations/%s/queues/%s", PROJECT_ID, LOCATION_ID, QUEUE_NAME_1);
    PauseQueue.pauseQueue(PROJECT_ID, LOCATION_ID, QUEUE_NAME_1);
    String got = bout.toString();
    assertThat(got).contains("Queue Paused.");
  }

  @Test
  public void test8DeleteQueue() throws Exception {
    DeleteQueue.deleteQueue(PROJECT_ID, LOCATION_ID, QUEUE_NAME_1);
    DeleteQueue.deleteQueue(PROJECT_ID, LOCATION_ID, QUEUE_NAME_2);
    String got = bout.toString();
    assertThat(got).contains("Queue Deleted.");
  }

  @Test
  public void test8RetryTasks() throws Exception {
    String queue1 = UUID.randomUUID().toString();
    String queue2 = UUID.randomUUID().toString();
    String queue3 = UUID.randomUUID().toString();

    String name =
        String.format("projects/%s/locations/%s/queues/%s", PROJECT_ID, LOCATION_ID, queue1);
    RetryTask.retryTask(PROJECT_ID, LOCATION_ID, queue1, queue2, queue3);
    String got = bout.toString();
    assertThat(got).contains(name);

    DeleteQueue.deleteQueue(PROJECT_ID, LOCATION_ID, queue1);
    DeleteQueue.deleteQueue(PROJECT_ID, LOCATION_ID, queue2);
    DeleteQueue.deleteQueue(PROJECT_ID, LOCATION_ID, queue3);
  }
}
