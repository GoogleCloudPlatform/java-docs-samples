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

// [START generativeaionvertexai_gemini_single_turn_image]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseStream;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.Base64;

public class SingleTurnMultimodal {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";
    String textPrompt = "What is this image";
    String dataImageBase64 = "your-base64-encoded-image";

    generateContent(projectId, location, modelName, textPrompt, dataImageBase64);
  }

  // Analyses the given multimodal input.
  public static void generateContent(String projectId, String location, String modelName,
      String textPrompt, String dataImageBase64) throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {

      ByteString decodedImage = ByteString.copyFrom(Base64.getDecoder().decode(dataImageBase64));

      GenerationConfig generationConfig =
          GenerationConfig.newBuilder()
              .setMaxOutputTokens(2048)
              .setTemperature(0.4F)
              .setTopK(32)
              .setTopP(1)
              .build();

      GenerativeModel model = new GenerativeModel(modelName, vertexAI)
          .withGenerationConfig(generationConfig);

      ResponseStream<GenerateContentResponse> responseStream = model.generateContentStream(
          ContentMaker.fromMultiModalData(
              PartMaker.fromMimeTypeAndData("image/png", decodedImage),
              textPrompt
          ));
      responseStream.stream().forEach(System.out::println);
    }
  }
}
// [END generativeaionvertexai_gemini_single_turn_image]