/*
 * Copyright 2020 Google LLC
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

// [START secretmanager_create_secret]
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.protobuf.Duration;
import java.io.IOException;

public class CreateSecret {

  public static void createSecret() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String secretId = "your-secret-id";
    createSecret(projectId, secretId);
  }

  // Create a new secret with automatic replication.
  public static void createSecret(String projectId, String secretId) throws IOException {
    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests. After completing all of your requests,
    // call the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the parent name from the project.
      ProjectName projectName = ProjectName.of(projectId);

      // Optionally set a TTL for the secret. This demonstrates how to configure
      // a secret to be automatically deleted after a certain period. The TTL is
      // specified in seconds (e.g., 900 for 15 minutes). This can be useful
      // for managing sensitive data and reducing storage costs.
      Duration ttl = Duration.newBuilder().setSeconds(900).build();

      // Build the secret to create.
      Secret secret =
          Secret.newBuilder()
              .setReplication(
                  Replication.newBuilder()
                      .setAutomatic(Replication.Automatic.newBuilder().build())
                      .build())
              .setTtl(ttl)
              .build();

      // Create the secret.
      Secret createdSecret = client.createSecret(projectName, secretId, secret);
      System.out.printf("Created secret %s\n", createdSecret.getName());
    }
  }
}
// [END secretmanager_create_secret]
