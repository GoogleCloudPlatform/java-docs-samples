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

// [START secretmanager_update_regional_secret_with_managed_rotation_schedule]
import com.google.cloud.secretmanager.v1.Rotation;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.protobuf.Duration;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.FieldMaskUtil;
import com.google.protobuf.util.Timestamps;
import java.io.IOException;
import java.time.Instant;

public class UpdateRegionalSecretWithManagedRotationSchedule {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // Your GCP project ID.
    String projectId = "your-project-id";
    // Location of the secret.
    String locationId = "your-location-id";
    // Resource ID of the secret.
    String secretId = "your-secret-id";
    // Interval between rotations, in seconds.
    long rotationPeriodSeconds = 86400; // 24 hours
    updateRegionalSecretWithManagedRotationSchedule(
        projectId, locationId, secretId, rotationPeriodSeconds);
  }

  // Updates the rotation schedule of a CLOUD_SQL_DB_CREDENTIALS typed secret.
  public static Secret updateRegionalSecretWithManagedRotationSchedule(
      String projectId, String locationId, String secretId, long rotationPeriodSeconds)
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

      // The rotation schedule of a CLOUD_SQL_DB_CREDENTIALS secret can be set before or after
      // enabling managed rotation; EnableManagedRotation does not need to be called first. Other
      // secret types also support a rotation schedule, but only when Pub/Sub topics are configured.
      // Pub/Sub topics are not required for CLOUD_SQL_DB_CREDENTIALS.
      // next_rotation_time and rotation_period must be set together.
      Instant nextRotationInstant = Instant.now().plusSeconds(rotationPeriodSeconds);
      Timestamp nextRotationTime = Timestamps.fromMillis(nextRotationInstant.toEpochMilli());
      Duration rotationPeriod = Durations.fromSeconds(rotationPeriodSeconds);

      // Build the updated secret.
      Secret secret =
          Secret.newBuilder()
              .setName(secretName.toString())
              .setRotation(
                  Rotation.newBuilder()
                      .setNextRotationTime(nextRotationTime)
                      .setRotationPeriod(rotationPeriod)
                      .build())
              .build();

      // Mask only the rotation subfields being set, not the whole "rotation" submessage.
      FieldMask fieldMask =
          FieldMaskUtil.fromString("rotation.next_rotation_time,rotation.rotation_period");

      // Update the secret.
      Secret updatedSecret = client.updateSecret(secret, fieldMask);
      System.out.printf("Updated regional secret rotation schedule: %s\n", updatedSecret.getName());

      return updatedSecret;
    }
  }
}
// [END secretmanager_update_regional_secret_with_managed_rotation_schedule]
