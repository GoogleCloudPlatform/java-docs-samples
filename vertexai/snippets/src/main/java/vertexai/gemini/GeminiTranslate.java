/*
 * Copyright 2024 Google LLC
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

// [START generativeaionvertexai_gemini_translate]

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.HarmCategory;
import com.google.cloud.vertexai.api.SafetySetting;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GeminiTranslate {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.0-pro";
    // The text to be translated.
    String text = "Hello! How are you doing today?";
    // The language code of the target language. Defaults to "fr" (French).
    // Available language codes:
    // https://cloud.google.com/translate/docs/languages#neural_machine_translation_model
    String targetLanguageCode = "fr";

    String output = geminiTranslate(projectId, location, modelName, text, targetLanguageCode);
    System.out.println(output);
  }

  // Translates the given text to the specified target language using the Gemini model.
  // The response from the model containing the translated text.
  public static String geminiTranslate(
      String projectId, String location, String modelName, String text, String targetLanguageCode)
      throws IOException {

    List<SafetySetting> safetySettings = Arrays.asList(
        SafetySetting.newBuilder()
            .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
            .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
            .build(),
        SafetySetting.newBuilder()
            .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
            .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
            .build(),
        SafetySetting.newBuilder()
            .setCategory(HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT)
            .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
            .build(),
        SafetySetting.newBuilder()
            .setCategory(HarmCategory.HARM_CATEGORY_HARASSMENT)
            .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
            .build()
    );
    GenerationConfig generationConfig =
        GenerationConfig.newBuilder()
            .setMaxOutputTokens(2048)
            .setTemperature(0.4F)
            .setTopK(32)
            .setTopP(1)
            .build();
    String question = String.format(
        "Your mission is to translate text in English to %s.", targetLanguageCode);
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      GenerativeModel model = new GenerativeModel(modelName, vertexAI)
          .withGenerationConfig(generationConfig)
          .withSafetySettings(safetySettings)
          .withSystemInstruction(ContentMaker.fromString(question));

      GenerateContentResponse response = model.generateContent(text);
      return ResponseHandler.getText(response);
    }
  }
}
// [END generativeaionvertexai_gemini_translate]
