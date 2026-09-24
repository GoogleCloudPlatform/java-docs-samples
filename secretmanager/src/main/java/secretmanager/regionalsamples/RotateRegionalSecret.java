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

// [START secretmanager_rotate_regional_secret]
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretVersion;
import java.io.IOException;

public class RotateRegionalSecret {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // Your GCP project ID.
    String projectId = "your-project-id";
    // Location of the secret.
    String locationId = "your-location-id";
    // Resource ID of the Cloud SQL DB credentials secret to rotate.
    String secretId = "your-secret-id";
    rotateRegionalSecret(projectId, locationId, secretId);
  }

  // Trigger a managed rotation for a Cloud SQL DB credentials secret. Managed rotation must
  // already be enabled on the secret (see enableRegionalSecretManagedRotation). Each call
  // generates a new password, updates the Cloud SQL user, and adds the result as a new secret
  // version.
  public static SecretVersion rotateRegionalSecret(
      String projectId, String locationId, String secretId) throws IOException {

    // Endpoint to call the regional secret manager sever
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client =
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      // Despite the field name, the request's "parent" holds the full secret resource name, not
      // a collection parent.
      SecretName secretName =
          SecretName.ofProjectLocationSecretName(projectId, locationId, secretId);

      // Rotate the secret.
      SecretVersion version = client.rotateSecret(secretName);
      System.out.printf("Rotated secret, created secret version: %s\n", version.getName());

      return version;
    }
  }
}
// [END secretmanager_rotate_regional_secret]
