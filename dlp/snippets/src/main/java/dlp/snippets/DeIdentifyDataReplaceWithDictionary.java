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

// [START dlp_deidentify_dictionary_replacement]


import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.CustomInfoType.Dictionary.WordList;
import com.google.privacy.dlp.v2.DeidentifyConfig;
import com.google.privacy.dlp.v2.DeidentifyContentRequest;
import com.google.privacy.dlp.v2.DeidentifyContentResponse;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeTransformations;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.PrimitiveTransformation;
import com.google.privacy.dlp.v2.ReplaceDictionaryConfig;
import java.io.IOException;

public class DeIdentifyDataReplaceWithDictionary {
  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The string to de-identify
    String textToDeIdentify =
        "My name is Charlie and email address is charlie@example.com.";
    deidentifyDataReplaceWithDictionary(projectId, textToDeIdentify);
  }

  // Performs data de-identification by replacing identified email addresses in a given text with
  // randomly selected values from a dictionary.
  public static void deidentifyDataReplaceWithDictionary(String projectId, String textToDeIdentify)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Specify what content you want the service to de-identify.
      ContentItem item = ContentItem.newBuilder().setValue(textToDeIdentify).build();

      // Specify the type of info the inspection will look for.
      // See https://cloud.google.com/dlp/docs/infotypes-reference for complete list of info types
      InfoType infoType = InfoType.newBuilder().setName("EMAIL_ADDRESS").build();
      InspectConfig inspectConfig = InspectConfig.newBuilder().addInfoTypes(infoType).build();

      // Specify list of value which will randomly replace identified email addresses.
      WordList wordList =
          WordList.newBuilder().addWords("izumi@example.com").addWords("alex@example.com").build();

      // Specify the dictionary to use for selecting replacement values for the finding.
      ReplaceDictionaryConfig replaceDictionaryConfig =
          ReplaceDictionaryConfig.newBuilder().setWordList(wordList).build();

      // Define type of de-identification as replacement with items from dictionary.
      PrimitiveTransformation primitiveTransformation =
          PrimitiveTransformation.newBuilder()
              .setReplaceDictionaryConfig(replaceDictionaryConfig)
              .build();

      InfoTypeTransformations.InfoTypeTransformation transformation =
          InfoTypeTransformations.InfoTypeTransformation.newBuilder()
              .addInfoTypes(infoType)
              .setPrimitiveTransformation(primitiveTransformation)
              .build();

      DeidentifyConfig deidentifyConfig =
          DeidentifyConfig.newBuilder()
              .setInfoTypeTransformations(
                  InfoTypeTransformations.newBuilder().addTransformations(transformation))
              .build();

      // Combine configurations into a request for the service.
      DeidentifyContentRequest request =
          DeidentifyContentRequest.newBuilder()
              .setParent(LocationName.of(projectId, "global").toString())
              .setItem(item)
              .setDeidentifyConfig(deidentifyConfig)
              .setInspectConfig(inspectConfig)
              .build();

      // Use the client to send the API request.
      DeidentifyContentResponse response = dlp.deidentifyContent(request);

      // Parse the response and process results.
      System.out.print("Text after de-identification: " + response.getItem().getValue());
    }
  }
}
// [END dlp_deidentify_dictionary_replacement]
