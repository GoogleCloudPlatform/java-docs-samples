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

// [START secretmanager_update_secret_rotation]
import com.google.cloud.secretmanager.v1.Rotation;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.Topic;
import com.google.protobuf.Duration;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.FieldMaskUtil;
import java.io.IOException;
import java.time.Instant;

public class UpdateSecretRotation {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    
    // This is the id of the GCP project
    String projectId = "your-project-id";
    // This is the id of the secret to update
    String secretId = "your-secret-id";
    // This is the rotation period in seconds (e.g., 2592000 for 30 days)
    long rotationPeriodSeconds = 2592000;
    // This is the topic name in the format projects/PROJECT_ID/topics/TOPIC_ID
    String topicName = "projects/your-project-id/topics/your-topic-id";
    updateSecretRotation(projectId, secretId, rotationPeriodSeconds, topicName);
  }

  // Update an existing secret with a new rotation policy.
  public static Secret updateSecretRotation(
      String projectId, String secretId, long rotationPeriodSeconds, String topicName) 
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the secret name.
      SecretName secretName = SecretName.of(projectId, secretId);

      // Calculate the next rotation time.
      Instant nextRotationTime = Instant.now().plusSeconds(rotationPeriodSeconds);
      Timestamp nextRotationTimestamp = Timestamp.newBuilder()
          .setSeconds(nextRotationTime.getEpochSecond())
          .setNanos(nextRotationTime.getNano())
          .build();

      // Build the rotation policy.
      Rotation rotation = Rotation.newBuilder()
          .setNextRotationTime(nextRotationTimestamp)
          .setRotationPeriod(Duration.newBuilder().setSeconds(rotationPeriodSeconds).build())
          .build();

      // Build the topic for rotation notifications.
      Topic topic = Topic.newBuilder()
          .setName(topicName)
          .build();

      // Build the updated secret with new rotation policy and topic.
      Secret secret =
          Secret.newBuilder()
              .setName(secretName.toString())
              .setRotation(rotation)
              .addTopics(topic)
              .build();

      // Build the field mask to update rotation and topics.
      FieldMask fieldMask = FieldMaskUtil.fromString("rotation,topics");

      // Update the secret.
      Secret updatedSecret = client.updateSecret(secret, fieldMask);
      System.out.printf("Updated secret %s with new rotation policy\n", updatedSecret.getName());

      return updatedSecret;
    }
  }
}
// [END secretmanager_update_secret_rotation]
