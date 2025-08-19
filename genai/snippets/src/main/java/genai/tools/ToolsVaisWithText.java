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

// [START googlegenaisdk_tools_vais_with_txt]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Retrieval;
import com.google.genai.types.Tool;
import com.google.genai.types.VertexAISearch;

public class ToolsVaisWithText {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    // Load Data Store ID from Vertex AI Search
    // E.g datastoreId =
    // "projects/project-id/locations/global/collections/default_collection/dataStores/datastore-id"
    String datastoreId = "your-datastore";
    generateContent(modelId, datastoreId);
  }

  // Generates text with Vertex AI Search tool
  public static String generateContent(String modelId, String datastoreId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      // Set the VertexAI Search tool and the datastore that the model can use to retrieve data from
      Tool vaisSearchTool =
          Tool.builder()
              .retrieval(
                  Retrieval.builder()
                      .vertexAiSearch(VertexAISearch.builder().datastore(datastoreId).build())
                      .build())
              .build();

      // Create a GenerateContentConfig and set the Vertex AI Search tool
      GenerateContentConfig contentConfig =
          GenerateContentConfig.builder().tools(vaisSearchTool).build();

      GenerateContentResponse response =
          client.models.generateContent(
              modelId, "How do I make an appointment to renew my driver's license?", contentConfig);

      System.out.print(response.text());
      // Example response:
      // The process for making an appointment to renew your driver's license varies depending
      // on your location. To provide you with the most accurate instructions...
      return response.text();
    }
  }
}
// [END googlegenaisdk_tools_vais_with_txt]