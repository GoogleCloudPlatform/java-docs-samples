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

import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.pubsub.v1.AcknowledgeRequest;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.ReceivedMessage;
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
        new BufferedReader(new InputStreamReader(terraformProcess.getErrorStream()));

    // Process terraform output.
    String line;
    while ((line = reader.readLine()) != null) {
      System.out.println(line);
      System.err.println(line);
      if (line.contains("pubsub_input_topic = ")) {
        // example: pubsub_output_topic = "THE NAME WE WANT"
        StreamingPipelineTest.pubsubInputTopic = line.split("\"")[1];
      } else if (line.contains("pubsub_output_topic = ")) {
        // example: pubsub_output_subscription = "THE NAME WE WANT"
        StreamingPipelineTest.pubsubOutputTopic = line.split("\"")[1];
      } else if (line.contains("pubsub_output_subscription = ")) {
        // example: gcs_bucket = "THE NAME WE WANT"
        StreamingPipelineTest.pubsubOutputSubscription = line.split("\"")[1];
      } else if (line.contains("gcs_bucket = ")) {
        // example: gcs_bucket = "THE NAME WE WANT"
        StreamingPipelineTest.gcsBucket = line.split("\"")[1];
      } else if (line.contains("cbt_instance = ")) {
        // example: cbt_instance = "THE NAME WE WANT"
        StreamingPipelineTest.cbtInstanceID = line.split("\"")[1];
      } else if (line.contains("cbt_table = ")) {
        // example: cbt_table = "THE NAME WE WANT"
        StreamingPipelineTest.cbtTableID = line.split("\"")[1];
      }
    }
  }

  // Parse Terraform output and populate the variables needed for testing.
  private static String parseTerraformOutput2(Process terraformProcess) throws IOException {
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(terraformProcess.getInputStream()));
    StringBuilder answer = new StringBuilder();
    // Process terraform output.
    String line;
    while ((line = reader.readLine()) != null) {
      System.err.println(line);
      answer.append(line).append("\n");
      if (line.contains("pubsub_input_topic = ")) {
        // example: pubsub_output_topic = "THE NAME WE WANT"
        StreamingPipelineTest.pubsubInputTopic = line.split("\"")[1];
      } else if (line.contains("pubsub_output_topic = ")) {
        // example: pubsub_output_subscription = "THE NAME WE WANT"
        StreamingPipelineTest.pubsubOutputTopic = line.split("\"")[1];
      } else if (line.contains("pubsub_output_subscription = ")) {
        // example: gcs_bucket = "THE NAME WE WANT"
        StreamingPipelineTest.pubsubOutputSubscription = line.split("\"")[1];
      } else if (line.contains("gcs_bucket = ")) {
        // example: gcs_bucket = "THE NAME WE WANT"
        StreamingPipelineTest.gcsBucket = line.split("\"")[1];
      } else if (line.contains("cbt_instance = ")) {
        // example: cbt_instance = "THE NAME WE WANT"
        StreamingPipelineTest.cbtInstanceID = line.split("\"")[1];
      } else if (line.contains("cbt_table = ")) {
        // example: cbt_table = "THE NAME WE WANT"
        StreamingPipelineTest.cbtTableID = line.split("\"")[1];
      }
    }
    return answer.toString();
  }

  public static int runCommand(String command) throws IOException, InterruptedException {
    Process process = new ProcessBuilder(command.split(" ")).start();
    parseTerraformOutput(process);
    // Wait for the process to finish running and return the exit code.
    return process.waitFor();
  }

  public static String runCommand2(String command) throws IOException, InterruptedException {
    Process process = new ProcessBuilder(command.split(" ")).start();
    String answer = parseTerraformOutput2(process);
    // Wait for the process to finish running and return the exit code.
    int r = process.waitFor();
    return answer;
  }

  // Returns all transactions in a file inside a GCS bucket.
  public static String[] getTransactions(String projectID, String gcsBucket, String filePath) {
    // Set StorageOptions for reading.
    StorageOptions options = StorageOptions.newBuilder()
        .setProjectId(projectID).build();

    Storage storage = options.getService();
    Blob blob = storage.get(gcsBucket, filePath);
    String fileContent = new String(blob.getContent());
    // return all transactions inside gcsBucket/filePath.
    return fileContent.split("\n");
  }

  public static SubscriberStub buildSubscriberStub() throws IOException {
    // Build Subscriber stub settings.
    SubscriberStubSettings subscriberStubSettings =
        SubscriberStubSettings.newBuilder()
            .setTransportChannelProvider(
                SubscriberStubSettings.defaultGrpcTransportProviderBuilder()
                    .setMaxInboundMessageSize(1 * 1024 * 1024) // 1MB (maximum message size).
                    .build())
            .build();
    return GrpcSubscriberStub.create(subscriberStubSettings);
  }

  // Read one message from subscriptionId, ack it and returns it.
  public static String readOneMessage(SubscriberStub subscriberStub, String projectId,
      String subscriptionId) throws IOException {
    String subscriptionName = ProjectSubscriptionName.format(projectId, subscriptionId);
    PullRequest pullRequest =
        PullRequest.newBuilder().setMaxMessages(1).setSubscription(subscriptionName).build();

    // Try to receive a message.
    ReceivedMessage receivedMessage = null;
    String payload = null;
    int numOfRetries = 10;
    while (receivedMessage == null && numOfRetries-- > 0) {
      PullResponse pullResponse = subscriberStub.pullCallable().call(pullRequest);
      if (pullResponse.getReceivedMessagesList().size() > 0) {
        receivedMessage = pullResponse.getReceivedMessagesList().get(0);
        payload = receivedMessage.getMessage().getData().toStringUtf8();
      }
    }

    // If we didn't receive anything, return null.
    if (receivedMessage == null) {
      return null;
    }

    // Ack the message.
    String ackId = receivedMessage.getAckId();
    AcknowledgeRequest acknowledgeRequest =
        AcknowledgeRequest.newBuilder()
            .setSubscription(subscriptionName)
            .addAckIds(ackId)
            .build();
    subscriberStub.acknowledgeCallable().call(acknowledgeRequest);
    return payload;
  }
}
