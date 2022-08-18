/*
 * Copyright 2022 Google LLC
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

import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FraudDetectionTestUtil {

  // Make sure that the variable is set from running Terraform.
  public static void requireVar(String varName) {
    assertNotNull(varName);
  }

  // Make sure that the required environment variables are set before running the tests.
  public static String requireEnv(String varName) {
    String value = System.getenv(varName);
    assertNotNull(
        String.format("Environment variable '%s' is required to perform these tests.", varName),
        value);
    return value;
  }

  // Parse Terraform output and populate the variables needed for testing.
  private static void parseTerraformOutput(Process terraformProcess) throws IOException {
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(terraformProcess.getInputStream()));

    // Process terraform output.
    String line;
    while ((line = reader.readLine()) != null) {
      System.out.println(line);
      if (line.contains("pubsub_input_topic = ")) {
        StreamingPipelineTest.pubsubInputTopic = line.split("\"")[1];
      } else if (line.contains("pubsub_output_topic = ")) {
        StreamingPipelineTest.pubsubOutputTopic = line.split("\"")[1];
      } else if (line.contains("pubsub_output_subscription = ")) {
        StreamingPipelineTest.pubsubOutputSubscription = line.split("\"")[1];
      } else if (line.contains("gcs_bucket = ")) {
        StreamingPipelineTest.gcsBucket = line.split("\"")[1];
      } else if (line.contains("cbt_instance = ")) {
        StreamingPipelineTest.cbtInstanceID = line.split("\"")[1];
      } else if (line.contains("cbt_table = ")) {
        StreamingPipelineTest.cbtTableID = line.split("\"")[1];
      }
    }
  }

  // Parse Terraform output and populate the variables needed for testing.
  private static String parseTerraformOutput2(Process terraformProcess) throws IOException {
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(terraformProcess.getInputStream()));

    // Process terraform output.
    String line = "";
    while ((line = reader.readLine()) != null) {
      // System.out.println(line);
      line += line + "\n";
      if (line.contains("pubsub_input_topic = ")) {
        StreamingPipelineTest.pubsubInputTopic = line.split("\"")[1];
      } else if (line.contains("pubsub_output_topic = ")) {
        StreamingPipelineTest.pubsubOutputTopic = line.split("\"")[1];
      } else if (line.contains("pubsub_output_subscription = ")) {
        StreamingPipelineTest.pubsubOutputSubscription = line.split("\"")[1];
      } else if (line.contains("gcs_bucket = ")) {
        StreamingPipelineTest.gcsBucket = line.split("\"")[1];
      } else if (line.contains("cbt_instance = ")) {
        StreamingPipelineTest.cbtInstanceID = line.split("\"")[1];
      } else if (line.contains("cbt_table = ")) {
        StreamingPipelineTest.cbtTableID = line.split("\"")[1];
      }
    }
    return line;
  }

  public static int runCommand(String command) throws IOException, InterruptedException {
    Process process = new ProcessBuilder(command.split(" ")).start();
    parseTerraformOutput(process);
    // Wait for the process to finish running and return the exit code.
    return process.waitFor();
  }
  public static String runCommand2(String command) throws IOException, InterruptedException {
    Process process = new ProcessBuilder(command.split(" ")).start();
    return parseTerraformOutput2(process);
  }
}
