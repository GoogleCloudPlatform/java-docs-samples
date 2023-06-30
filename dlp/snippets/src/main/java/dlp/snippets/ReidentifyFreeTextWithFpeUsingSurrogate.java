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

// [START dlp_reidentify_free_text_with_fpe_using_surrogate]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.common.io.BaseEncoding;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.CryptoKey;
import com.google.privacy.dlp.v2.CryptoReplaceFfxFpeConfig;
import com.google.privacy.dlp.v2.CustomInfoType;
import com.google.privacy.dlp.v2.DeidentifyConfig;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeTransformations;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.PrimitiveTransformation;
import com.google.privacy.dlp.v2.ReidentifyContentRequest;
import com.google.privacy.dlp.v2.ReidentifyContentResponse;
import com.google.privacy.dlp.v2.UnwrappedCryptoKey;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class ReidentifyFreeTextWithFpeUsingSurrogate {
  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.

    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The string to de-identify.
    String textToDeIdentify = "My phone number is 4359916731";
    // The base64-encoded AES-256 key to use.
    String base64EncodedKey = "your-base64-encoded-key";

    // Obtain the de-identified text.
    String textToReIdentify =
        DeidentifyFreeTextWithFpeUsingSurrogate.deIdentifyWithFpeSurrogate(
            projectId, textToDeIdentify, base64EncodedKey);

    reIdentifyWithFpeSurrogate(projectId, textToReIdentify, base64EncodedKey);
  }

  // Re-identifies sensitive data in a string that was encrypted by Format Preserving Encryption
  // (FPE) with surrogate type.
  public static void reIdentifyWithFpeSurrogate(
      String projectId, String textToReIdentify, String unwrappedKey) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Specify what content you want the service to re-identify.
      ContentItem contentItem = ContentItem.newBuilder().setValue(textToReIdentify).build();
      CustomInfoType.SurrogateType surrogateType =
          CustomInfoType.SurrogateType.newBuilder().build();

      // Specify the surrogate type used at time of de-identification.
      InfoType surrogateInfoType = InfoType.newBuilder().setName("PHONE_TOKEN").build();

      CustomInfoType customInfoType =
          CustomInfoType.newBuilder()
              .setInfoType(surrogateInfoType)
              .setSurrogateType(surrogateType)
              .build();
      InspectConfig inspectConfig =
          InspectConfig.newBuilder().addCustomInfoTypes(customInfoType).build();

      // Specify an unwrapped crypto key.
      UnwrappedCryptoKey unwrappedCryptoKey =
          UnwrappedCryptoKey.newBuilder()
              .setKey(ByteString.copyFrom(BaseEncoding.base64().decode(unwrappedKey)))
              .build();
      CryptoKey cryptoKey = CryptoKey.newBuilder().setUnwrapped(unwrappedCryptoKey).build();

      // Specify how to decrypt the previously de-identified information.
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

      InfoTypeTransformations.InfoTypeTransformation infoTypeTransformation =
          InfoTypeTransformations.InfoTypeTransformation.newBuilder()
              .setPrimitiveTransformation(primitiveTransformation)
              .build();

      InfoTypeTransformations transformations =
          InfoTypeTransformations.newBuilder().addTransformations(infoTypeTransformation).build();

      DeidentifyConfig reidentifyConfig =
          DeidentifyConfig.newBuilder().setInfoTypeTransformations(transformations).build();

      // Combine configurations into a request for the service.
      ReidentifyContentRequest request =
          ReidentifyContentRequest.newBuilder()
              .setParent(LocationName.of(projectId, "global").toString())
              .setItem(contentItem)
              .setInspectConfig(inspectConfig)
              .setReidentifyConfig(reidentifyConfig)
              .build();
      // Send the request and receive response from the service.
      ReidentifyContentResponse response = dlp.reidentifyContent(request);

      // Print the results.
      System.out.println("Text after re-identification: " + response.getItem().getValue());
    }
  }
}
// [END dlp_reidentify_free_text_with_fpe_using_surrogate]
