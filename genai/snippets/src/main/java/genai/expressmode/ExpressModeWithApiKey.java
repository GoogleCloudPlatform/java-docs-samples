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

package genai.expressmode;

// [START googlegenaisdk_vertexai_express_mode]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;

public class ExpressModeWithApiKey {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    String apiKey = "YOUR_API_KEY";
    generateContent(modelId, apiKey);
  }

  // Generates content with Vertex AI Api key.
  public static String generateContent(String modelId, String apiKey) {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client = Client.builder().apiKey(apiKey).vertexAI(true).build()) {

      GenerateContentResponse response =
          client.models.generateContent(
              modelId, "Explain bubble sort to me.", GenerateContentConfig.builder().build());

      System.out.print(response.text());
      // Example response:
      // Bubble sort is one of the simplest sorting algorithms. It's often used to introduce the
      // concept of sorting because its logic is very straightforward.
      //
      // Imagine you have a list of numbers that you want to put in order, like `[5, 1, 4, 2, 8]`.
      // ...
      return response.text();
    }
  }
}
// [END googlegenaisdk_vertexai_express_mode]
