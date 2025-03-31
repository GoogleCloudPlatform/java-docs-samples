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

// [START secretmanager_view_regional_secret_labels]
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import java.io.IOException;
import java.util.Map;

public class ViewRegionalSecretLabels {

  public static void viewRegionalSecretLabels() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    
    // This is the id of the GCP project
    String projectId = "your-project-id";
    // Location of the secret.
    String locationId = "your-location-id";
    // This is the id of the secret whose labels to view
    String secretId = "your-secret-id";
    viewRegionalSecretLabels(projectId, locationId, secretId);
  }

  // View the labels of an existing secret.
  public static Map<String, String> viewRegionalSecretLabels(
      String projectId,
      String locationId,
      String secretId
  ) throws IOException {

    // Endpoint to call the regional secret manager sever
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      // Build the name.
      SecretName secretName = 
          SecretName.ofProjectLocationSecretName(projectId, locationId, secretId);

      // Create the secret.
      Secret secret = client.getSecret(secretName);
      
      Map<String, String> labels = secret.getLabels();

      System.out.printf("Secret %s \n", secret.getName());

      for (Map.Entry<String, String> label : labels.entrySet()) {
        System.out.printf("Label key : %s, Label Value : %s\n", label.getKey(), label.getValue());
      }

      return secret.getLabels();
    }
  }
}
// [END secretmanager_view_regional_secret_labels]
