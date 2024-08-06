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

package secretmanager;

// [START secretmanager_view_secret_labels]
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import java.io.IOException;
import java.util.Map;

public class ViewSecretLabels {

  public static void viewSecretLabels() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    
    // This is the id of the GCP project
    String projectId = "your-project-id";
    // This is the id of the secret whose labels to view
    String secretId = "your-secret-id";
    viewSecretLabels(projectId, secretId);
  }

  // View the labels of an existing secret.
  public static Map<String, String> viewSecretLabels(
      String projectId,
      String secretId
  ) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the name.
      SecretName secretName = SecretName.of(projectId, secretId);

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
// [END secretmanager_view_secret_labels]
