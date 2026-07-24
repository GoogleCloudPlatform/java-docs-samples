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

package genai.tools;

// [START googlegenaisdk_tools_func_desc_with_txt]

import com.google.genai.Client;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Schema;
import com.google.genai.types.Tool;
import com.google.genai.types.Type;
import java.util.List;
import java.util.Map;

public class ToolFunctionDescriptionWithText {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    String contents = "What is the weather like in Boston?";

    generateContent(modelId, contents);
  }

  // Generates content with text input and function declaration that
  // the model may use to retrieve external data for the response
  public static String generateContent(String modelId, String contents) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      FunctionDeclaration getCurrentWeather =
          FunctionDeclaration.builder()
              .name("get_current_weather")
              .description("Get the current weather in a given location")
              // Function parameters are specified in schema format
              .parameters(
                  Schema.builder()
                      .type(Type.Known.OBJECT)
                      .properties(
                          Map.of(
                              "location",
                              Schema.builder()
                                  .type(Type.Known.STRING)
                                  .description(
                                      "The city name of the location for which to get the weather.")
                                  .build()))
                      .required(List.of("location"))
                      .build()) // End parameters schema
              .build(); // End function declaration

      Tool weatherTool = Tool.builder().functionDeclarations(getCurrentWeather).build();

      GenerateContentConfig config =
          GenerateContentConfig.builder().tools(weatherTool).temperature(0.0f).build();

      GenerateContentResponse response = client.models.generateContent(modelId, contents, config);

      // response.functionCalls() returns an ImmutableList<FunctionCall>.
      System.out.println(response.functionCalls().get(0));

      return response.functionCalls().toString();
      // Example response:
      // [FunctionCall{args=Optional[{location=Boston, MA}], name=Optional[get_current_weather]}]
    }
  }
}
// [END googlegenaisdk_tools_func_desc_with_txt]
