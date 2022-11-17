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

// [START kms_create_key_for_import]
import com.google.cloud.kms.v1.CreateCryptoKeyRequest;
import com.google.cloud.kms.v1.CryptoKey;
import com.google.cloud.kms.v1.CryptoKeyVersionTemplate;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.kms.v1.KeyRingName;
import com.google.cloud.kms.v1.ProtectionLevel;
import com.google.cloud.kms.v1.CryptoKey.CryptoKeyPurpose;
import com.google.cloud.kms.v1.CryptoKeyVersion.CryptoKeyVersionAlgorithm;

import java.io.IOException;

public class CreateKeyForImport {

  public void createKeyForImport() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "us-east1";
    String keyRingId = "my-key-ring";
    String id = "my-import-key";
    createKeyForImport(projectId, locationId, keyRingId, id);
  }

  // Create a new crypto key to hold imported key versions.
  public void createKeyForImport(String projectId, String locationId,
      String keyRingId, String id)
      throws IOException {
    // Initialize client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests. After
    // completing all of your requests, call the "close" method on the client to
    // safely clean up any remaining background resources.
    try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
      // Build the parent name from the project, location, and key ring.
      KeyRingName keyRingName = KeyRingName.of(projectId, locationId, keyRingId);

      // Create the crypto key.
      CryptoKey createdKey = client.createCryptoKey(
          CreateCryptoKeyRequest.newBuilder()
              .setParent(keyRingName.toString())
              .setCryptoKeyId(id)
              .setCryptoKey(CryptoKey.newBuilder()
                  .setPurpose(CryptoKeyPurpose.ASYMMETRIC_SIGN)
                  .setVersionTemplate(
                      CryptoKeyVersionTemplate.newBuilder()
                          .setProtectionLevel(ProtectionLevel.HSM)
                          .setAlgorithm(CryptoKeyVersionAlgorithm.EC_SIGN_P256_SHA256))
                  // Ensure that only imported versions may be
                  // added to this key.
                  .setImportOnly(true))
              .setSkipInitialVersionCreation(true)
              .build());

      System.out.printf("Created crypto key %s%n", createdKey.getName());
    }
  }
}
// [END kms_create_crypto_key]
