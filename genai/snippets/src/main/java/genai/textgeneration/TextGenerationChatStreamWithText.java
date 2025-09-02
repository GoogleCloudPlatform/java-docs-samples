/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package genai.textgeneration;

// [START googlegenaisdk_textgen_chat_stream_with_txt]

import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;

public class TextGenerationChatStreamWithText {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    generateContent(modelId);
  }

  // Shows how to create a new chat session stream
  public static String generateContent(String modelId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      Chat chatSession = client.chats.create(modelId);
      StringBuilder responseTextBuilder = new StringBuilder();

      try (ResponseStream<GenerateContentResponse> response =
          chatSession.sendMessageStream("Why is the sky blue?")) {

        for (GenerateContentResponse chunk : response) {
          System.out.println(chunk.text());
          responseTextBuilder.append(chunk.text());
        }

      }
      // Example response:
      //
      // The sky is blue primarily due to a phenomenon called **Rayleigh scattering**,
      // named after the British physicist Lord Rayleigh. Here's a breakdown of how...
      return responseTextBuilder.toString();
    }
  }
}
// [END googlegenaisdk_textgen_chat_stream_with_txt]