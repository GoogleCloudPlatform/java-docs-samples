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
    // Resource ID of the secret.
    String secretId = "your-secret-id";
    // ID of the Cloud SQL instance.
    String instanceId = "your-cloud-sql-instance-id";
    // Username of the Cloud SQL database user.
    String username = "your-cloud-sql-username";
    enableRegionalSecretManagedRotation(projectId, locationId, secretId, instanceId, username);
  }

  // Enables managed rotation of a CLOUD_SQL_DB_CREDENTIALS typed secret. It validates and
  // enables the rotation, adding a version and sets the passed password (optional).
  // Note: AddSecretVersion is disabled on the CLOUD_SQL_DB_CREDENTIALS currently and for any
  // necessary manual rotations please trigger rotateRegionalSecret.
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
      // Build the name.
      SecretName secretName =
          SecretName.ofProjectLocationSecretName(projectId, locationId, secretId);

      // Build the Cloud SQL credentials.
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
