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

package secretmanager.regionalsamples;

// [START secretmanager_update_regional_secret_expiration]
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.FieldMaskUtil;
import java.io.IOException;
import java.time.Instant;

public class UpdateRegionalSecretExpiration {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    
    // Your GCP project ID.
    String projectId = "your-project-id";
    // Location of the secret.
    String locationId = "your-location-id";
    // Resource ID of the secret to update.
    String secretId = "your-secret-id";
    // This is the time in seconds from now when the secret will expire.
    long expireTimeSeconds = 86400; // 24 hours
    updateRegionalSecretExpiration(projectId, locationId, secretId, expireTimeSeconds);
  }

  // Update an existing regional secret with a new expiration time.
  public static Secret updateRegionalSecretExpiration(
      String projectId, String locationId, String secretId, long expireTimeSeconds) 
      throws IOException {
    
    // Endpoint to call the regional secret manager sever
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();
    
    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      // Build the secret name.
      SecretName secretName = 
          SecretName.ofProjectLocationSecretName(projectId, locationId, secretId);

      // Calculate the expiration time.
      Instant expireTime = Instant.now().plusSeconds(expireTimeSeconds);
      Timestamp expireTimestamp = Timestamp.newBuilder()
          .setSeconds(expireTime.getEpochSecond())
          .setNanos(expireTime.getNano())
          .build();

      // Build the updated secret with new expiration time.
      Secret secret =
          Secret.newBuilder()
              .setName(secretName.toString())
              .setExpireTime(expireTimestamp)
              .build();

      // Build the field mask to update only the expiration time.
      FieldMask fieldMask = FieldMaskUtil.fromString("expire_time");

      // Update the secret.
      Secret updatedSecret = client.updateSecret(secret, fieldMask);
      System.out.printf("Updated secret %s with new expiration time\n", 
          updatedSecret.getName());

      return updatedSecret;
    }
  }
}
// [END secretmanager_update_regional_secret_expiration]
