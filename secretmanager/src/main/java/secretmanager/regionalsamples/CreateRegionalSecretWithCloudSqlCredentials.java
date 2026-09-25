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

// [START secretmanager_create_regional_secret_with_cloud_sql_credentials]
import com.google.cloud.secretmanager.v1.LocationName;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.Secret.SecretType;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import java.io.IOException;

public class CreateRegionalSecretWithCloudSqlCredentials {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // Your GCP project ID.
    String projectId = "your-project-id";
    // Location of the secret; must match the Cloud SQL instance's region.
    String locationId = "your-location-id";
    // Resource ID of the secret to create.
    String secretId = "your-secret-id";
    createRegionalSecretWithCloudSqlCredentials(projectId, locationId, secretId);
  }

  // Create a new secret with the Cloud SQL DB credentials secret type. This type is required
  // to enable Secret Manager's automatic rotation of Cloud SQL passwords. It can only be set
  // when the secret is created, and the secret's location must match the region of the target
  // Cloud SQL instance.
  public static Secret createRegionalSecretWithCloudSqlCredentials(
      String projectId, String locationId, String secretId) throws IOException {

    // Endpoint to call the regional secret manager sever
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client =
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      // Build the parent name from the project.
      LocationName location = LocationName.of(projectId, locationId);

      // Build the secret to create, with the Cloud SQL DB credentials secret type.
      Secret secret =
          Secret.newBuilder().setSecretType(SecretType.CLOUD_SQL_DB_CREDENTIALS).build();

      // Create the regional secret.
      Secret createdSecret = client.createSecret(location.toString(), secretId, secret);
      System.out.printf("Created secret: %s\n", createdSecret.getName());

      // This built-in identity is what you grant Cloud SQL IAM permissions to, so that Secret
      // Manager can rotate the database password on its behalf.
      System.out.printf(
          "Grant this identity Cloud SQL IAM permissions to enable rotation: %s\n",
          createdSecret.getPolicyMember().getIamPolicyUidPrincipal());

      return createdSecret;
    }
  }
}
// [END secretmanager_create_regional_secret_with_cloud_sql_credentials]
