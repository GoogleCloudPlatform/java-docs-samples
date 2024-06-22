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

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;

public class MultiTurnMultimodal {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";

    multiTurnMultimodal(projectId, location, modelName);
  }

  // Analyses the given multi-turn multimodal input.
  public static void multiTurnMultimodal(String projectId, String location, String modelName)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      // Update the values for your query.
      String firstTextPrompt = "What is this image";
      String imageUri = "gs://generativeai-downloads/images/scones.jpg";
      String secondTextPrompt = "what did I just show you";

      GenerationConfig generationConfig =
          GenerationConfig.newBuilder()
              .setMaxOutputTokens(2048)
              .setTemperature(0.4F)
              .setTopK(32)
              .setTopP(1)
              .build();

      GenerativeModel model = new GenerativeModel(modelName, vertexAI)
          .withGenerationConfig(generationConfig);
      // For multi-turn responses, start a chat session.
      ChatSession chatSession = model.startChat();

      GenerateContentResponse response;
      // First message with multimodal input
      response = chatSession.sendMessage(ContentMaker.fromMultiModalData(
          firstTextPrompt,
          PartMaker.fromMimeTypeAndData(
              // Update Mime type according to your image.
              "image/jpeg",
              imageUri)
      ));
      System.out.println(ResponseHandler.getText(response));

      // Second message with text input
      response = chatSession.sendMessage(secondTextPrompt);
      System.out.println(ResponseHandler.getText(response));
    }
  }
}
