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

// [START secretmanager_delete_regional_secret_with_etag]
import com.google.cloud.secretmanager.v1.DeleteSecretRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import java.io.IOException;

public class DeleteRegionalSecretWithEtag {

  public static void deleteRegionalSecret() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String secretId = "your-secret-id";
    // Including the quotes is important.
    String etag = "\"1234\"";
    deleteRegionalSecret(projectId, locationId, secretId, etag);
  }

  // Delete an existing secret with the given name and etag.
  public static void deleteRegionalSecret(
      String projectId, String locationId, String secretId, String etag)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      // Build the secret name.
      SecretName secretName = SecretName.ofProjectLocationSecretName(
          projectId, locationId, secretId);

      // Construct the request.
      DeleteSecretRequest request =
          DeleteSecretRequest.newBuilder()
              .setName(secretName.toString())
              .setEtag(etag)
              .build();

      // Delete the secret.
      client.deleteSecret(request);
      System.out.printf("Deleted regional secret %s\n", secretId);
    }
  }
}
// [END secretmanager_delete_regional_secret_with_etag]
