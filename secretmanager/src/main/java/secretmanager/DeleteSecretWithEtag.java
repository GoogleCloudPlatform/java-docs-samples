/*
 * Copyright 2021 Google LLC
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

// [START secretmanager_delete_secret_with_etag]
import com.google.cloud.secretmanager.v1.DeleteSecretRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import java.io.IOException;

public class DeleteSecretWithEtag {

  public static void deleteSecret() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String secretId = "your-secret-id";
    // Including the quotes is important.
    String etag = "\"1234\"";
    deleteSecret(projectId, secretId, etag);
  }

  // Delete an existing secret with the given name and etag.
  public static void deleteSecret(String projectId, String secretId, String etag)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the secret name.
      SecretName secretName = SecretName.of(projectId, secretId);

      // Construct the request.
      DeleteSecretRequest request =
          DeleteSecretRequest.newBuilder()
              .setName(secretName.toString())
              .setEtag(etag)
              .build();

      // Delete the secret.
      client.deleteSecret(request);
      System.out.printf("Deleted secret %s\n", secretId);
    }
  }
}
// [END secretmanager_delete_secret_with_etag]
