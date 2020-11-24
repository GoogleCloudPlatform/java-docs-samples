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

// [START kms_sign_asymmetric]
import com.google.cloud.kms.v1.AsymmetricSignRequest;
import com.google.cloud.kms.v1.AsymmetricSignResponse;
import com.google.cloud.kms.v1.CryptoKeyVersionName;
import com.google.cloud.kms.v1.Digest;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.protobuf.ByteString;
import com.google.protobuf.Int64Value;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

public class SignAsymmetric {

  public void signAsymmetric() throws IOException, GeneralSecurityException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "us-east1";
    String keyRingId = "my-key-ring";
    String keyId = "my-key";
    String keyVersionId = "123";
    String message = "my message";
    signAsymmetric(projectId, locationId, keyRingId, keyId, keyVersionId, message);
  }

  // Get the public key associated with an asymmetric key.
  public void signAsymmetric(
      String projectId,
      String locationId,
      String keyRingId,
      String keyId,
      String keyVersionId,
      String message)
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

      // Convert the message into bytes. Cryptographic plaintexts and
      // ciphertexts are always byte arrays.
      byte[] plaintext = message.getBytes(StandardCharsets.UTF_8);

      // Calculate the digest.
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      byte[] hash = sha256.digest(plaintext);

      // Build the digest object.
      Digest digest = Digest.newBuilder().setSha256(ByteString.copyFrom(hash)).build();

      // Optional, but recommended: compute digest's CRC32C. See helper below.
      long digestCrc32c = getCrc32cAsLong(hash);

      // Sign the digest.
      AsymmetricSignRequest request =
          AsymmetricSignRequest.newBuilder()
              .setName(keyVersionName.toString())
              .setDigest(digest)
              .setDigestCrc32C(Int64Value.newBuilder().setValue(digestCrc32c).build())
              .build();
      AsymmetricSignResponse response = client.asymmetricSign(request);

      // Optional, but recommended: perform integrity verification on response.
      // For more details on ensuring E2E in-transit integrity to and from Cloud KMS visit:
      // https://cloud.google.com/kms/docs/data-integrity-guidelines
      if (!response.getVerifiedDigestCrc32C()) {
        throw new IOException("AsymmetricSign: request to server corrupted");
      }

      // See helper below.
      if (!crcMatches(response.getSignatureCrc32C().getValue(),
          response.getSignature().toByteArray())) {
        throw new IOException("AsymmetricSign: response from server corrupted");
      }

      // Get the signature.
      byte[] signature = response.getSignature().toByteArray();

      System.out.printf("Signature %s%n", signature);
    }
  }

  private long getCrc32cAsLong(byte[] data) {
    return Hashing.crc32c().hashBytes(data).padToLong();
  }

  private boolean crcMatches(long expectedCrc, byte[] data) {
    return expectedCrc == getCrc32cAsLong(data);
  }
}
// [END kms_sign_asymmetric]
