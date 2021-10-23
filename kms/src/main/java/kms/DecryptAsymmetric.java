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

// [START kms_decrypt_asymmetric]
import com.google.cloud.kms.v1.AsymmetricDecryptRequest;
import com.google.cloud.kms.v1.AsymmetricDecryptResponse;
import com.google.cloud.kms.v1.CryptoKeyVersionName;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.protobuf.ByteString;
import com.google.protobuf.Int64Value;
import java.io.IOException;

public class DecryptAsymmetric {

  public void decryptAsymmetric() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "us-east1";
    String keyRingId = "my-key-ring";
    String keyId = "my-key";
    String keyVersionId = "123";
    byte[] ciphertext = null;
    decryptAsymmetric(projectId, locationId, keyRingId, keyId, keyVersionId, ciphertext);
  }

  // Decrypt data that was encrypted using the public key component of the given
  // key version.
  public void decryptAsymmetric(
      String projectId,
      String locationId,
      String keyRingId,
      String keyId,
      String keyVersionId,
      byte[] ciphertext)
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

      // Optional, but recommended: compute ciphertext's CRC32C. See helpers below.
      long ciphertextCrc32c = getCrc32cAsLong(ciphertext);

      // Decrypt the ciphertext.
      AsymmetricDecryptRequest request =
          AsymmetricDecryptRequest.newBuilder()
              .setName(keyVersionName.toString())
              .setCiphertext(ByteString.copyFrom(ciphertext))
              .setCiphertextCrc32C(
                  Int64Value.newBuilder().setValue(ciphertextCrc32c).build())
              .build();
      AsymmetricDecryptResponse response = client.asymmetricDecrypt(request);

      // Optional, but recommended: perform integrity verification on response.
      // For more details on ensuring E2E in-transit integrity to and from Cloud KMS visit:
      // https://cloud.google.com/kms/docs/data-integrity-guidelines
      if (!response.getVerifiedCiphertextCrc32C()) {
        throw new IOException("AsymmetricDecrypt: request to server corrupted");
      }

      if (!crcMatches(response.getPlaintextCrc32C().getValue(),
          response.getPlaintext().toByteArray())) {
        throw new IOException("AsymmetricDecrypt: response from server corrupted");
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
// [END kms_decrypt_asymmetric]
