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

// [START secretmanager_get_secret_type]
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import java.io.IOException;

public class GetSecretType {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // Your GCP project ID.
    String projectId = "your-project-id";
    // Resource ID of the secret you want to inspect.
    String secretId = "your-secret-id";
    getSecretType(projectId, secretId);
  }

  // Get and print the secret type (e.g. CLOUD_SQL_DB_CREDENTIALS, ACCESS_KEY, CERTIFICATE,
  // OTHER_DB_CREDENTIALS, OTHER, or SECRET_TYPE_UNSPECIFIED for a secret with no type
  // restriction) of the given secret.
  public static Secret getSecretType(String projectId, String secretId) throws IOException {
    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the name.
      SecretName secretName = SecretName.of(projectId, secretId);

      // Get the secret.
      Secret secret = client.getSecret(secretName);

      System.out.printf(
          "Found secret %s with secret type %s\n", secret.getName(), secret.getSecretType());

      return secret;
    }
  }
}
// [END secretmanager_get_secret_type]
