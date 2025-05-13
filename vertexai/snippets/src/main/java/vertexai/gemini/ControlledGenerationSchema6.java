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

// [START generativeaionvertexai_gemini_controlled_generation_response_schema_6]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.Schema;
import com.google.cloud.vertexai.api.Type;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;

public class ControlledGenerationSchema6 {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "genai-java-demos";
    String location = "us-central1";
    String modelName = "gemini-2.0-flash-001";

    controlGenerationWithJsonSchema6(projectId, location, modelName);
  }

  // Generate responses that are always valid JSON and comply with a JSON schema
  public static String controlGenerationWithJsonSchema6(
      String projectId, String location, String modelName)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      GenerationConfig generationConfig = GenerationConfig.newBuilder()
          .setResponseMimeType("application/json")
          .setResponseSchema(Schema.newBuilder()
              .setType(Type.ARRAY)
              .setItems(Schema.newBuilder()
                  .setType(Type.OBJECT)
                  .putProperties("object", Schema.newBuilder().setType(Type.STRING).build())
                  .build())
              .build())
          .build();

      GenerativeModel model = new GenerativeModel(modelName, vertexAI)
          .withGenerationConfig(generationConfig);

      // These images in Cloud Storage are viewable at
      // https://storage.googleapis.com/cloud-samples-data/generative-ai/image/office-desk.jpeg
      // https://storage.googleapis.com/cloud-samples-data/generative-ai/image/gardening-tools.jpeg

      GenerateContentResponse response = model.generateContent(
          ContentMaker.fromMultiModalData(
              PartMaker.fromMimeTypeAndData("image/jpeg",
                  "gs://cloud-samples-data/generative-ai/image/office-desk.jpeg"),
              PartMaker.fromMimeTypeAndData("image/jpeg",
                  "gs://cloud-samples-data/generative-ai/image/gardening-tools.jpeg"),
              "Generate a list of objects in the images."
          )
      );

      String output = ResponseHandler.getText(response);
      System.out.println(output);
      return output;
    }
  }
}
// [END generativeaionvertexai_gemini_controlled_generation_response_schema_6]