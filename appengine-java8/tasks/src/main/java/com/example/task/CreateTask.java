/*
 * Copyright 2018 Google LLC
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

import com.google.cloud.tasks.v2beta3.AppEngineHttpRequest;
import com.google.cloud.tasks.v2beta3.CloudTasksClient;
import com.google.cloud.tasks.v2beta3.HttpMethod;
import com.google.cloud.tasks.v2beta3.QueueName;
import com.google.cloud.tasks.v2beta3.Task;
import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

import java.nio.charset.Charset;
import java.time.Clock;
import java.time.Instant;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CreateTask {
  private static String GOOGLE_CLOUD_PROJECT_KEY = "GOOGLE_CLOUD_PROJECT";

  private static Option PROJECT_ID_OPTION = Option.builder("pid")
      .longOpt("project-id")
      .desc("The Google Cloud Project, if not set as GOOGLE_CLOUD_PROJECT env var.")
      .hasArg()
      .argName("project-id")
      .type(String.class)
      .build();

  private static Option QUEUE_OPTION = Option.builder("q")
      .required()
      .longOpt("queue")
      .desc("The Cloud Tasks queue.")
      .hasArg()
      .argName("queue")
      .type(String.class)
      .build();

  private static Option LOCATION_OPTION = Option.builder("l")
      .required()
      .longOpt("location")
      .desc("The region in which your queue is running.")
      .hasArg()
      .argName("location")
      .type(String.class)
      .build();

  private static Option PAYLOAD_OPTION = Option.builder("p")
      .longOpt("payload")
      .desc("The payload string for the task.")
      .hasArg()
      .argName("payload")
      .type(String.class)
      .build();

  private static Option IN_SECONDS_OPTION = Option.builder("s")
      .longOpt("in-seconds")
      .desc("Schedule time for the task to create.")
      .hasArg()
      .argName("in-seconds")
      .type(int.class)
      .build();

  public static void main(String... args) throws Exception {
    Options options = new Options();
    options.addOption(PROJECT_ID_OPTION);
    options.addOption(QUEUE_OPTION);
    options.addOption(LOCATION_OPTION);
    options.addOption(PAYLOAD_OPTION);
    options.addOption(IN_SECONDS_OPTION);

    if (args.length == 0) {
      printUsage(options);
      return;
    }

    CommandLineParser parser = new DefaultParser();
    CommandLine params = null;
    try {
      params = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println("Invalid command line: " + e.getMessage());
      printUsage(options);
      return;
    }

    String projectId;
    if (params.hasOption("project-id")) {
      projectId = params.getOptionValue("project-id");
    } else {
      projectId = System.getenv(GOOGLE_CLOUD_PROJECT_KEY);
    }
    if (Strings.isNullOrEmpty(projectId)) {
      printUsage(options);
      return;
    }

    String queueName = params.getOptionValue(QUEUE_OPTION.getOpt());
    String location = params.getOptionValue(LOCATION_OPTION.getOpt());
    String payload = params.getOptionValue(PAYLOAD_OPTION.getOpt(), "default payload");

    // [START cloud_tasks_appengine_create_task]
    // Instantiates a client.
    try (CloudTasksClient client = CloudTasksClient.create()) {

      // Variables provided by the CLI.
      // projectId = "my-project-id";
      // queueName = "my-appengine-queue";
      // location = "us-central1";
      // payload = "hello";

      // Construct the fully qualified queue name.
      String queuePath = QueueName.of(projectId, location, queueName).toString();

      // Construct the task body.
      Task.Builder taskBuilder = Task
          .newBuilder()
          .setAppEngineHttpRequest(AppEngineHttpRequest.newBuilder()
              .setBody(ByteString.copyFrom(payload, Charset.defaultCharset()))
              .setRelativeUri("/tasks/create")
              .setHttpMethod(HttpMethod.POST)
              .build());

      if (params.hasOption(IN_SECONDS_OPTION.getOpt())) {
        // Add the scheduled time to the request.
        int seconds = Integer.parseInt(params.getOptionValue(IN_SECONDS_OPTION.getOpt()));
        taskBuilder.setScheduleTime(Timestamp
            .newBuilder()
            .setSeconds(Instant.now(Clock.systemUTC()).plusSeconds(seconds).getEpochSecond()));
      }

      // Send create task request.
      Task task = client.createTask(queuePath, taskBuilder.build());
      System.out.println("Task created: " + task.getName());
    }
    // [END cloud_tasks_appengine_create_task]
  }

  private static void printUsage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(
        "client",
        "A simple Cloud Tasks command line client that triggers a call to an AppEngine "
            + "endpoint.",
        options, "", true);
    throw new RuntimeException();
  }

}
