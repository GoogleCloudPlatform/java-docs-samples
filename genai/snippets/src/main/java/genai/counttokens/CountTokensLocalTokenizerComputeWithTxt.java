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

// [START googlegenaisdk_counttoken_localtokenizer_compute_with_txt]

import com.google.genai.LocalTokenizer;
import com.google.genai.types.ComputeTokensResult;
import com.google.genai.types.TokensInfo;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class CountTokensLocalTokenizerComputeWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    computeTokens(modelId);
  }

  // Computes tokens with Local Tokenizer and a text input
  public static Optional<List<TokensInfo>> computeTokens(String modelId) {
    LocalTokenizer tokenizer = new LocalTokenizer(modelId);
    ComputeTokensResult result =
        tokenizer.computeTokens("What's the longest word in the English language?");

    result.tokensInfo().ifPresent(tokensInfoList -> {
      for (TokensInfo info : tokensInfoList) {
        info.role().ifPresent(role -> System.out.println("role: " + role));
        info.tokenIds().ifPresent(tokenIds -> System.out.println("tokenIds: " + tokenIds));
        // Print tokens input as strings since they are in a form of byte array.
        System.out.println("tokens: ");
        info.tokens().ifPresent(tokens ->
                tokens.forEach(token ->
                        System.out.println(new String(token, StandardCharsets.UTF_8))
                )
        );
      }
    });
    // Example response:
    // role: user
    // tokenIds: [3689, 236789, 236751, 506, 27801, 3658, 528, 506, 5422, 5192, 236881]
    // tokens:
    // What
    // '
    // s
    // the
    // longest
    // ...
    return result.tokensInfo();
  }
}
// [END googlegenaisdk_counttoken_localtokenizer_compute_with_txt]
