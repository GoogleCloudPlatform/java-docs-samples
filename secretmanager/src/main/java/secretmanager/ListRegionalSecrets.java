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

// [START secretmanager_list_regional_secrets]
import com.google.cloud.secretmanager.v1.LocationName;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient.ListSecretsPagedResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import java.io.IOException;

public class ListRegionalSecrets {

  public static void listRegionalSecrets() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    listRegionalSecrets(projectId, locationId);
  }

  // List all secrets for a project
  public static void listRegionalSecrets(String projectId, String locationId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      // Build the parent name.
      LocationName parent = LocationName.of(projectId, locationId);

      // Get all secrets.
      ListSecretsPagedResponse pagedResponse = client.listSecrets(parent.toString());

      // List all secrets.
      pagedResponse
          .iterateAll()
          .forEach(
              secret -> {
                System.out.printf("Regional secret %s\n", secret.getName());
              });
    }
  }
}
// [END secretmanager_list_regional_secrets]
