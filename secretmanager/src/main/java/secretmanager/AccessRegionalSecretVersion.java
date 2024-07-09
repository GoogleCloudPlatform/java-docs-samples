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

package secretmanager;

// [START secretmanager_access_regional_secret_version]
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import java.io.IOException;
import java.util.zip.CRC32C;
import java.util.zip.Checksum;

public class AccessRegionalSecretVersion {

  public static void accessRegionalSecretVersion() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String secretId = "your-secret-id";
    String versionId = "your-version-id";
    accessRegionalSecretVersion(projectId, locationId, secretId, versionId);
  }

  // Access the payload for the given secret version if one exists. The version
  // can be a version number as a string (e.g. "5") or an alias (e.g. "latest").
  public static void accessRegionalSecretVersion(
      String projectId, String locationId, String secretId, String versionId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();
      
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      SecretVersionName secretVersionName = 
          SecretVersionName.ofProjectLocationSecretSecretVersionName(
              projectId, locationId, secretId, versionId);
      // Access the secret version.
      AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

      // Verify checksum. The used library is available in Java 9+.
      // If using Java 8, you may use the following:
      // https://github.com/google/guava/blob/e62d6a0456420d295089a9c319b7593a3eae4a83/guava/src/com/google/common/hash/Hashing.java#L395
      byte[] data = response.getPayload().getData().toByteArray();
      Checksum checksum = new CRC32C();
      checksum.update(data, 0, data.length);
      if (response.getPayload().getDataCrc32C() != checksum.getValue()) {
        System.out.printf("Data corruption detected.");
        return;
      }

      // Print the secret payload.
      //
      // WARNING: Do not print the secret in a production environment - this
      // snippet is showing how to access the secret material.
      String payload = response.getPayload().getData().toStringUtf8();
      System.out.printf("Plaintext: %s\n", payload);
    }
  }
}
// [END secretmanager_access_regional_secret_version]
