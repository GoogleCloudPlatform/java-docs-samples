/* Copyright 2019 Google LLC
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

// [START cloud_tasks_enqueue]
import com.google.cloud.tasks.v2.AppEngineHttpRequest;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.HttpMethod;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.Task;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
    name = "TaskEnqueue",
    description = "Enqueue a task targeted at endpoint '/cloudtasks/worker'",
    urlPatterns = "/cloudtasks/enqueue")
public class Enqueue extends HttpServlet {

  // TODO(developer): Replace these variables before running the sample.
  static final String projectId = "my-project-id";
  static final String locationId = "us-central1";

  // Function creates Cloud Tasks from form submissions.
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String key = request.getParameter("key");

    try (CloudTasksClient client = CloudTasksClient.create()) {
      // Construct the fully qualified queue name.
      String queueName = QueueName.of(projectId, locationId, "default").toString();

      // Construct the task body.
      Task task =
          Task.newBuilder()
              .setAppEngineHttpRequest(
                  AppEngineHttpRequest.newBuilder()
                      .setRelativeUri("/cloudtasks/worker?key=" + key)
                      .setHttpMethod(HttpMethod.POST)
                      .build())
              .build();

      // Add the task to the default queue.
      Task taskResponse = client.createTask(queueName, task);
      System.out.println("Task created: " + taskResponse.getName());
    }

    response.sendRedirect("/");
  }
}
// [END cloud_tasks_enqueue]
