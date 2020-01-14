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

// [START secretmanager_enable_secret_version]
import com.google.cloud.secretmanager.v1beta1.EnableSecretVersionRequest;
import com.google.cloud.secretmanager.v1beta1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1beta1.SecretVersion;
import com.google.cloud.secretmanager.v1beta1.SecretVersionName;
import java.io.IOException;

public class EnableSecretVersion {

  // Enable an existing secret version.
  public SecretVersion enableSecretVersion(String projectId, String secretId, String versionId)
      throws IOException {
    // Create a Secret Manager client with cleanup.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the name from the version.
      SecretVersionName name = SecretVersionName.of(projectId, secretId, versionId);

      // Create the request.
      EnableSecretVersionRequest request =
          EnableSecretVersionRequest.newBuilder().setName(name.toString()).build();

      // Create the secret.
      SecretVersion version = client.enableSecretVersion(request);
      System.out.printf("Enabled secret version %s\n", version.getName());

      return version;
    }
  }
}
// [END secretmanager_enable_secret_version]
