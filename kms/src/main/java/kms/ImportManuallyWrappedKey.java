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

// [START kms_import_manually_wrapped_key]
import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.cloud.kms.v1.CryptoKeyVersion;
import com.google.cloud.kms.v1.ImportCryptoKeyVersionRequest;
import com.google.cloud.kms.v1.ImportJob;
import com.google.cloud.kms.v1.ImportJobName;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.crypto.tink.subtle.Kwp;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class ImportManuallyWrappedKey {

    public void importManuallyWrappedKey() throws GeneralSecurityException, IOException {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "your-project-id";
        String locationId = "us-east1";
        String keyRingId = "my-key-ring";
        String cryptoKeyId = "my-crypto-key";
        String importJobId = "my-import-job";
        importManuallyWrappedKey(projectId, locationId, keyRingId, cryptoKeyId,
                importJobId);
    }

    // Generates and imports local key material into Cloud KMS.
    public void importManuallyWrappedKey(String projectId, String locationId,
            String keyRingId, String cryptoKeyId,
            String importJobId) throws GeneralSecurityException, IOException {

        // Generate a new ECDSA keypair, and format the private key as PKCS #8 DER.
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair kp = generator.generateKeyPair();
        byte[] privateBytes = kp.getPrivate().getEncoded();

        // Initialize client that will be used to send requests. This client only
        // needs to be created once, and can be reused for multiple requests. After
        // completing all of your requests, call the "close" method on the client to
        // safely clean up any remaining background resources.
        try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
            // Build the crypto key and import job names from the project, location,
            // key ring, and ID.
            CryptoKeyName cryptoKeyName = CryptoKeyName.of(projectId, locationId, keyRingId, cryptoKeyId);
            ImportJobName importJobName = ImportJobName.of(projectId, locationId, keyRingId, importJobId);

            // Generate a temporary 32-byte key for AES-KWP and wrap the key material.
            byte[] kwpKey = new byte[32];
            new SecureRandom().nextBytes(kwpKey);
            Kwp kwp = new Kwp(kwpKey);
            byte[] wrappedTargetKey = kwp.wrap(privateBytes);

            // Retrieve the public key from the import job.
            ImportJob importJob = client.getImportJob(importJobName);
            String publicKeyStr = importJob.getPublicKey().getPem();
            // Manually convert PEM to DER. :-(
            publicKeyStr = publicKeyStr.replace("-----BEGIN PUBLIC KEY-----", "");
            publicKeyStr = publicKeyStr.replace("-----END PUBLIC KEY-----", "");
            publicKeyStr = publicKeyStr.replaceAll("\n", "");
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(publicKeyBytes));

            // Wrap the KWP key using the import job key.
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey,
                    new OAEPParameterSpec("SHA-1", "MGF1",
                            MGF1ParameterSpec.SHA1,
                            PSource.PSpecified.DEFAULT));
            byte[] wrappedWrappingKey = cipher.doFinal(kwpKey);

            // Concatenate the wrapped KWP key and the wrapped target key.
            ByteString combinedWrappedKeys = ByteString.copyFrom(wrappedWrappingKey)
                    .concat(ByteString.copyFrom(wrappedTargetKey));

            // Import the wrapped key material.
            CryptoKeyVersion version = client.importCryptoKeyVersion(
                    ImportCryptoKeyVersionRequest.newBuilder()
                            .setParent(cryptoKeyName.toString())
                            .setImportJob(importJobName.toString())
                            .setAlgorithm(CryptoKeyVersion.CryptoKeyVersionAlgorithm.EC_SIGN_P256_SHA256)
                            .setRsaAesWrappedKey(combinedWrappedKeys)
                            .build());

            System.out.printf("Imported: %s%n", version.getName());
        }
    }
}
// [END kms_import_manually_wrapped_key]
