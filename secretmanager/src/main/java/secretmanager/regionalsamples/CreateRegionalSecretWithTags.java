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

package secretmanager.regionalsamples;

// [START secretmanager_create_regional_secret_with_tags]
import com.google.cloud.secretmanager.v1.LocationName;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import java.io.IOException;

public class CreateRegionalSecretWithTags {

  public static void createRegionalSecretWithTags() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    
    // This is the id of the GCP project
    String projectId = "your-project-id";
    // Location of the secret.
    String locationId = "your-location-id";
    // This is the id of the secret to act on
    String secretId = "your-secret-id";
    // This is the key of the tag to be added
    String tagKey = "your-tag-key";
    // This is the value of the tag to be added
    String tagValue = "your-tag-value";
    createRegionalSecretWithTags(projectId, locationId, secretId, tagKey, tagValue);
  }

  // Create a secret with tags.
  public static Secret createRegionalSecretWithTags(
       String projectId,
       String locationId,
       String secretId,
       String tagKey,
       String tagValue)
      throws IOException {
    
    // Endpoint to call the regional secret manager sever
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      
      // Build the parent name from the project.
      LocationName location = LocationName.of(projectId, locationId);

      // Build the secret to create with tags.
      Secret secret =
          Secret.newBuilder()
            .putTags(tagKey, tagValue)
            .build();

      // Create the secret.
      Secret createdSecret = client.createSecret(location.toString(), secretId, secret);
      System.out.printf("Created secret with Tags%s\n", createdSecret.getName());
      return createdSecret;
    }
  }
}
// [END secretmanager_create_regional_secret_with_tags]
