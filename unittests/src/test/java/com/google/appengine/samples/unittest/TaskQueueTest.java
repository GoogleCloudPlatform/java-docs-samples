package com.google.appengine.samples.unittest;

// [START taskqueue_example_1]
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TaskQueueTest {

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalTaskQueueTestConfig());

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
        QueueFactory.getDefaultQueue().add(TaskOptions.Builder.withTaskName("task29"));
        // give the task time to execute if tasks are actually enabled (which they
        // aren't, but that's part of the test)
        Thread.sleep(1000);
        LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
        QueueStateInfo qsi = ltq.getQueueStateInfo().get(QueueFactory.getDefaultQueue()
                .getQueueName());
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
// [END taskqueue_example_1]
