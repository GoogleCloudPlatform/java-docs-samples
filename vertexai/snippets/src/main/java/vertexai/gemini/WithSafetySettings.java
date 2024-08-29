/*
 * Copyright 2023 Google LLC
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

package vertexai.gemini;

// [START generativeaionvertexai_gemini_safety_settings]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.HarmCategory;
import com.google.cloud.vertexai.api.SafetySetting;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import java.util.Arrays;
import java.util.List;

public class WithSafetySettings {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";
    String textPrompt = "your-text-here";

    String output = safetyCheck(projectId, location, modelName, textPrompt);
    System.out.println(output);
  }

  // Use safety settings to avoid harmful questions and content generation.
  public static String safetyCheck(String projectId, String location, String modelName,
      String textPrompt) throws Exception {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      StringBuilder output = new StringBuilder();

      GenerationConfig generationConfig =
          GenerationConfig.newBuilder()
              .setMaxOutputTokens(2048)
              .setTemperature(0.4F)
              .build();

      List<SafetySetting> safetySettings = Arrays.asList(
          SafetySetting.newBuilder()
              .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
              .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE)
              .build(),
          SafetySetting.newBuilder()
              .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
              .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE)
              .build()
      );

      GenerativeModel model = new GenerativeModel(modelName, vertexAI)
          .withGenerationConfig(generationConfig)
          .withSafetySettings(safetySettings);

      GenerateContentResponse response = model.generateContent(textPrompt);
      output.append(response).append("\n");

      // Verifies if the above content has been blocked for safety reasons.
      boolean blockedForSafetyReason = response.getCandidatesList()
          .stream()
          .anyMatch(candidate -> candidate.getFinishReason() == Candidate.FinishReason.SAFETY);
      output.append("Blocked for safety reasons?: ").append(blockedForSafetyReason);

      return output.toString();
    }
  }
}
// [END generativeaionvertexai_gemini_safety_settings]
