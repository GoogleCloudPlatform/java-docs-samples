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

// [START googlegenaisdk_thinking_with_txt]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;

public class ThinkingWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-pro";
    generateContent(modelId);
  }

  // Generates text with thinking model and text input
  public static String generateContent(String modelId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      GenerateContentResponse response =
          client.models.generateContent(
              modelId, "solve x^2 + 4x + 4 = 0", GenerateContentConfig.builder().build());

      System.out.println(response.text());
      // Example response:
      // There are a couple of common ways to solve this quadratic equation.
      //
      // The equation is: **x² + 4x + 4 = 0**
      //
      // ### Method 1: Factoring (The Easiest Method for this Problem)
      //
      // This equation is a special case called a "perfect square trinomial".
      //
      // 1.  **Find two numbers** that multiply to the last term (4) and add up to the middle term
      // (4).
      //    *   The numbers are +2 and +2. (Since 2 * 2 = 4 and 2 + 2 = 4)
      //
      // 2.  **Factor the equation** using these numbers.
      //    *   (x + 2)(x + 2) = 0
      //    *   This can be written as: (x + 2)² = 0
      //
      // 3.  **Solve for x.**
      //    *   If (x + 2)² is zero, then (x + 2) must be zero.
      //    *   x + 2 = 0
      //    *   x = -2
      //
      // ### Method 2: The Quadratic Formula
      //
      // You can use the quadratic formula for any equation in the form ax² + bx + c = 0.
      //
      // The formula is: **x = [-b ± √(b² - 4ac)] / 2a**
      //
      // 1.  **Identify a, b, and c** from your equation (x² + 4x + 4 = 0).
      //    *   a = 1
      //    *   b = 4
      //    *   c = 4
      //
      // 2.  **Plug the values into the formula.**
      //    *   x = [-4 ± √(4² - 4 * 1 * 4)] / (2 * 1)
      //
      // 3.  **Simplify.**
      //    *   x = [-4 ± √(16 - 16)] / 2
      //    *   x = [-4 ± √0] / 2
      //    *   x = -4 / 2
      //    *   x = -2
      //
      // Both methods give the same solution.
      //
      // ---
      //
      // ### Final Answer
      //
      // The solution is **x = -2**.
      return response.text();
    }
  }
}
// [END googlegenaisdk_thinking_with_txt]
