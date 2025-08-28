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

package genai.textgeneration;

// [START googlegenaisdk_textgen_with_txt_stream]

import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;

public class TextGenerationWithTextStream {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String contents = "Why is the sky blue?";
    String modelId = "gemini-2.5-flash";
    generateContent(modelId, contents);
  }

  // Generates text stream with text input
  public static String generateContent(String modelId, String contents) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      StringBuilder responseTextBuilder = new StringBuilder();

      try (ResponseStream<GenerateContentResponse> responseStream =
          client.models.generateContentStream(modelId, contents, null)) {

        for (GenerateContentResponse chunk : responseStream) {
          System.out.print(chunk.text());
          responseTextBuilder.append(chunk.text());
        }
      }
      // Example response:
      // The sky appears blue due to a phenomenon called **Rayleigh scattering**. Here's
      // a breakdown of why:
      // ...
      return responseTextBuilder.toString();
    }
  }
}
// [END googlegenaisdk_textgen_with_txt_stream]
