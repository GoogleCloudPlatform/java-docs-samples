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

// [START secretmanager_add_secret_version]
import com.google.cloud.secretmanager.v1beta1.AddSecretVersionRequest;
import com.google.cloud.secretmanager.v1beta1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1beta1.SecretName;
import com.google.cloud.secretmanager.v1beta1.SecretPayload;
import com.google.cloud.secretmanager.v1beta1.SecretVersion;
import com.google.protobuf.ByteString;
import java.io.IOException;

public class AddSecretVersion {

  // Add a new version to the existing secret.
  public SecretVersion addSecretVersion(String projectId, String secretId) throws IOException {
    // Create a Secret Manager client with cleanup.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      SecretName name = SecretName.of(projectId, secretId);

      // Create the secret payload
      String payload = "my super secret data";

      // Create the request.
      AddSecretVersionRequest request =
          AddSecretVersionRequest.newBuilder()
              .setParent(name.toString())
              .setPayload(
                  SecretPayload.newBuilder().setData(ByteString.copyFromUtf8(payload)).build())
              .build();

      // Add the secret version.
      SecretVersion version = client.addSecretVersion(request);
      System.out.printf("Added secret version %s\n", version.getName());

      return version;
    }
  }
}
// [END secretmanager_add_secret_version]
