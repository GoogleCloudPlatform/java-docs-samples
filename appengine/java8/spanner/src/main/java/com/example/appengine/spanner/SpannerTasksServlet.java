/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appengine.spanner;

import com.example.appengine.spanner.SpannerTasks.Task;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Example code for using the Cloud Spanner API. This example demonstrates all the common operations
 * that can be done on Cloud Spanner. These are:
 *
 * <p><ul><li>Creating a Cloud Spanner database. <li>Writing, reading and executing SQL queries.
 * <li>Writing data using a read-write transaction. <li>Using an index to read and execute SQL
 * queries over data. </ul>
 *
 * <p></p>Individual tasks can be run using "tasks" query parameter. {@link SpannerTasks.Task} lists
 * supported tasks. All tasks are run in order if no parameter or "tasks=all" is provided.
 */
// With @WebServlet annotation the webapp/WEB-INF/web.xml is no longer required.
@WebServlet(value = "/spanner")
public class SpannerTasksServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text");
    PrintWriter pw = resp.getWriter();
    try {
      String tasksParam = req.getParameter("tasks");
      List<Task> tasks;
      if (tasksParam == null || tasksParam.equals("all")) {
        // cycle through all operations in order
        tasks = Arrays.asList(Task.values());
      } else {
        String[] tasksStr = tasksParam.split(",");
        tasks = Arrays.stream(tasksStr).map(Task::valueOf).collect(Collectors.toList());
      }

      for (Task task : tasks) {
        SpannerTasks.runTask(task, pw);
      }
    } catch (Exception e) {
      e.printStackTrace(pw);
      pw.append(e.getMessage());
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
