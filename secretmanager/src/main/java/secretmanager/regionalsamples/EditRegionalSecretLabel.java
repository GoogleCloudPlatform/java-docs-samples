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

// [START secretmanager_edit_regional_secret_label]
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.protobuf.FieldMask;
import com.google.protobuf.util.FieldMaskUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditRegionalSecretLabel {

  public static void editRegionalSecretLabel() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    
    // This is the id of the GCP project
    String projectId = "your-project-id";
    // Location of the secret.
    String locationId = "your-location-id";
    // This is the id of the secret to act on
    String secretId = "your-secret-id";
    // This is the key of the label to be added/updated
    String labelKey = "your-label-key";
    // This is the value of the label to be added/updated
    String labelValue = "your-label-value";
    editRegionalSecretLabel(projectId, locationId, secretId, labelKey, labelValue);
  }

  // Update an existing secret, by creating a new label or updating an existing label.
  public static Secret editRegionalSecretLabel(
       String projectId, String locationId, String secretId, String labelKey, String labelValue)
      throws IOException {

    // Endpoint to call the regional secret manager sever
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      // Build the secret name.
      SecretName secretName = 
          SecretName.ofProjectLocationSecretName(projectId, locationId, secretId);

      // Get the existing secret
      Secret existingSecret = client.getSecret(secretName);

      Map<String, String> existingLabelsMap = 
                      new HashMap<String, String>(existingSecret.getLabels());

      // Add a new label key and value.
      existingLabelsMap.put(labelKey, labelValue);

      // Build the updated secret.
      Secret secret =
          Secret.newBuilder()
              .setName(secretName.toString())
              .putAllLabels(existingLabelsMap)
              .build();

      // Build the field mask.
      FieldMask fieldMask = FieldMaskUtil.fromString("labels");

      // Update the secret.
      Secret updatedSecret = client.updateSecret(secret, fieldMask);
      System.out.printf("Updated secret %s\n", updatedSecret.getName());

      return updatedSecret;
    }
  }
}
// [END secretmanager_edit_regional_secret_label]
