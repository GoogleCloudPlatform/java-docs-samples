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

package com.example.taskqueue;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Form Handling Servlet -- takes the form submission from /src/main/webapp/taskqueues-pull.jsp to
 * add and delete tasks.
 */
// With @WebServlet annotation the webapp/WEB-INF/web.xml is no longer required.
@WebServlet(
    name = "TaskPull",
    description = "TaskQueues: Process some queues",
    urlPatterns = "/taskqueues/queue"
)
public class TaskqueueServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(TaskqueueServlet.class.getName());
  private static final int numberOfTasksToAdd = 100;
  private static final int numberOfTasksToLease = 100;
  private static boolean useTaggedTasks = true;
  private static String output;
  private static String message;

  // Process the http POST of the form
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    if (req.getParameter("addTask") != null) {
      String content = req.getParameter("content");
      String output =
          String.format(
              "Adding %d Tasks to the Task Queue with a payload of '%s'",
              numberOfTasksToAdd, content.toString());
      log.info(output.toString());

      // Add Tasks to Task Queue
      // [START get_queue]
      Queue q = QueueFactory.getQueue("pull-queue");
      // [END get_queue]
      if (!useTaggedTasks) {
        for (int i = 0; i < numberOfTasksToAdd; i++) {
          // [START add_task]
          q.add(
              TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).payload(content.toString()));
          // [END add_task]
        }
      } else {
        for (int i = 0; i < numberOfTasksToAdd; i++) {
          // [START add_task_w_tag]
          q.add(
              TaskOptions.Builder.withMethod(TaskOptions.Method.PULL)
                  .payload(content.toString())
                  .tag("process".getBytes()));
          // [END add_task_w_tag]
        }
      }
      try {
        message = "Added " + numberOfTasksToAdd + " tasks to the task queue.";
        req.setAttribute("message", message);
        req.getRequestDispatcher("taskqueues-pull.jsp").forward(req, resp);
      } catch (ServletException e) {
        throw new ServletException("ServletException error: ", e);
      }
    } else {
      if (req.getParameter("leaseTask") != null) {
        output = String.format("Pulling %d Tasks from the Task Queue", numberOfTasksToLease);
        log.info(output.toString());

        // Pull tasks from the Task Queue and process them
        Queue q = QueueFactory.getQueue("pull-queue");
        if (!useTaggedTasks) {
          // [START lease_tasks]
          List<TaskHandle> tasks = q.leaseTasks(3600, TimeUnit.SECONDS, numberOfTasksToLease);
          // [END lease_tasks]
          message = processTasks(tasks, q);
        } else {
          // [START lease_tasks_by_tag]
          // Lease only tasks tagged with "process"
          List<TaskHandle> tasks =
              q.leaseTasksByTag(3600, TimeUnit.SECONDS, numberOfTasksToLease, "process");
          // You can also specify a tag to lease via LeaseOptions passed to leaseTasks.
          // [END lease_tasks_by_tag]
          message = processTasks(tasks, q);
        }
        req.setAttribute("message", message);
        req.getRequestDispatcher("taskqueues-pull.jsp").forward(req, resp);
      } else {
        resp.sendRedirect("/");
      }
    }
  }

  //Method to process and delete tasks
  private static String processTasks(List<TaskHandle> tasks, Queue q) {
    String payload;
    int numberOfDeletedTasks = 0;
    for (TaskHandle task : tasks) {
      payload = new String(task.getPayload());
      output =
          String.format(
              "Processing: taskName='%s'  payload='%s'",
              task.getName().toString(), payload.toString());
      log.info(output.toString());
      output = String.format("Deleting taskName='%s'", task.getName().toString());
      log.info(output.toString());
      // [START delete_task]
      q.deleteTask(task);
      // [END delete_task]
      numberOfDeletedTasks++;
    }
    if (numberOfDeletedTasks > 0) {
      message =
          "Processed and deleted " + numberOfTasksToLease + " tasks from the " + " task queue.";
    } else {
      message = "Task Queue has no tasks available for lease.";
    }
    return message;
  }
}
