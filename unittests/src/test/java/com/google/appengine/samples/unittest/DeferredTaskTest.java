package com.google.appengine.samples.unittest;

// [START taskqueue_example_2]
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

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
// [END taskqueue_example_2]
