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

// [START googlegenaisdk_counttoken_localtokenizer_with_txt]

import com.google.genai.LocalTokenizer;
import com.google.genai.types.CountTokensResult;
import java.util.Optional;

public class CountTokensLocalTokenizerWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    countTokens(modelId);
  }

  // Counts tokens with Local Tokenizer and a text input
  public static Optional<Integer> countTokens(String modelId) {
    LocalTokenizer tokenizer = new LocalTokenizer(modelId);
    CountTokensResult result = tokenizer.countTokens("What's the highest mountain in Africa?");
    System.out.println(result.totalTokens());
    // Example response:
    // Optional[9]
    return result.totalTokens();
  }
}
// [END googlegenaisdk_counttoken_localtokenizer_with_txt]
