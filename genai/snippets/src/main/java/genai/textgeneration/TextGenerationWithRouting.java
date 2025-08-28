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

package genai.textgeneration;

// [START googlegenaisdk_textgen_with_routing]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.ModelSelectionConfig;

public class TextGenerationWithRouting {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String promptText = "Why do we have 365 days in a year?";
    String featureSelectionPreference = "PRIORITIZE_COST";

    String generateContentText = generateContent(promptText, featureSelectionPreference);

    System.out.println("Response: " + generateContentText);
  }

  // Generates text with text input and routing option
  public static String generateContent(String promptText, String featureSelectionPreference) {

    // Model name for Model Optimizer
    String modelName = "model-optimizer-exp-04-09";

    ModelSelectionConfig modelSelectionConfig =
        ModelSelectionConfig.builder()
            .featureSelectionPreference(featureSelectionPreference)
            .build();

    GenerateContentConfig generateContentConfig =
        GenerateContentConfig.builder().modelSelectionConfig(modelSelectionConfig).build();       

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("us-central1")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1beta1").build())
            .build()) {

      GenerateContentResponse response =
          client.models.generateContent(modelName, promptText, generateContentConfig);

      System.out.print(response.text());

      return response.text();
    }
  }
}
// [END googlegenaisdk_textgen_with_routing]
