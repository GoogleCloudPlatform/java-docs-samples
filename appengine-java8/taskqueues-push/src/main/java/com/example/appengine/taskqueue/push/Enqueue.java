/* Copyright 2016 Google Inc.
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

package com.example.appengine.taskqueue.push;

// [START import]
import com.google.cloud.tasks.v2.AppEngineHttpRequest;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.HttpMethod;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.Task;
// [END import]
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START enqueue]
// The Enqueue servlet should be mapped to the "/enqueue" URL.
// With @WebServlet annotation the webapp/WEB-INF/web.xml is no longer required.
@WebServlet(
    name = "TaskEnque",
    description = "taskqueue: Enqueue a job with a key",
    urlPatterns = "/taskqueues/enqueue")
public class Enqueue extends HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String key = request.getParameter("key");

    // Add the task to the default queue.
    // [START addQueue]
    try (CloudTasksClient client = CloudTasksClient.create()) {
      String projectId = "starter-akitsch";
      String locationId = "us-central1";
      // String param = "key-test";

      // Construct the fully qualified queue name.
      String queueName = QueueName.of(projectId, locationId, "default").toString();

      // Construct the task body.
      Task task =
          Task.newBuilder()
              .setAppEngineHttpRequest(
                  AppEngineHttpRequest.newBuilder()
                      .setRelativeUri("/taskqueues/worker?key=" + key)
                      .setHttpMethod(HttpMethod.POST)
                      .build())
              .build();

      Task taskResponse = client.createTask(queueName, task);
      System.out.println(taskResponse);
      // [END addQueue]
    }

    response.sendRedirect("/");
  }
}
// [END enqueue]
