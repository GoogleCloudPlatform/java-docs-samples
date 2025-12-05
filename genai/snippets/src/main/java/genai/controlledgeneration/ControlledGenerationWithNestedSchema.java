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

package genai.controlledgeneration;

// [START googlegenaisdk_ctrlgen_with_nested_class_schema]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;

public class ControlledGenerationWithNestedSchema {

  private static final Gson gson = new Gson();

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String contents = "List about 10 home-baked cookies and give them grades based on tastiness.";
    String modelId = "gemini-2.5-flash";
    generateContent(modelId, contents);
  }

  public static String generateContent(String modelId, String contents) {
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      // Enum schema for Grade
      Schema gradeSchema =
          Schema.builder()
              .type(Type.Known.STRING)
              .enum_(List.of("a+", "a", "b", "c", "d", "f"))
              .build();

      // Schema for Recipe object
      Schema recipeSchema =
          Schema.builder()
              .type(Type.Known.OBJECT)
              .properties(
                  Map.of(
                      "recipe_name",
                      Schema.builder().type(Type.Known.STRING).build(),
                      "rating",
                      gradeSchema))
              .build();

      // Response is list of Recipe
      Schema responseSchema = Schema.builder().type(Type.Known.ARRAY).items(recipeSchema).build();

      GenerateContentConfig config =
          GenerateContentConfig.builder()
              .responseSchema(responseSchema)
              .responseMimeType("application/json")
              .build();

      GenerateContentResponse response = client.models.generateContent(modelId, contents, config);

      System.out.println("Raw JSON:\n" + response.text());

      // Deserialize to typed objects
      Recipe[] parsed = gson.fromJson(response.text(), Recipe[].class);

      System.out.println("\nParsed objects:");
      for (Recipe r : parsed) {
        System.out.println(r);
      }
      // Example response:
      // [{"rating": "a+", "recipe_name": "Classic Chocolate Chip Cookies"}, ...]
      return response.text();
    }
  }

  public enum Grade {
    A_PLUS("a+"),
    A("a"),
    B("b"),
    C("c"),
    D("d"),
    F("f");

    public final String value;

    Grade(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  public static class Recipe {
    public String recipe_name;
    public Grade rating;

    @Override
    public String toString() {
      return "Recipe{name='" + recipe_name + "', rating=" + rating + "}";
    }
  }
}

// [END googlegenaisdk_ctrlgen_with_nested_class_schema]
