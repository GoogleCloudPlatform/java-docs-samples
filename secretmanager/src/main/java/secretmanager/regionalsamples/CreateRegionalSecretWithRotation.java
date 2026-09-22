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

package secretmanager.regionalsamples;

// [START secretmanager_create_regional_secret_with_rotation]
import com.google.cloud.secretmanager.v1.LocationName;
import com.google.cloud.secretmanager.v1.Rotation;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.Topic;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import java.io.IOException;
import java.time.Instant;

public class CreateRegionalSecretWithRotation {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    
    // Your GCP project ID.
    String projectId = "your-project-id";
    // Location of the secret.
    String locationId = "your-location-id";
    // Resource ID of the secret to create.
    String secretId = "your-secret-id";
    // This is the rotation period in seconds (e.g., 2592000 for 30 days).
    long rotationPeriodSeconds = 2592000;
    // This is the topic name in the format projects/PROJECT_ID/topics/TOPIC_ID.
    String topicName = "projects/your-project-id/topics/your-topic-id";
    createRegionalSecretWithRotation(projectId, locationId, secretId, rotationPeriodSeconds, 
        topicName);
  }

  // Create a new regional secret with automatic rotation.
  public static Secret createRegionalSecretWithRotation(
      String projectId, String locationId, String secretId, long rotationPeriodSeconds,
      String topicName) throws IOException {
    
    // Endpoint to call the regional secret manager sever
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();
    
    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      // Build the parent name from the project.
      LocationName location = LocationName.of(projectId, locationId);

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

      // Build the regional secret to create with rotation and topic.
      Secret secret =
          Secret.newBuilder()
              .setRotation(rotation)
              .addTopics(topic)
              .build();

      // Create the regional secret.
      Secret createdSecret = client.createSecret(location.toString(), secretId, secret);
      System.out.printf("Created secret %s with rotation\n", createdSecret.getName());

      return createdSecret;
    }
  }
}
// [END secretmanager_create_regional_secret_with_rotation]
