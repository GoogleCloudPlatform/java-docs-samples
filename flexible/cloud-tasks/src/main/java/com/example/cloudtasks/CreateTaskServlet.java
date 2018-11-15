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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudtasks.v2beta3.CloudTasks;
import com.google.api.services.cloudtasks.v2beta3.CloudTasksScopes;
import com.google.api.services.cloudtasks.v2beta3.model.AppEngineHttpRequest;
import com.google.api.services.cloudtasks.v2beta3.model.CreateTaskRequest;
import com.google.api.services.cloudtasks.v2beta3.model.Task;
import com.google.common.io.BaseEncoding;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = "/create_task")
@SuppressWarnings("serial")
public class CreateTaskServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    String message = req.getParameter("message");
    if (message == null) {
      message = "Hello World!";
    }

    String project = req.getParameter("project");
    String location = req.getParameter("location");
    String queueId = req.getParameter("queue");

    createTask(project, location, queueId, message);

    PrintWriter out = resp.getWriter();
    out.println(String.format("Created a task with the following message: %s", message));
  }

  /**
   * Creates an authorized CloudTasks client service using Application Default Credentials.
   *
   * @return an authorized CloudTasks client
   * @throws IOException if there's an error getting the default credentials.
   */
  private static CloudTasks createAuthorizedClient() throws IOException {
    // Create the credential
    HttpTransport transport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();
    // Authorize the client using Application Default Credentials
    // @see https://g.co/dv/identity/protocols/application-default-credentials
    GoogleCredential credential = GoogleCredential.getApplicationDefault(transport, jsonFactory);

    // Depending on the environment that provides the default credentials (e.g. Compute Engine, App
    // Engine), the credentials may require us to specify the scopes we need explicitly.
    // Check for this case, and inject the scope if required.
    if (credential.createScopedRequired()) {
      credential = credential.createScoped(CloudTasksScopes.all());
    }

    return new CloudTasks.Builder(transport, jsonFactory, credential)
        .setApplicationName("Cloud Tasks Snippets")
        .build();
  }

  // [START cloud_tasks_appengine_create_task]
  /**
   * Create a task for a given queue with a given payload.
   */
  private static Task createTask(
      String project, String location, String queue, String payload) throws IOException {
    // The name of the queue to use
    String queueName = String.format(
        "projects/%s/locations/%s/queues/%s", project, location, queue);

    // Create the Cloud Tasks Client
    CloudTasks client = createAuthorizedClient();

    // Create the Task to put in the Queue
    payload = BaseEncoding.base64().encode(payload.getBytes());
    AppEngineHttpRequest postRequest = new AppEngineHttpRequest()
        .setHttpMethod("POST")
        .setRelativeUri("/example_task_handler")
        .setBody(payload);
    Task task = new Task().setAppEngineHttpRequest(postRequest);

    // Create the CreateTaskRequest
    CreateTaskRequest request = new CreateTaskRequest().setTask(task);

    //Execute the request and return the created Task
    Task result = client
        .projects()
        .locations()
        .queues()
        .tasks()
        .create(queueName, request)
        .execute();
    System.out.println(String.format("Created task %s", task.getName()));
    return result;
  }

}
  // [END cloud_tasks_appengine_create_task]
