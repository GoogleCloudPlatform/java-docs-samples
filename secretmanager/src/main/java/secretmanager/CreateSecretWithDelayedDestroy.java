/*
 * Copyright 2025 Google LLC
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

// [START secretmanager_create_secret_with_delayed_destroy]

import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.protobuf.Duration;
import java.io.IOException;

public class CreateSecretWithDelayedDestroy {

  public static void createSecretWithDelayedDestroy() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String secretId = "your-secret-id";
    Integer versionDestroyTtl = 86400;
    createSecretWithDelayedDestroy(projectId, secretId, versionDestroyTtl);
  }

  // Create secret with version destroy TTL.
  public static Secret createSecretWithDelayedDestroy(
      String projectId,
      String secretId,
      Integer versionDestroyTtl)
      throws IOException {
    // Initialize the client that will be used to send requests.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the parent name from the project.
      ProjectName projectName = ProjectName.of(projectId);

      // Build the secret to create.
      Secret secret =
          Secret.newBuilder()
              .setReplication(
                  Replication.newBuilder()
                      .setAutomatic(Replication.Automatic.newBuilder().build())
                      .build())
              .setVersionDestroyTtl(Duration.newBuilder().setSeconds(versionDestroyTtl))
              .build();

      // Create the secret.
      Secret createdSecret = client.createSecret(projectName, secretId, secret);
      System.out.printf("Created secret with version destroy ttl %s\n", createdSecret.getName());

      return createdSecret;
    }
  }
}
// [END secretmanager_create_secret_with_delayed_destroy]
