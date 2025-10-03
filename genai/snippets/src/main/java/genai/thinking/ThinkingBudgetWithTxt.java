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

package genai.thinking;

// [START googlegenaisdk_thinking_budget_with_txt]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.ThinkingConfig;

public class ThinkingBudgetWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    generateContent(modelId);
  }

  // Generates text controlling the thinking budget
  public static String generateContent(String modelId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      GenerateContentConfig contentConfig =
          GenerateContentConfig.builder()
              .thinkingConfig(ThinkingConfig.builder().thinkingBudget(1024).build())
              .build();

      GenerateContentResponse response =
          client.models.generateContent(modelId, "solve x^2 + 4x + 4 = 0", contentConfig);

      System.out.println(response.text());
      // Example response:
      // To solve the equation $x^2 + 4x + 4 = 0$, we can use several methods:
      //
      // **Method 1: Factoring (Recognizing a Perfect Square Trinomial)**
      //
      // Notice that the left side of the equation is a perfect square trinomial. It fits the form
      // $a^2 + 2ab + b^2 = (a+b)^2$...
      // ...
      // The solution is $x = -2$.

      response
          .usageMetadata()
          .ifPresent(
              metadata -> {
                System.out.println("Token count for thinking: " + metadata.thoughtsTokenCount());
                System.out.println("Total token count: " + metadata.totalTokenCount());
              });
      // Example response:
      // Token count for thinking: Optional[885]
      // Total token count: Optional[1468]
      return response.text();
    }
  }
}
// [END googlegenaisdk_thinking_budget_with_txt]
