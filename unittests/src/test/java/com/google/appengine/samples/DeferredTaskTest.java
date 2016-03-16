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

// [START DeferredTaskTest]

import static org.junit.Assert.assertTrue;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class DeferredTaskTest {

  // Unlike CountDownLatch, TaskCountDownlatch lets us reset.
  private final LocalTaskQueueTestConfig.TaskCountDownLatch latch =
      new LocalTaskQueueTestConfig.TaskCountDownLatch(1);

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalTaskQueueTestConfig()
          .setDisableAutoTaskExecution(false)
          .setCallbackClass(LocalTaskQueueTestConfig.DeferredTaskCallback.class)
          .setTaskExecutionLatch(latch));

  private static class MyTask implements DeferredTask {
    private static boolean taskRan = false;

    @Override
    public void run() {
      taskRan = true;
    }
  }

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    MyTask.taskRan = false;
    latch.reset();
    helper.tearDown();
  }

  @Test
  public void testTaskGetsRun() throws InterruptedException {
    QueueFactory.getDefaultQueue().add(
        TaskOptions.Builder.withPayload(new MyTask()));
    assertTrue(latch.await(5, TimeUnit.SECONDS));
    assertTrue(MyTask.taskRan);
  }
}
// [END DeferredTaskTest]
