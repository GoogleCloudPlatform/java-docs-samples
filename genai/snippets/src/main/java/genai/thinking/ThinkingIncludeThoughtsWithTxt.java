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

public class ThinkingIncludeThoughtsWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-pro";
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
      // We can solve the equation x² + 4x + 4 = 0 using a couple of common methods.
      //
      // ### Method 1: Factoring (The Easiest Method for this Problem)
      // **Recognize the pattern:** The pattern for a perfect square trinomial
      // is a² + 2ab + b² = (a + b)².
      // ...
      // ### Final Answer:
      // The solution is **x = -2**.

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
      // Alright, let's break down this quadratic equation, x² + 4x + 4 = 0. My initial thought is,
      // "classic quadratic."  I'll need to find the values of 'x' that make this equation true. The
      // equation is in standard form, and since the coefficients are relatively small, I
      // immediately suspect that factoring might be the easiest route.  It's worth checking.
      //
      // First, I assessed what I had. *a* is 1, *b* is 4, and *c* is 4. I consider my toolkit.
      // Factoring is the likely first choice, then I can use the quadratic formula as a backup,
      // because that ALWAYS works, and I could use graphing. However, for this, factoring seems the
      // cleanest approach.
      //
      // Okay, factoring. I need two numbers that multiply to *c* (which is 4) and add up to *b*
      // (also 4).  I quickly run through the factor pairs of 4: (1, 4), (-1, -4), (2, 2), (-2, -2).
      //  Aha! 2 and 2 fit the bill. They multiply to 4 *and* add up to 4.  Therefore, I can rewrite
      // the equation as (x + 2)(x + 2) = 0.  That simplifies to (x + 2)² = 0. Perfect square
      // trinomial – nice and tidy. Seeing that pattern from the outset can save a step or two. Now,
      // to solve for *x*:  if (x + 2)² = 0, then x + 2 must equal 0.  Therefore, x = -2. Done.
      //
      // But, for the sake of a full explanation, let's use the quadratic formula as a second
      // method. It's a reliable way to double-check the answer, plus it's good practice.  I plug my
      // *a*, *b*, and *c* values into the formula: x = [-b ± √(b² - 4ac)] / (2a). That gives me  x
      // = [-4 ± √(4² - 4 * 1 * 4)] / (2 * 1). Simplifying under the radical, I get x = [-4 ± √(16 -
      // 16)] / 2. So, x = [-4 ± √0] / 2. The square root of 0 is zero, which is very telling!  When
      // the discriminant (b² - 4ac) is zero, you get one real solution, a repeated root. This means
      // x = -4 / 2, which simplifies to x = -2.  Exactly the same as before.
      //
      // Therefore, the answer is x = -2.  Factoring was the most straightforward route.  For
      // completeness, I showed the solution via the quadratic formula, too. Both approaches lead to
      // the same single solution.  This is a repeated root – a double root, if you will.
      //
      // And to be absolutely sure...let's check our answer! Substitute -2 back into the original
      // equation. (-2)² + 4(-2) + 4 = 4 - 8 + 4 = 0.  Yep, 0 = 0. The solution is correct.
      return response.text();
    }
  }
}
// [END googlegenaisdk_thinking_includethoughts_with_txt]
