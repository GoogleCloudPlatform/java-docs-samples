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

// [START googlegenaisdk_textgen_config_with_txt]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;

public class TextGenerationConfigWithText {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    generateContent(modelId);
  }

  // Generates text with text input and extra configurations
  public static String generateContent(String modelId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      // Set optional configuration parameters
      GenerateContentConfig contentConfig =
          GenerateContentConfig.builder()
              .temperature(0.0F)
              .candidateCount(0)
              .responseMimeType("application/json")
              .topP(0.95F)
              .topK(20F)
              .seed(5)
              .maxOutputTokens(500)
              .stopSequences("STOP!")
              .presencePenalty(0.0F)
              .frequencyPenalty(0.0F)
              .build();

      // Generate content using optional configuration
      GenerateContentResponse response =
          client.models.generateContent(modelId, "Why is the sky blue?", contentConfig);

      System.out.print(response.text());
      // Example response:
      // {
      //  "explanation": "The sky appears blue due to a phenomenon called Rayleigh scattering.
      // Sunlight, which appears white, is actually composed of all the colors of the rainbow...
      // }
      return response.text();
    }
  }
}
// [END googlegenaisdk_textgen_config_with_txt]