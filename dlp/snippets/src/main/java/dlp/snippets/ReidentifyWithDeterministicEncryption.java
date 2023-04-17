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

package dlp.snippets;

// [START dlp_reidentify_deterministic]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.CryptoDeterministicConfig;
import com.google.privacy.dlp.v2.CryptoKey;
import com.google.privacy.dlp.v2.CustomInfoType;
import com.google.privacy.dlp.v2.DeidentifyConfig;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeTransformations;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.KmsWrappedCryptoKey;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.PrimitiveTransformation;
import com.google.privacy.dlp.v2.ReidentifyContentRequest;
import com.google.privacy.dlp.v2.ReidentifyContentResponse;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;

public class ReidentifyWithDeterministicEncryption {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.

    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The string to de-identify.
    String textToIdentify = "My SSN is 372819127";
    // The encrypted ('wrapped') AES-256 key to use.
    // This key should be encrypted using the Cloud KMS key specified by key_name.
    String wrappedKey = "YOUR_ENCRYPTED_AES_256_KEY";
    // The name of the Cloud KMS key used to encrypt ('wrap') the AES-256 key.
    String kmsKeyName =
        "projects/YOUR_PROJECT/"
            + "locations/YOUR_KEYRING_REGION/"
            + "keyRings/YOUR_KEYRING_NAME/"
            + "cryptoKeys/YOUR_KEY_NAME";
    // The string to re-identify.
    String textToReIdentify =
        DeIdenitfyWithDeterministicEncryption.deIdentifyWithDeterministicEncryption(
            projectId, textToIdentify, wrappedKey, kmsKeyName);
    reIdentifyWithDeterminsiticEncryption(projectId, textToReIdentify, wrappedKey, kmsKeyName);
  }

  public static void reIdentifyWithDeterminsiticEncryption(
      String projectId, String textToReIdentify, String wrappedKey, String key) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Specify what content you want the service to ReIdentify
      ContentItem contentItem = ContentItem.newBuilder().setValue(textToReIdentify).build();

      CustomInfoType.SurrogateType surrogateType =
          CustomInfoType.SurrogateType.newBuilder().build();

      // Specify the surrogate type used at time of de-identification.
      InfoType surrogateInfoType = InfoType.newBuilder()
              .setName("SSN_TOKEN")
              .build();

      CustomInfoType customInfoType = CustomInfoType.newBuilder()
              .setInfoType(surrogateInfoType)
              .setSurrogateType(surrogateType)
              .build();

      InspectConfig inspectConfig = InspectConfig.newBuilder()
              .addCustomInfoTypes(customInfoType)
              .build();

      // Specify an encrypted AES-256 key and the name of the Cloud KMS key that encrypted it.
      KmsWrappedCryptoKey unwrappedCryptoKey = KmsWrappedCryptoKey.newBuilder()
              .setWrappedKey(
                  ByteString.copyFrom(
                          Base64.decodeBase64(wrappedKey.getBytes(StandardCharsets.UTF_8))))
              .setCryptoKeyName(key)
              .build();
      CryptoKey cryptoKey = CryptoKey.newBuilder()
              .setKmsWrapped(unwrappedCryptoKey)
              .build();

      CryptoDeterministicConfig cryptoDeterministicConfig = CryptoDeterministicConfig.newBuilder()
              .setSurrogateInfoType(surrogateInfoType)
              .setCryptoKey(cryptoKey)
              .build();

      PrimitiveTransformation primitiveTransformation = PrimitiveTransformation.newBuilder()
              .setCryptoDeterministicConfig(cryptoDeterministicConfig)
              .build();

      InfoTypeTransformations.InfoTypeTransformation infoTypeTransformation =
          InfoTypeTransformations.InfoTypeTransformation.newBuilder()
              .setPrimitiveTransformation(primitiveTransformation)
              .build();

      InfoTypeTransformations transformations = InfoTypeTransformations.newBuilder()
              .addTransformations(infoTypeTransformation)
              .build();

      DeidentifyConfig deidentifyConfig = DeidentifyConfig.newBuilder()
              .setInfoTypeTransformations(transformations)
              .build();

      // Combine configurations into a request for the service.
      ReidentifyContentRequest request = ReidentifyContentRequest.newBuilder()
              .setParent(LocationName.of(projectId, "global").toString())
              .setItem(contentItem)
              .setInspectConfig(inspectConfig)
              .setReidentifyConfig(deidentifyConfig)
              .build();

      // Send the request and receive response from the service.
      ReidentifyContentResponse response = dlp.reidentifyContent(request);

      // Print the results.
      System.out.println("Text after re-identification: " + response.getItem().getValue());
    }
  }
}

// [END dlp_reidentify_deterministic]
