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

// [START secretmanager_create_secret_with_expiration]
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.protobuf.Timestamp;
import java.io.IOException;
import java.time.Instant;

public class CreateSecretWithExpiration {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    
    // This is the id of the GCP project
    String projectId = "your-project-id";
    // This is the id of the secret to create
    String secretId = "your-secret-id";
    // This is the time in seconds from now when the secret will expire
    long expireTimeSeconds = 86400; // 24 hours
    createSecretWithExpiration(projectId, secretId, expireTimeSeconds);
  }

  // Create a new secret with an expiration time.
  public static Secret createSecretWithExpiration(
      String projectId, String secretId, long expireTimeSeconds) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the parent name from the project.
      ProjectName projectName = ProjectName.of(projectId);

      // Calculate the expiration time.
      Instant expireTime = Instant.now().plusSeconds(expireTimeSeconds);
      Timestamp expireTimestamp = Timestamp.newBuilder()
          .setSeconds(expireTime.getEpochSecond())
          .setNanos(expireTime.getNano())
          .build();

      // Build the secret to create with expiration time.
      Secret secret =
          Secret.newBuilder()
              .setReplication(
                  Replication.newBuilder()
                      .setAutomatic(Replication.Automatic.newBuilder().build())
                      .build())
              .setExpireTime(expireTimestamp)
              .build();

      // Create the secret.
      Secret createdSecret = client.createSecret(projectName, secretId, secret);
      System.out.printf("Created secret %s with expire time\n", createdSecret.getName());

      return createdSecret;
    }
  }
}
// [END secretmanager_create_secret_with_expiration]
