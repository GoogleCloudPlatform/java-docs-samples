/*
 * Copyright 2021 Google LLC
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

// [START secretmanager_destroy_secret_version_with_etag]
import com.google.cloud.secretmanager.v1.DestroySecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import java.io.IOException;

public class DestroySecretVersionWithEtag {

  public static void destroySecretVersion() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String secretId = "your-secret-id";
    String versionId = "your-version-id";
    // Including the quotes is important.
    String etag = "\"1234\"";
    destroySecretVersion(projectId, secretId, versionId, etag);
  }

  // Destroy an existing secret version.
  public static void destroySecretVersion(
      String projectId, String secretId, String versionId, String etag)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the name from the version.
      SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);

      // Build the request.
      DestroySecretVersionRequest request =
          DestroySecretVersionRequest.newBuilder()
              .setName(secretVersionName.toString())
              .setEtag(etag)
              .build();

      // Destroy the secret version.
      SecretVersion version = client.destroySecretVersion(request);
      System.out.printf("Destroyed secret version %s\n", version.getName());
    }
  }
}
// [END secretmanager_destroy_secret_version_with_etag]
