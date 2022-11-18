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

// [START kms_decrypt_symmetric]
import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.cloud.kms.v1.DecryptRequest;
import com.google.cloud.kms.v1.DecryptResponse;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.protobuf.ByteString;
import com.google.protobuf.Int64Value;
import java.io.IOException;



public class DecryptSymmetric {

  public void decryptSymmetric() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "us-east1";
    String keyRingId = "my-key-ring";
    String keyId = "my-key";
    byte[] ciphertext = null;
    decryptSymmetric(projectId, locationId, keyRingId, keyId, ciphertext);
  }

  // Decrypt data that was encrypted using a symmetric key.
  public void decryptSymmetric(
      String projectId, String locationId, String keyRingId, String keyId, byte[] ciphertext)
      throws IOException {
    // Initialize client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests. After
    // completing all of your requests, call the "close" method on the client to
    // safely clean up any remaining background resources.
    try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
      // Build the key version from the project, location, key ring, and key.
      CryptoKeyName keyName = CryptoKeyName.of(projectId, locationId, keyRingId, keyId);

      // Optional, but recommended: compute ciphertext's CRC32C. See helpers below.
      long ciphertextCrc32c = getCrc32cAsLong(ciphertext);

      // Decrypt the ciphertext.
      DecryptRequest request =
          DecryptRequest.newBuilder()
              .setName(keyName.toString())
              .setCiphertext(ByteString.copyFrom(ciphertext))
              .setCiphertextCrc32C(
                  Int64Value.newBuilder().setValue(ciphertextCrc32c).build())
              .build();
      DecryptResponse response = client.decrypt(request);

      // Optional, but recommended: perform integrity verification on response.
      // For more details on ensuring E2E in-transit integrity to and from Cloud KMS visit:
      // https://cloud.google.com/kms/docs/data-integrity-guidelines
      if (!crcMatches(response.getPlaintextCrc32C().getValue(),
          response.getPlaintext().toByteArray())) {
        throw new IOException("Decrypt: response from server corrupted");
      }

      System.out.printf("Plaintext: %s%n", response.getPlaintext().toStringUtf8());
    }
  }

  private long getCrc32cAsLong(byte[] data) {
    return Hashing.crc32c().hashBytes(data).padToLong();
  }

  private boolean crcMatches(long expectedCrc, byte[] data) {
    return expectedCrc == getCrc32cAsLong(data);
  }
}
// [END kms_decrypt_symmetric]
