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

// [START googlegenaisdk_counttoken_with_txt_vid]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.CountTokensResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import java.util.List;
import java.util.Optional;

public class CountTokensWithTextAndVideo {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    countTokens(modelId);
  }

  // Counts tokens with text and video inputs
  public static Optional<Integer> countTokens(String modelId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder().httpOptions(HttpOptions.builder().apiVersion("v1").build()).build()) {

      Content content =
          Content.fromParts(
              Part.fromText("Provide a description of this video"),
              Part.fromUri("gs://cloud-samples-data/generative-ai/video/pixel8.mp4", "video/mp4"));

      CountTokensResponse response = client.models.countTokens(modelId, List.of(content), null);

      System.out.print(response);
      // Example response:
      // CountTokensResponse{totalTokens=Optional[16707], cachedContentTokenCount=Optional.empty}
      return response.totalTokens();
    }
  }
}
// [END googlegenaisdk_counttoken_with_txt_vid]
