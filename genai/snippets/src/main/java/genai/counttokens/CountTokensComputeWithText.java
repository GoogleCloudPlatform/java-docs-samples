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

// [START googlegenaisdk_counttoken_compute_with_txt]

import com.google.genai.Client;
import com.google.genai.types.ComputeTokensResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.TokensInfo;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class CountTokensComputeWithText {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    computeTokens(modelId);
  }

  // Computes tokens with text input
  public static Optional<List<TokensInfo>> computeTokens(String modelId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      ComputeTokensResponse response = client.models.computeTokens(
              modelId, "What's the longest word in the English language?", null);

      // Print TokensInfo
      response.tokensInfo().ifPresent(tokensInfoList -> {
        for (TokensInfo info : tokensInfoList) {
          info.role().ifPresent(role -> System.out.println("role: " + role));
          info.tokenIds().ifPresent(tokenIds -> System.out.println("tokenIds: " + tokenIds));
          // print tokens input as strings since they are in a form of byte array
          System.out.println("tokens: ");
          info.tokens().ifPresent(tokens ->
              tokens.forEach(token ->
                  System.out.println(new String(token, StandardCharsets.UTF_8))
              )
          );
        }
      });
      // Example response.tokensInfo()
      // role: user
      // tokenIds: [1841, 235303, 235256, 573, 32514, 2204, 575, 573, 4645, 5255, 235336]
      // tokens:
      // What
      // '
      // s
      // the
      return response.tokensInfo();
    }
  }
}
// [END googlegenaisdk_counttoken_compute_with_txt]
