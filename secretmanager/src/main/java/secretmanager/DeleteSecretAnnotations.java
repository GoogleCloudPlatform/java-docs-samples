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

package secretmanager;

// [START secretmanager_delete_secret_annotations]
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.protobuf.FieldMask;
import com.google.protobuf.util.FieldMaskUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DeleteSecretAnnotations {

  public static void main() throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // This is the id of the GCP project
    String projectId = "your-project-id";
    // This is the id of the secret to act on
    String secretId = "your-secret-id";
    deleteSecretAnnotations(projectId, secretId);
  }

  // Delete annotations from an existing secret.
  public static Secret deleteSecretAnnotations(String projectId, String secretId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created
    // once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the name of the secret.
      SecretName secretName = SecretName.of(projectId, secretId);

      // Build the updated secret with an empty annotations map.
      Secret secret =
          Secret.newBuilder()
              .setName(secretName.toString())
              .putAllAnnotations(new HashMap<>())
              .build();

      // Create the field mask for updating only the annotations
      FieldMask fieldMask = FieldMaskUtil.fromString("annotations");

      // Update the secret.
      Secret updatedSecret = client.updateSecret(secret, fieldMask);
      System.out.printf("Deleted annotations from %s\n", updatedSecret.getName());

      return updatedSecret;
    }
  }
}
// [END secretmanager_delete_secret_annotations]
