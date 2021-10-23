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

// [START kms_encrypt_symmetric]
import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.cloud.kms.v1.EncryptRequest;
import com.google.cloud.kms.v1.EncryptResponse;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.protobuf.ByteString;
import com.google.protobuf.Int64Value;
import java.io.IOException;



public class EncryptSymmetric {

  public void encryptSymmetric() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "us-east1";
    String keyRingId = "my-key-ring";
    String keyId = "my-key";
    String plaintext = "Plaintext to encrypt";
    encryptSymmetric(projectId, locationId, keyRingId, keyId, plaintext);
  }

  // Encrypt data with a given key.
  public void encryptSymmetric(
      String projectId, String locationId, String keyRingId, String keyId, String plaintext)
      throws IOException {

    // Initialize client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests. After
    // completing all of your requests, call the "close" method on the client to
    // safely clean up any remaining background resources.
    try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
      // Build the key name from the project, location, key ring, and key.
      CryptoKeyName cryptoKeyName = CryptoKeyName.of(projectId, locationId, keyRingId, keyId);

      // Convert plaintext to ByteString.
      ByteString plaintextByteString = ByteString.copyFromUtf8(plaintext);

      // Optional, but recommended: compute plaintext's CRC32C. See helper below.
      long plaintextCrc32c = getCrc32cAsLong(plaintextByteString.toByteArray());

      // Encrypt the plaintext.
      EncryptRequest request = EncryptRequest.newBuilder()
                               .setName(cryptoKeyName.toString())
                               .setPlaintext(plaintextByteString)
                               .setPlaintextCrc32C(
                                   Int64Value.newBuilder().setValue(plaintextCrc32c).build())
                               .build();
      EncryptResponse response = client.encrypt(request);

      // Optional, but recommended: perform integrity verification on response.
      // For more details on ensuring E2E in-transit integrity to and from Cloud KMS visit:
      // https://cloud.google.com/kms/docs/data-integrity-guidelines
      if (!response.getVerifiedPlaintextCrc32C()) {
        throw new IOException("Encrypt: request to server corrupted");
      }

      // See helper below.
      if (!crcMatches(response.getCiphertextCrc32C().getValue(),
          response.getCiphertext().toByteArray())) {
        throw new IOException("Encrypt: response from server corrupted");
      }

      System.out.printf("Ciphertext: %s%n", response.getCiphertext().toStringUtf8());
    }
  }

  private long getCrc32cAsLong(byte[] data) {
    return Hashing.crc32c().hashBytes(data).padToLong();
  }

  private boolean crcMatches(long expectedCrc, byte[] data) {
    return expectedCrc == getCrc32cAsLong(data);
  }
}
// [END kms_encrypt_symmetric]
