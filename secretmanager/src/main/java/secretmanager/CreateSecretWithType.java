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

// [START secretmanager_create_secret_with_type]
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.Secret.SecretType;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import java.io.IOException;

public class CreateSecretWithType {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // Your GCP project ID.
    String projectId = "your-project-id";
    // Resource ID of the secret to create.
    String secretId = "your-secret-id";
    // Secret type restriction, e.g. ACCESS_KEY, CERTIFICATE, OTHER_DB_CREDENTIALS, or OTHER.
    // Use CLOUD_SQL_DB_CREDENTIALS only for a secret that will go through
    // enableManagedRotation, which additionally requires a regional secret; see
    // CreateRegionalSecretWithCloudSqlCredentials in the regionalsamples package.
    SecretType secretType = SecretType.ACCESS_KEY;
    createSecretWithType(projectId, secretId, secretType);
  }

  // Create a new secret with the given secret type restriction. Unlike
  // CLOUD_SQL_DB_CREDENTIALS, these other secret types are plain metadata tags: they don't
  // require any additional credentials payload at creation time.
  public static Secret createSecretWithType(
      String projectId, String secretId, SecretType secretType) throws IOException {
    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the parent name from the project.
      ProjectName projectName = ProjectName.of(projectId);

      // Build the secret to create, with the given secret type restriction.
      Secret secret =
          Secret.newBuilder()
              .setReplication(
                  Replication.newBuilder()
                      .setAutomatic(Replication.Automatic.newBuilder().build())
                      .build())
              .setSecretType(secretType)
              .build();

      // Create the secret.
      Secret createdSecret = client.createSecret(projectName, secretId, secret);
      System.out.printf("Created secret with secret type: %s\n", createdSecret.getName());

      return createdSecret;
    }
  }
}
// [END secretmanager_create_secret_with_type]
