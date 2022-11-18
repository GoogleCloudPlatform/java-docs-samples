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

package kms;

// [START kms_get_public_key]
import com.google.cloud.kms.v1.CryptoKeyVersionName;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.kms.v1.PublicKey;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.protobuf.Int64Value;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class GetPublicKey {

  public void getPublicKey() throws IOException, GeneralSecurityException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "us-east1";
    String keyRingId = "my-key-ring";
    String keyId = "my-key";
    String keyVersionId = "123";
    getPublicKey(projectId, locationId, keyRingId, keyId, keyVersionId);
  }

  // Get the public key associated with an asymmetric key.
  public void getPublicKey(
      String projectId, String locationId, String keyRingId, String keyId, String keyVersionId)
      throws IOException, GeneralSecurityException {
    // Initialize client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests. After
    // completing all of your requests, call the "close" method on the client to
    // safely clean up any remaining background resources.
    try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
      // Build the key version name from the project, location, key ring, key,
      // and key version.
      CryptoKeyVersionName keyVersionName =
          CryptoKeyVersionName.of(projectId, locationId, keyRingId, keyId, keyVersionId);

      // Get the public key.
      PublicKey publicKey = client.getPublicKey(keyVersionName);

      // Optional, but recommended: perform integrity verification on response.
      // For more details on ensuring E2E in-transit integrity to and from Cloud KMS visit:
      // https://cloud.google.com/kms/docs/data-integrity-guidelines
      if (!publicKey.getName().equals(keyVersionName.toString())) {
        throw new IOException("GetPublicKey: request to server corrupted");
      }

      // See helper below.
      if (!crcMatches(publicKey.getPemCrc32C().getValue(),
          publicKey.getPemBytes().toByteArray())) {
        throw new IOException("GetPublicKey: response from server corrupted");
      }

      System.out.printf("Public key: %s%n", publicKey.getPem());
    }
  }

  private long getCrc32cAsLong(byte[] data) {
    return Hashing.crc32c().hashBytes(data).padToLong();
  }

  private boolean crcMatches(long expectedCrc, byte[] data) {
    return expectedCrc == getCrc32cAsLong(data);
  }
}
// [END kms_get_public_key]
