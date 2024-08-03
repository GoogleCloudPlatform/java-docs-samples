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

// [START generativeaionvertexai_gemini_controlled_generation_response_schema_2]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.Schema;
import com.google.cloud.vertexai.api.Type;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;
import java.util.Arrays;

public class ControlledGenerationSchema2 {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "genai-java-demos";
    String location = "us-central1";
    String modelName = "gemini-1.5-pro-001";

    controlGenerationWithJsonSchema2(projectId, location, modelName);
  }

  // Generate responses that are always valid JSON and comply with a JSON schema
  public static String controlGenerationWithJsonSchema2(
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
                  .putProperties("rating", Schema.newBuilder().setType(Type.INTEGER).build())
                  .putProperties("flavor", Schema.newBuilder().setType(Type.STRING).build())
                  .addAllRequired(Arrays.asList("rating", "flavor"))
                  .build())
              .build())
          .build();

      GenerativeModel model = new GenerativeModel(modelName, vertexAI)
          .withGenerationConfig(generationConfig);

      GenerateContentResponse response = model.generateContent(
          "Reviews from our social media:\n"
              + "\"Absolutely loved it! Best ice cream I've ever had.\" "
              + "Rating: 4, Flavor: Strawberry Cheesecake\n"
              + "\"Quite good, but a bit too sweet for my taste.\" "
              + "Rating: 1, Flavor: Mango Tango"
      );

      String output = ResponseHandler.getText(response);
      System.out.println(output);
      return output;
    }
  }
}
// [END generativeaionvertexai_gemini_controlled_generation_response_schema_2]