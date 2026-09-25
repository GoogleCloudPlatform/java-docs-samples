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

// [START secretmanager_enable_regional_secret_managed_rotation]
import com.google.cloud.secretmanager.v1.EnableManagedRotationRequest.CloudSQLSingleUserCredentials;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretVersion;
import java.io.IOException;

public class EnableRegionalSecretManagedRotation {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // Your GCP project ID.
    String projectId = "your-project-id";
    // Location of the secret.
    String locationId = "your-location-id";
    // Resource ID of the Cloud SQL DB credentials secret to enable rotation on.
    String secretId = "your-secret-id";
    // Bare ID of the Cloud SQL instance (no project or region prefix).
    String instanceId = "your-cloud-sql-instance-id";
    // Username of the Cloud SQL database user.
    String username = "your-cloud-sql-username";
    enableRegionalSecretManagedRotation(projectId, locationId, secretId, instanceId, username);
  }

  // Enable managed rotation for a Cloud SQL DB credentials secret. This links the secret to a
  // Cloud SQL instance and database user, and can only be called once per secret. It adds the
  // secret's first version and sets the matching password on the Cloud SQL user, taking the
  // place of a manually added secret version, which this secret type doesn't support.
  // Afterwards, use rotateRegionalSecret to trigger further rotations.
  //
  // instanceId is the bare Cloud SQL instance ID (e.g. "my-instance") -- not a connection name.
  // Neither the project nor the region should be included: passing "PROJECT_ID:INSTANCE_ID" (as
  // gcloud's own `enable-managed-rotation --help` examples misleadingly show) or the full
  // "PROJECT_ID:LOCATION_ID:INSTANCE_ID" connection name both fail -- the service already knows
  // the project from the secret's own path, and prepends it internally, so a qualified value
  // ends up double-prefixed.
  public static SecretVersion enableRegionalSecretManagedRotation(
      String projectId, String locationId, String secretId, String instanceId, String username)
      throws IOException {

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

      // Build the Cloud SQL credentials. Leaving the password unset lets Secret Manager
      // generate a secure password itself.
      CloudSQLSingleUserCredentials cloudSqlCredentials =
          CloudSQLSingleUserCredentials.newBuilder()
              .setInstanceId(instanceId)
              .setUsername(username)
              .build();

      // Enable managed rotation.
      SecretVersion version = client.enableManagedRotation(secretName, cloudSqlCredentials);
      System.out.printf(
          "Enabled managed rotation, created secret version: %s\n", version.getName());

      return version;
    }
  }
}
// [END secretmanager_enable_regional_secret_managed_rotation]
