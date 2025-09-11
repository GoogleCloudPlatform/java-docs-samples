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
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;

public class ThinkingWithText {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    generateContent(modelId);
  }

  // Generates text with text input
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
          client.models.generateContent(modelId, "solve x^2 + 4x + 4 = 0", null);

      System.out.println(response.text());
      // Example response:
      // To solve the equation $x^2 + 4x + 4 = 0$, we can use several methods:
      //
      // **Method 1: Factoring (Recognizing a Perfect Square Trinomial)**
      //
      // Observe the structure of the equation: it is a quadratic trinomial.
      // We look for two numbers that multiply to $c$ (4) and add up to $b$ (4).
      // These numbers are 2 and 2 ($2 \times 2 = 4$ and $2 + 2 = 4$).
      //
      // So, the quadratic expression can be factored as $(x+2)(x+2)$.
      // This is equivalent to $(x+2)^2$.
      //
      // Now, the equation becomes:
      // $(x+2)^2 = 0$
      //
      // To solve for $x$, take the square root of both sides:
      // $\sqrt{(x+2)^2} = \sqrt{0}$
      // $x+2 = 0$
      //
      // Subtract 2 from both sides:
      // $x = -2$
      //
      // This is the only solution, as the quadratic has a repeated root (or a root with
      // multiplicity 2).
      //
      // **Method 2: Using the Quadratic Formula**
      //
      // The quadratic formula solves for $x$ in an equation of the form $ax^2 + bx + c = 0$:
      // $x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}$
      //
      // In our equation, $x^2 + 4x + 4 = 0$, we have:
      // $a = 1$
      // $b = 4$
      // $c = 4$
      //
      // Substitute these values into the formula:
      // $x = \frac{-4 \pm \sqrt{4^2 - 4(1)(4)}}{2(1)}$
      // $x = \frac{-4 \pm \sqrt{16 - 16}}{2}$
      // $x = \frac{-4 \pm \sqrt{0}}{2}$
      // $x = \frac{-4 \pm 0}{2}$
      // $x = \frac{-4}{2}$
      // $x = -2$
      //
      // Both methods yield the same solution.
      //
      // **Solution:**
      // The solution to the equation $x^2 + 4x + 4 = 0$ is $x = -2$.
      return response.text();
    }
  }
}
// [END googlegenaisdk_thinking_with_txt]