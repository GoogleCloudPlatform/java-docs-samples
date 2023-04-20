/*
 * Copyright 2023 Google LLC
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
// NOTE: The library nimbusds is NOT endorsed for anything beyond conversion to JWK.
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class ConvertPublicKeyToJwk {

  public void convertPublicKey() throws IOException, GeneralSecurityException, JOSEException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "us-east1";
    String keyRingId = "my-key-ring";
    String keyId = "my-key";
    String keyVersionId = "123";
    convertPublicKey(projectId, locationId, keyRingId, keyId, keyVersionId);
  }

  // (Get and) Convert the public key associated with an asymmetric key.
  public void convertPublicKey(
      String projectId, String locationId, String keyRingId, String keyId, String keyVersionId)
      throws IOException, GeneralSecurityException, JOSEException {
    // Initialize client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests. After
    // completing all of your requests, call the "close" method on the client to
    // safely clean up any remaining background resources.
    try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
      // Build the key version name from the project, location, key ring, key,
      // and key version.
      CryptoKeyVersionName keyVersionName =
          CryptoKeyVersionName.of(projectId, locationId, keyRingId, keyId, keyVersionId);

      // Get the public key and convert it to JWK format.
      PublicKey publicKey = client.getPublicKey(keyVersionName);
      JWK jwk = JWK.parseFromPEMEncodedObjects(publicKey.getPem());
      System.out.println(jwk.toJSONString());
    }
  }
}
 // [END kms_get_public_key]
