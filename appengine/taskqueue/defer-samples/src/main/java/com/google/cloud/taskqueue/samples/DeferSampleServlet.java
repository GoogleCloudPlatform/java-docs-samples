/*
 * Copyright 2015 Google Inc.
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

/**
 * This package demonstrates how to use the task queue with Java.
 */

package com.google.cloud.taskqueue.samples;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This small servlet demonstrates how to use the DeferredTask
 * interface to background a task on the AppEngine task queues,
 * without needing to create a separate URL handler.
 */
public class DeferSampleServlet extends HttpServlet {

  /**
   * Number of ms long we will arbitrarily delay.
   */
  static final int DELAY_MS = 5000;

  //[START defer]
  /**
   * A hypothetical expensive operation we want to defer on a background task.
   */
  public static class ExpensiveOperation implements DeferredTask {
    @Override
    public void run() {
      System.out.println("Doing an expensive operation...");
      // expensive operation to be backgrounded goes here
    }
  }

  /**
   * Basic demonstration of adding a deferred task.
   * @param request servlet request
   * @param resp servlet response
   */
  @Override
  public void doGet(final HttpServletRequest request,
      final HttpServletResponse resp) throws IOException {
    // Add the task to the default queue.
    Queue queue = QueueFactory.getDefaultQueue();

    // Wait 5 seconds to run for demonstration purposes
    queue.add(TaskOptions.Builder.withPayload(new ExpensiveOperation())
        .etaMillis(System.currentTimeMillis() + DELAY_MS));

    resp.setContentType("text/plain");
    resp.getWriter().println("Task is backgrounded on queue!");
  }
  //[END defer]

}
