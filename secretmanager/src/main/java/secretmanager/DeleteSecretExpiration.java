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

// [START secretmanager_delete_secret_expiration]
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.protobuf.FieldMask;
import com.google.protobuf.util.FieldMaskUtil;
import java.io.IOException;

public class DeleteSecretExpiration {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    
    // This is the id of the GCP project
    String projectId = "your-project-id";
    // This is the id of the secret to update
    String secretId = "your-secret-id";
    deleteSecretExpiration(projectId, secretId);
  }

  // Delete the expiration time from an existing secret.
  public static Secret deleteSecretExpiration(String projectId, String secretId) 
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the secret name.
      SecretName secretName = SecretName.of(projectId, secretId);

      // Build the updated secret without expiration time.
      Secret secret =
          Secret.newBuilder()
              .setName(secretName.toString())
              .build();

      // Build the field mask to clear the expiration time.
      FieldMask fieldMask = FieldMaskUtil.fromString("expire_time");

      // Update the secret to remove expiration.
      Secret updatedSecret = client.updateSecret(secret, fieldMask);
      System.out.printf("Deleted expiration from secret %s\n", updatedSecret.getName());

      return updatedSecret;
    }
  }
}
// [END secretmanager_delete_secret_expiration]
