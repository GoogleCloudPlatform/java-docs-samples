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

// [START kms_delete_key]
import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.cloud.kms.v1.DeleteCryptoKeyMetadata;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DeleteKey {

  public void deleteKey() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "us-east1";
    String keyRingId = "my-key-ring";
    String keyId = "my-key";
    deleteKey(projectId, locationId, keyRingId, keyId);
  }

  // deleteKey deletes a crypto key. This action is permanent and cannot be undone. Once the key
  // is deleted, it will no longer exist.
  public void deleteKey(String projectId, String locationId, String keyRingId, String keyId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests. After
    // completing all of your requests, call the "close" method on the client to
    // safely clean up any remaining background resources.
    try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
      // Build the key name from the project, location, key ring, and key.
      CryptoKeyName keyName = CryptoKeyName.of(projectId, locationId, keyRingId, keyId);

      // Delete the key.
      // Warning: This operation is permanent and cannot be undone.
      // Wait for the operation to complete.
      client.deleteCryptoKeyAsync(keyName).get();
      System.out.printf("Deleted key: %s%n", keyName.toString());
    } catch (Exception e) {
      System.err.printf("Failed to delete key: %s%n", e.getMessage());
    }
  }
}
// [END kms_delete_key]
