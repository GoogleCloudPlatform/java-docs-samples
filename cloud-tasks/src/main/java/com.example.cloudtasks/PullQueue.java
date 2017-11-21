/**
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

import com.google.api.services.cloudtasks.v2beta2.CloudTasks;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudtasks.v2beta2.CloudTasksScopes;
import com.google.api.services.cloudtasks.v2beta2.model.AcknowledgeTaskRequest;
import com.google.api.services.cloudtasks.v2beta2.model.CreateTaskRequest;
import com.google.api.services.cloudtasks.v2beta2.model.PullTaskTarget;
import com.google.api.services.cloudtasks.v2beta2.model.PullTasksRequest;
import com.google.api.services.cloudtasks.v2beta2.model.PullTasksResponse;
import com.google.api.services.cloudtasks.v2beta2.model.Task;
import com.google.common.io.BaseEncoding;
import java.io.IOException;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;


public class PullQueue {

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

  /**
   * Create a task for a given queue with a given payload.
   */
  private static Task createTask(
      String project, String location, String queue) throws IOException {
    // The name of the queue to use
    String queueName = String.format(
        "projects/%s/locations/%s/queues/%s", project, location, queue);

    // Create the Cloud Tasks Client
    CloudTasks client = createAuthorizedClient();

    // Create the Task to put in the Queue
    String message = "a message for the recipient";
    String payload = BaseEncoding.base64().encode(message.getBytes());
    Task task = new Task().setPullTaskTarget(new PullTaskTarget().setPayload(payload));

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
    System.out.println(String.format("Created task %s",task.getName()));
    return result;
  }

  /**
   * Pull a single task from a given queue and lease it for 10 minutes.
   */
  private static Task pullTask(
      String project, String location, String queue) throws IOException {
    // The name of the queue to use
    String queueName = String.format(
        "projects/%s/locations/%s/queues/%s", project, location, queue);

    // Create the Cloud Tasks Client
    CloudTasks client = createAuthorizedClient();

    // Create the PullTasksRequest
    PullTasksRequest request = new PullTasksRequest().setMaxTasks(1).setLeaseDuration("600s");

    //Execute the request and return the pulled task
    PullTasksResponse response = client
        .projects()
        .locations()
        .queues()
        .tasks()
        .pull(queueName, request)
        .execute();
    return response.getTasks().get(0);
  }

  /**
   * Acknowledge a given task, which removes it from the queue.
   */
  private static void acknowledgeTask(Task task) throws IOException{
    // Create the Cloud Tasks Client
    CloudTasks client = createAuthorizedClient();

    // Create the AcknowledgeTaskRequest
    AcknowledgeTaskRequest request = new AcknowledgeTaskRequest()
        .setScheduleTime(task.getScheduleTime());

    //Execute the request
    client
        .projects()
        .locations()
        .queues()
        .tasks()
        .acknowledge(task.getName(), request)
        .execute();
    System.out.println(String.format("Acknowledged task %s", task.getName()));
  }

  public static void main(String[] args) throws Exception {
    ArgumentParser parser = ArgumentParsers.newFor("PullQueue").build()
        .defaultHelp(true)
        .description("Sample command-line program for interacting with the Cloud Tasks API.\n\n"
            + "See README.md for instructions on setting up your development environment "
            + "and running the scripts.");

    Subparsers subparsers = parser.addSubparsers().dest("command");

    // Create the parser for the command 'create-task'
    ArgumentParser createTaskParser = subparsers
        .addParser("create-task")
        .help("Acknowledge a given task, which removes it from the queue.");
    createTaskParser
        .addArgument("--project")
        .help("Project of the queue to add the task to.")
        .required(true);
    createTaskParser
        .addArgument("--location")
        .help("Location of the queue to add the task to.")
        .required(true);
    createTaskParser
        .addArgument("--queue")
        .help("ID (short name) of the queue to add the task to.")
        .required(true);

    // Create the parser for the command 'pull-and-ack-task'
    ArgumentParser pullAndAckParser = subparsers
        .addParser("pull-and-ack-task")
        .help("Create a task for a given queue with an arbitrary payload.");
    pullAndAckParser
        .addArgument("--project")
        .help("Project of the queue to add the task to.")
        .required(true);
    pullAndAckParser
        .addArgument("--location")
        .help("Location of the queue to add the task to.")
        .required(true);
    pullAndAckParser
        .addArgument("--queue")
        .help("ID (short name) of the queue to add the task to.")
        .required(true);

    // Parse commands
    Namespace cmd = parser.parseArgs(args);

    String command = cmd.get("command");
    String project = cmd.get("project");
    String queue = cmd.get("queue");
    String location = cmd.get("location");

    // Execute commands
    if(command.equals("create-task")){
      createTask(project, location, queue);
    }
    if(command.equals("pull-and-ask-task")){
      Task task = pullTask(project, location, queue);
      acknowledgeTask(task);
    }
  }

}