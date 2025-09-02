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

// [START googlegenaisdk_thinking_includethoughts_with_txt]

import com.google.genai.Client;
import com.google.genai.types.Candidate;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.ThinkingConfig;

public class ThinkingIncludeThoughtsWithText {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    generateContent(modelId);
  }

  // Generates text including thoughts in the response
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
              .thinkingConfig(ThinkingConfig.builder().includeThoughts(true).build())
              .build();

      GenerateContentResponse response =
          client.models.generateContent(modelId, "solve x^2 + 4x + 4 = 0", contentConfig);

      System.out.println(response.text());
      // Example response:
      // To solve the equation $x^2 + 4x + 4 = 0$, we can use several methods:
      // ...
      // **Solution:**
      // The solution to the equation $x^2 + 4x + 4 = 0$ is $x = -2$.

      // Get parts of the response and print thoughts
      response
          .candidates()
          .flatMap(candidates -> candidates.stream().findFirst())
          .flatMap(Candidate::content)
          .flatMap(Content::parts)
          .ifPresent(
              parts -> {
                parts.forEach(
                    part -> {
                      if (part.thought().orElse(false)) {
                        part.text().ifPresent(System.out::println);
                      }
                    });
              });
      // Example response:
      // Alright, here's how I'd approach this problem. I'm looking at the quadratic equation $x^2 +
      // 4x + 4 = 0$. Immediately, I recognize a few potential solution paths, the beauty of
      // quadratic equations is the multiple ways to solve them.
      //
      // First, **factoring** seems promising. I mentally scan the expression and see if it can be
      // easily factored, and sure enough, it screams "perfect square trinomial." $x^2$ is clearly a
      // perfect square, and so is $4$. The middle term, $4x$, fits the pattern $2ab$ with $a = x$
      // and $b = 2$. So, I can rewrite the equation as $(x+2)^2 = 0$. Taking the square root of
      // both sides, I get $x+2 = 0$, and therefore $x = -2$. Done.
      //
      // But, just to be thorough and ensure I haven't missed anything, I should also check the
      // **quadratic formula**. I have to, of course, apply the standard formula: $x = \frac{-b \pm
      // \sqrt{b^2 - 4ac}}{2a}$. Here, $a = 1$, $b = 4$, and $c = 4$. Plugging those values in, I
      // get $x = \frac{-4 \pm \sqrt{16 - 16}}{2}$, which simplifies to $x = \frac{-4}{2}$, meaning
      // $x = -2$. Perfect.
      //
      // And finally, as a sanity check, I'll briefly consider **completing the square**.  Well, in
      // this case, I've already essentially done it! The equation is already set up in a perfect
      // square form. The constant term is, effectively, $(4/2)^2 = 4$. Therefore, I can directly
      // proceed to the factored form and solve for x.
      //
      // So, all three methods, factoring, the quadratic formula and completing the square, yield
      // the same result: $x = -2$. The fact that the discriminant, $b^2 - 4ac$, equals 0 tells me
      // that there's only one real solution, a repeated root, which is exactly what I found.
      return response.text();
    }
  }
}
// [END googlegenaisdk_thinking_includethoughts_with_txt]