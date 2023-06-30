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

// [START dlp_deidentify_free_text_with_fpe_using_surrogate]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.common.io.BaseEncoding;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.CryptoKey;
import com.google.privacy.dlp.v2.CryptoReplaceFfxFpeConfig;
import com.google.privacy.dlp.v2.DeidentifyConfig;
import com.google.privacy.dlp.v2.DeidentifyContentRequest;
import com.google.privacy.dlp.v2.DeidentifyContentResponse;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeTransformations;
import com.google.privacy.dlp.v2.InfoTypeTransformations.InfoTypeTransformation;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.PrimitiveTransformation;
import com.google.privacy.dlp.v2.UnwrappedCryptoKey;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class DeidentifyFreeTextWithFpeUsingSurrogate {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.

    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The string to de-identify.
    String textToDeIdentify = "My phone number is 4359916732";
    // The base64-encoded key to use.
    String base64EncodedKey = "your-base64-encoded-key";

    deIdentifyWithFpeSurrogate(projectId, textToDeIdentify, base64EncodedKey);
  }

  /**
   * Uses the Data Loss Prevention API to deidentify sensitive data in a string using Format
   * Preserving Encryption (FPE).The encryption is performed with an unwrapped key.
   *
   * @param projectId The Google Cloud project id to use as a parent resource.
   * @param textToDeIdentify The string to deidentify.
   * @param unwrappedKey The base64-encoded AES-256 key to use.
   */
  public static String deIdentifyWithFpeSurrogate(
      String projectId, String textToDeIdentify, String unwrappedKey) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Set the text to be de-identified.
      ContentItem contentItem = ContentItem.newBuilder().setValue(textToDeIdentify).build();

      // Specify the InfoType the inspection will look for.
      // See https://cloud.google.com/dlp/docs/infotypes-reference for complete list of info types
      InfoType infoType = InfoType.newBuilder()
              .setName("PHONE_NUMBER").build();

      InspectConfig inspectConfig =
          InspectConfig.newBuilder()
                  .addAllInfoTypes(Collections.singletonList(infoType)).build();

      // Specify an unwrapped crypto key.
      UnwrappedCryptoKey unwrappedCryptoKey =
          UnwrappedCryptoKey.newBuilder()
              .setKey(ByteString.copyFrom(BaseEncoding.base64().decode(unwrappedKey)))
              .build();

      CryptoKey cryptoKey = CryptoKey.newBuilder().setUnwrapped(unwrappedCryptoKey).build();

      InfoType surrogateInfoType = InfoType.newBuilder().setName("PHONE_TOKEN").build();

      // Specify how the info from the inspection should be encrypted.
      CryptoReplaceFfxFpeConfig cryptoReplaceFfxFpeConfig =
          CryptoReplaceFfxFpeConfig.newBuilder()
              .setCryptoKey(cryptoKey)
              // Set of characters in the input text. For more info, see
              // https://cloud.google.com/dlp/docs/reference/rest/v2/organizations.deidentifyTemplates#DeidentifyTemplate.FfxCommonNativeAlphabet
              .setCommonAlphabet(CryptoReplaceFfxFpeConfig.FfxCommonNativeAlphabet.NUMERIC)
              .setSurrogateInfoType(surrogateInfoType)
              .build();

      PrimitiveTransformation primitiveTransformation =
          PrimitiveTransformation.newBuilder()
              .setCryptoReplaceFfxFpeConfig(cryptoReplaceFfxFpeConfig)
              .build();

      InfoTypeTransformation infoTypeTransformation =
          InfoTypeTransformation.newBuilder()
              .setPrimitiveTransformation(primitiveTransformation)
              .build();

      InfoTypeTransformations transformations =
          InfoTypeTransformations.newBuilder()
                  .addTransformations(infoTypeTransformation).build();

      DeidentifyConfig deidentifyConfig =
          DeidentifyConfig.newBuilder()
                  .setInfoTypeTransformations(transformations).build();

      // Combine configurations into a request for the service.
      DeidentifyContentRequest request =
          DeidentifyContentRequest.newBuilder()
              .setParent(LocationName.of(projectId, "global").toString())
              .setItem(contentItem)
              .setInspectConfig(inspectConfig)
              .setDeidentifyConfig(deidentifyConfig)
              .build();

      // Send the request and receive response from the service.
      DeidentifyContentResponse response = dlp.deidentifyContent(request);

      // Print the results.
      System.out.println("Text after de-identification: " + response.getItem().getValue());

      return response.getItem().getValue();
    }
  }
}

// [END dlp_deidentify_free_text_with_fpe_using_surrogate]
