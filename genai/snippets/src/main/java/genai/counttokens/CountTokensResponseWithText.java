/*
 * Copyright 2025 Google LLC
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

package genai.counttokens;

// [START googlegenaisdk_counttoken_resp_with_txt]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import com.google.genai.types.HttpOptions;
import java.util.Optional;

public class CountTokensResponseWithText {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    countTokens(modelId);
  }

  // Generates content response usage metadata that contains prompt and response token counts
  public static Optional<GenerateContentResponseUsageMetadata> countTokens(String modelId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      GenerateContentResponse response =
              client.models.generateContent(modelId, "Why is the sky blue?", null);

      response.usageMetadata().ifPresent(System.out::println);
      // Example response:
      // GenerateContentResponseUsageMetadata{cacheTokensDetails=Optional.empty,
      // cachedContentTokenCount=Optional.empty, candidatesTokenCount=Optional[569],
      // candidatesTokensDetails=Optional[[ModalityTokenCount{modality=Optional[TEXT],
      // tokenCount=Optional[569]}]], promptTokenCount=Optional[6],
      // promptTokensDetails=Optional[[ModalityTokenCount{modality=Optional[TEXT],
      // tokenCount=Optional[6]}]], thoughtsTokenCount=Optional[1132],
      // toolUsePromptTokenCount=Optional.empty, toolUsePromptTokensDetails=Optional.empty,
      // totalTokenCount=Optional[1707], trafficType=Optional[ON_DEMAND]}
      return response.usageMetadata();
    }
  }
}
// [END googlegenaisdk_counttoken_resp_with_txt]
