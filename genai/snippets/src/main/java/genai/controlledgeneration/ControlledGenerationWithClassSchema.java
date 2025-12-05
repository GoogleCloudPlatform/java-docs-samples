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

// [START googlegenaisdk_ctrlgen_with_class_schema]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;

public class ControlledGenerationWithClassSchema {

  private static final Gson gson = new Gson();

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String contents = "List a few popular cookie recipes.";
    String modelId = "gemini-2.5-flash";
    generateContent(modelId, contents);
  }

  // Generates content with a class response schema
  public static String generateContent(String modelId, String contents) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      // Schema for a single Recipe object
      Schema recipeSchema =
          Schema.builder()
              .type(Type.Known.OBJECT)
              .properties(
                  Map.of(
                      "recipe_name",
                      Schema.builder().type(Type.Known.STRING).build(),
                      "ingredients",
                      Schema.builder()
                          .type(Type.Known.ARRAY)
                          .items(Schema.builder().type(Type.Known.STRING).build())
                          .build()))
              .build();

      // Schema defining response as an array of Recipe
      Schema responseSchema = Schema.builder().type(Type.Known.ARRAY).items(recipeSchema).build();

      GenerateContentConfig config =
          GenerateContentConfig.builder()
              .responseSchema(responseSchema)
              .responseMimeType("application/json")
              .build();

      GenerateContentResponse response = client.models.generateContent(modelId, contents, config);

      System.out.println("Raw JSON:\n" + response.text());

      // Parse JSON into typed objects
      Recipe[] parsed = gson.fromJson(response.text(), Recipe[].class);

      System.out.println("\nParsed objects:");
      for (Recipe r : parsed) {
        System.out.println(r);
      }
      // Example response:
      // [
      //   {
      //     "recipe_name": "Classic Chocolate Chip Cookies",
      //     "ingredients": [
      //       "2 1/4 cups all-purpose flour",
      //       "1 teaspoon baking soda",
      //       "1 teaspoon salt",
      //       "1 cup (2 sticks) unsalted butter, softened",
      //       "3/4 cup granulated sugar",
      //       "3/4 cup packed brown sugar",
      //       "1 teaspoon vanilla extract",
      //       "2 large eggs",
      //       "2 cups chocolate chips"
      //     ]
      //   },
      //   ...
      // ]
      return response.text();
    }
  }

  public static class Recipe {
    @com.google.gson.annotations.SerializedName("recipe_name")
    public String recipeName;

    public List<String> ingredients;

    @Override
    public String toString() {
      return "Recipe{name='" + recipeName + "', ingredients=" + ingredients + "}";
    }
  }
}

// [END googlegenaisdk_ctrlgen_with_class_schema]
