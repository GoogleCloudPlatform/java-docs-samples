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
import java.util.Map;

public class ToolFunctionDescriptionWithText {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    String contents =
        "At Stellar Sounds, a music label, 2024 was a rollercoaster. \"Echoes of the Night,\""
            + " a debut synth-pop album, \n surprisingly sold 350,000 copies, while veteran"
            + " rock band \"Crimson Tide's\" latest, \"Reckless Hearts,\" \n lagged at"
            + " 120,000. Their up-and-coming indie artist, \"Luna Bloom's\" EP, \"Whispers "
            + "of Dawn,\" \n secured 75,000 sales. The biggest disappointment was the "
            + "highly-anticipated rap album \"Street Symphony\" \n only reaching 100,000"
            + " units. Overall, Stellar Sounds moved over 645,000 units this year, revealing"
            + " unexpected \n trends in music consumption.";

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

      FunctionDeclaration getAlbumSales =
          FunctionDeclaration.builder()
              .name("get_album_sales")
              .description("Gets the number of albums sold")
              // Function parameters are specified in schema format
              .parameters(
                  Schema.builder()
                      .type(Type.Known.OBJECT)
                      .properties(
                          Map.of(
                              "albums",
                              Schema.builder()
                                  .type(Type.Known.ARRAY)
                                  .description("List of albums")
                                  .items(
                                      Schema.builder()
                                          .description("Album and its sales")
                                          .type(Type.Known.OBJECT)
                                          .properties(
                                              Map.of(
                                                  "album_name",
                                                      Schema.builder()
                                                          .type(Type.Known.STRING)
                                                          .description("Name of the music album")
                                                          .build(),
                                                  "copies_sold",
                                                      Schema.builder()
                                                          .type(Type.Known.INTEGER)
                                                          .description("Number of copies sold")
                                                          .build()))
                                          .build()) // End items schema for albums
                                  .build() // End "albums" property schema
                              ))
                      .build()) // End parameters schema
              .build(); // End function declaration

      Tool salesTool = Tool.builder().functionDeclarations(getAlbumSales).build();

      GenerateContentConfig config =
          GenerateContentConfig.builder().tools(salesTool).temperature(0.0f).build();

      GenerateContentResponse response = client.models.generateContent(modelId, contents, config);

      // response.functionCalls() returns an Immutable<FunctionCall>.
      System.out.println(response.functionCalls().get(0));

      return response.functionCalls().toString();
      // Example response:
      // FunctionCall{id=Optional.empty, args=Optional[{albums=[{copies_sold=350000,
      // album_name=Echoes of the Night},
      // {copies_sold=120000, album_name=Reckless Hearts}, {copies_sold=75000, album_name=Whispers
      // of Dawn},
      // {album_name=Street Symphony, copies_sold=100000}]}], name=Optional[get_album_sales]}
    }
  }
}
// [END googlegenaisdk_tools_func_desc_with_txt]
