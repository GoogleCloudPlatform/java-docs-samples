/*
 * Copyright 2017 Google Inc.
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

package com.example.cloudtasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START cloud_tasks_appengine_handler]
@WebServlet(value = "/example_task_handler")
@SuppressWarnings("serial")
public class TaskHandlerServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    BufferedReader br = req.getReader();
    StringBuffer dataBuffer = new StringBuffer();
    while (br.ready()) {
      dataBuffer.append(br.readLine());
    }
    String data = dataBuffer.toString();

    System.out.println(String.format("Received task with payload: %s", data));

    PrintWriter out = resp.getWriter();
    out.println(String.format("Received task with payload: %s", data));
  }

}
// [END cloud_tasks_appengine_handler]
