/*
 * Copyright 2026 Google LLC
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

package secretmanager;

// [START secretmanager_create_secret_with_topic]
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.Topic;
import java.io.IOException;

public class CreateSecretWithTopic {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    
    // This is the id of the GCP project
    String projectId = "your-project-id";
    // This is the id of the secret to create
    String secretId = "your-secret-id";
    // This is the topic name in the format projects/PROJECT_ID/topics/TOPIC_ID
    String topicName = "projects/your-project-id/topics/your-topic-id";
    createSecretWithTopic(projectId, secretId, topicName);
  }

  // Create a new secret with a Pub/Sub topic for notifications.
  public static Secret createSecretWithTopic(
      String projectId, String secretId, String topicName) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the parent name from the project.
      ProjectName projectName = ProjectName.of(projectId);

      // Build the topic.
      Topic topic = Topic.newBuilder()
          .setName(topicName)
          .build();

      // Build the secret to create with topic.
      Secret secret =
          Secret.newBuilder()
              .setReplication(
                  Replication.newBuilder()
                      .setAutomatic(Replication.Automatic.newBuilder().build())
                      .build())
              .addTopics(topic)
              .build();

      // Create the secret.
      Secret createdSecret = client.createSecret(projectName, secretId, secret);
      System.out.printf("Created secret %s with topic\n", createdSecret.getName());

      return createdSecret;
    }
  }
}
// [END secretmanager_create_secret_with_topic]
