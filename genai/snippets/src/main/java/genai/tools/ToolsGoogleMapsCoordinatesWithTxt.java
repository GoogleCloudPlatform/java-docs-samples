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

// [START googlegenaisdk_tools_google_maps_coordinates_with_txt]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GoogleMaps;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.LatLng;
import com.google.genai.types.RetrievalConfig;
import com.google.genai.types.Tool;
import com.google.genai.types.ToolConfig;

public class ToolsGoogleMapsCoordinatesWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    generateContent(modelId);
  }

  // Generates content with Google Maps Tool.
  public static String generateContent(String modelId) {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      // Set the Google Maps Tool.
      Tool tool = Tool.builder().googleMaps(GoogleMaps.builder().build()).build();

      ToolConfig toolConfig =
          ToolConfig.builder()
              .retrievalConfig(
                  RetrievalConfig.builder()
                      // Pass coordinates for location-aware grounding
                      .latLng(LatLng.builder().latitude(40.7128).longitude(-74.006).build())
                      // Localize Maps results
                      .languageCode("en_US")
                      .build())
              .build();

      GenerateContentResponse response =
          client.models.generateContent(
              modelId,
              "Where can I get the best espresso near me?",
              GenerateContentConfig.builder().tools(tool).toolConfig(toolConfig).build());

      System.out.println(response.text());
      // Example response:
      // Here are some of the top-rated coffee shops near you that serve excellent espresso...
      return response.text();
    }
  }
}
// [END googlegenaisdk_tools_google_maps_coordinates_with_txt]
