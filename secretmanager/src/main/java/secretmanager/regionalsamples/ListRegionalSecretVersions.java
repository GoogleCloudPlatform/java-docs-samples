/*
 * Copyright 2024 Google LLC
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

// [START secretmanager_list_regional_secret_versions]
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient.ListSecretVersionsPagedResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import java.io.IOException;

public class ListRegionalSecretVersions {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
   
    // Your GCP project ID.
    String projectId = "your-project-id";
    // Location of the secret.
    String locationId = "your-location-id";
    // Resource ID of the secret.
    String secretId = "your-secret-id";
    listRegionalSecretVersions(projectId, locationId, secretId);
  }

  // List all secret versions for a secret.
  public static ListSecretVersionsPagedResponse listRegionalSecretVersions(
      String projectId, String locationId, String secretId)
      throws IOException {

    // Endpoint to call the regional secret manager sever
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      // Build the parent name.
      SecretName secretName = 
          SecretName.ofProjectLocationSecretName(projectId, locationId, secretId);

      // Get all versions.
      ListSecretVersionsPagedResponse pagedResponse = client.listSecretVersions(secretName);

      // List all versions and their state.
      pagedResponse
          .iterateAll()
          .forEach(
              version -> {
                System.out.printf("Regional secret version %s, %s\n", 
                    version.getName(), version.getState());
              });

      return pagedResponse;
    }
  }
}
// [END secretmanager_list_regional_secret_versions]
