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

// [START secretmanager_update_regional_secret_with_delayed_destroy]

import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.protobuf.Duration;
import com.google.protobuf.FieldMask;
import com.google.protobuf.util.FieldMaskUtil;
import java.io.IOException;

public class UpdateRegionalSecretWithDelayedDestroy {

  public static void updateRegionalSecretWithDelayedDestroy() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String secretId = "your-secret-id";
    Integer versionDestroyTtl = 86400;
    updateRegionalSecretWithDelayedDestroy(projectId, locationId, secretId, versionDestroyTtl);
  }

  // Update secret with version destroy TTL.
  public static Secret updateRegionalSecretWithDelayedDestroy(
      String projectId,
      String locationId,
      String secretId,
      Integer versionDestroyTtl)
      throws IOException {
    // Endpoint to call the regional secret manager sever
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize the client that will be used to send requests.
    try (SecretManagerServiceClient client =
             SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      // Build the parent name from the project and secret.
      SecretName secretName =
          SecretName.ofProjectLocationSecretName(projectId, locationId, secretId);

      // Build the secret to update.
      Secret secret =
          Secret.newBuilder()
              .setName(secretName.toString())
              .setVersionDestroyTtl(Duration.newBuilder().setSeconds(versionDestroyTtl))
              .build();

      // Build the field mask.
      FieldMask fieldMask = FieldMaskUtil.fromString("version_destroy_ttl");

      // Update the secret.
      Secret updatedSecret = client.updateSecret(secret, fieldMask);
      System.out.printf("Updated secret %s\n", updatedSecret.getName());

      return updatedSecret;
    }
  }
}
// [END secretmanager_update_regional_secret_with_delayed_destroy]
