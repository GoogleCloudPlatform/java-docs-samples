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

package kms;

// [START kms_delete_key_version]
import com.google.cloud.kms.v1.CryptoKeyVersionName;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import java.io.IOException;

public class DeleteKeyVersion {

  public void deleteKeyVersion() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "us-east1";
    String keyRingId = "my-key-ring";
    String keyId = "my-key";
    String keyVersionId = "123";
    deleteKeyVersion(projectId, locationId, keyRingId, keyId, keyVersionId);
  }

  // deleteKeyVersion deletes a key version. This action is permanent and cannot be undone. Once the
  // key version is deleted, it will no longer exist.
  public void deleteKeyVersion(
      String projectId, String locationId, String keyRingId, String keyId, String keyVersionId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests. After
    // completing all of your requests, call the "close" method on the client to
    // safely clean up any remaining background resources.
    try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
      // Build the key version name from the project, location, key ring, key,
      // and key version.
      CryptoKeyVersionName keyVersionName =
          CryptoKeyVersionName.of(projectId, locationId, keyRingId, keyId, keyVersionId);

      // Delete the key version.
      // Warning: This operation is permanent and cannot be undone.
      // Wait for the operation to complete.
      client.deleteCryptoKeyVersionAsync(keyVersionName).get();
      System.out.printf("Deleted key version: %s%n", keyVersionName.toString());
    } catch (Exception e) {
      System.err.printf("Failed to delete key version: %s%n", e.getMessage());
    }
  }
}
// [END kms_delete_key_version]
