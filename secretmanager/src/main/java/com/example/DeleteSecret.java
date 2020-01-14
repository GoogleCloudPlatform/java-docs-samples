/*
 * Copyright 2020 Google LLC
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

package com.example;

// [START secretmanager_delete_secret]
import com.google.cloud.secretmanager.v1beta1.DeleteSecretRequest;
import com.google.cloud.secretmanager.v1beta1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1beta1.SecretName;
import java.io.IOException;

public class DeleteSecret {

  // Delete an existing secret with the given name.
  public void deleteSecret(String projectId, String secretId) throws IOException {
    // Create a Secret Manager client with cleanup.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the secret name.
      SecretName name = SecretName.of(projectId, secretId);

      // Create the request.
      DeleteSecretRequest request =
          DeleteSecretRequest.newBuilder().setName(name.toString()).build();

      // Create the secret.
      client.deleteSecret(request);
      System.out.printf("Deleted secret %s\n", secretId);
    }
  }
}
// [END secretmanager_delete_secret]
