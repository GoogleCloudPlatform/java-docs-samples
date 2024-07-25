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

// [START secretmanager_regional_quickstart]
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.LocationName;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.protobuf.ByteString;

public class RegionalQuickstart {

  public void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    
    // Your GCP project ID.
    String projectId = "your-project-id";
    // Location of the secret.
    String locationId = "your-location-id";
    // Resource ID of the secret.
    String secretId = "your-secret-id";
    regionalQuickstart(projectId, locationId, secretId);
  }

  // Demonstrates basic capabilities in the regional Secret Manager API.
  public SecretPayload regionalQuickstart(
      String projectId, String locationId, String secretId) 
      throws Exception {

    // Endpoint to call the regional secret manager sever
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      // Build the parent name from the project.
      LocationName parent = LocationName.of(projectId, locationId);

      // Create the parent secret.
      Secret secret =
          Secret.newBuilder()
              .build();

      Secret createdSecret = client.createSecret(parent, secretId, secret);

      // Add a secret version.
      SecretPayload payload =
          SecretPayload.newBuilder().setData(ByteString.copyFromUtf8("Secret data")).build();
      SecretVersion addedVersion = client.addSecretVersion(createdSecret.getName(), payload);

      // Access the secret version.
      AccessSecretVersionResponse response = client.accessSecretVersion(addedVersion.getName());

      // Print the secret payload.
      //
      // WARNING: Do not print the secret in a production environment - this
      // snippet is showing how to access the secret material.
      String data = response.getPayload().getData().toStringUtf8();
      // System.out.printf("Plaintext: %s\n", data);

      return payload;
    }
  }
}
// [END secretmanager_regional_quickstart]
