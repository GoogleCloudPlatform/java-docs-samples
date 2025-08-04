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

package genai.gemini.textgeneration;

// [START googlegenaisdk_textgen_async_with_txt]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import java.util.concurrent.CompletableFuture;

public class TextGenerationAsyncWithText {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    System.out.println(generateContent(modelId));
  }

  // Generates text asynchronously with text input
  public static String generateContent(String modelId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      CompletableFuture<GenerateContentResponse> asyncResponse =
          client.async.models.generateContent(
              modelId, "Compose a song about the adventures of a time-traveling squirrel.", null);

      String response = asyncResponse.join().text();
      System.out.println(response);
      // Example response:
      // (Verse 1)
      // In an oak tree, so leafy and green,
      // Lived Squeaky the squirrel, a critter unseen.
      // Just burying nuts, a routine so grand,
      // ...

      return response;
    }
  }
}
// [END googlegenaisdk_textgen_async_with_txt]
