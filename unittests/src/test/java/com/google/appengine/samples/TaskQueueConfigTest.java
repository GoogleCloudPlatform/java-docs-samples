/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.appengine.samples;

import static org.junit.Assert.assertEquals;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TaskQueueConfigTest {
  // [START LocalServiceTestHelper]
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalTaskQueueTestConfig()
          .setQueueXmlPath("src/main/webapp/WEB-INF/queue.xml"));
  //[END LocalServiceTestHelper]

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // Run this test twice to demonstrate we're not leaking state across tests.
  // If we _are_ leaking state across tests we'll get an exception on the
  // second test because there will already be a task with the given name.
  private void doTest() throws InterruptedException {
    // [START QueueFactory]
    QueueFactory.getQueue("my-queue-name").add(TaskOptions.Builder.withTaskName("task29"));
    // [END QueueFactory]
    // Give the task time to execute if tasks are actually enabled (which they
    // aren't, but that's part of the test).
    Thread.sleep(1000);
    LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
    QueueStateInfo qsi =
        ltq.getQueueStateInfo().get(QueueFactory.getQueue("my-queue-name").getQueueName());
    assertEquals(1, qsi.getTaskInfo().size());
    assertEquals("task29", qsi.getTaskInfo().get(0).getTaskName());
  }

  @Test
  public void testTaskGetsScheduled1() throws InterruptedException {
    doTest();
  }

  @Test
  public void testTaskGetsScheduled2() throws InterruptedException {
    doTest();
  }
}
