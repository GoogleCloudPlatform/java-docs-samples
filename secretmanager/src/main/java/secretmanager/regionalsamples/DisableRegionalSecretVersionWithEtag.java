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

// [START secretmanager_disable_regional_secret_version_with_etag]
import com.google.cloud.secretmanager.v1.DisableSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import java.io.IOException;

public class DisableRegionalSecretVersionWithEtag {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
   
    // Your GCP project ID.
    String projectId = "your-project-id";
    // Location of the secret.
    String locationId = "your-location-id";
    // Resource ID of the secret.
    String secretId = "your-secret-id";
    // Version of the Secret ID you want to disable.
    String versionId = "your-version-id";
    // Etag associated with the secret. Quotes should be included as part of the string.
    String etag = "\"1234\"";
    disableRegionalSecretVersionWithEtag(projectId, locationId, secretId, versionId, etag);
  }

  // Disable an existing secret version.
  public static SecretVersion disableRegionalSecretVersionWithEtag(
      String projectId, String locationId, String secretId, String versionId, String etag)
      throws IOException {
    
    // Endpoint to call the regional secret manager sever
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      // Build the name from the version.
      SecretVersionName secretVersionName 
            = SecretVersionName.ofProjectLocationSecretSecretVersionName(
            projectId, locationId, secretId, versionId);

      // Build the request.
      DisableSecretVersionRequest request =
          DisableSecretVersionRequest.newBuilder()
              .setName(secretVersionName.toString())
              .setEtag(etag)
              .build();

      // Disable the secret version.
      SecretVersion version = client.disableSecretVersion(request);
      System.out.printf("Disabled regional secret version %s\n", version.getName());

      return version;
    }
  }
}
// [END secretmanager_disable_regional_secret_version_with_etag]
