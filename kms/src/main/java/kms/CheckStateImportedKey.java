/*
 * Copyright 2022 Google LLC
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

// [START kms_check_state_imported_key]
import com.google.cloud.kms.v1.CryptoKeyVersion;
import com.google.cloud.kms.v1.CryptoKeyVersionName;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import java.io.IOException;

public class CheckStateImportedKey {

  public void checkStateImportedKey() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "us-east1";
    String keyRingId = "my-key-ring";
    String cryptoKeyId = "my-crypto-key";
    String cryptoKeyVersionId = "1";
    checkStateImportedKey(projectId, locationId, keyRingId, cryptoKeyId,
                          cryptoKeyVersionId);
  }

  // Check the state of an imported key in Cloud KMS.
  public void checkStateImportedKey(String projectId, String locationId,
                                    String keyRingId, String cryptoKeyId,
                                    String cryptoKeyVersionId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests. After
    // completing all of your requests, call the "close" method on the client to
    // safely clean up any remaining background resources.
    try (KeyManagementServiceClient client =
             KeyManagementServiceClient.create()) {
      // Build the version name from its path components.
      CryptoKeyVersionName versionName = CryptoKeyVersionName.of(
          projectId, locationId, keyRingId, cryptoKeyId, cryptoKeyVersionId);

      // Retrieve the state of an existing version.
      CryptoKeyVersion version = client.getCryptoKeyVersion(versionName);
      System.out.printf("Current state of crypto key version %s: %s%n",
                        version.getName(), version.getState());
    }
  }
}
// [END kms_check_state_imported_key]
