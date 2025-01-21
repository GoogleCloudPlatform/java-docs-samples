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

// [START generativeaionvertexai_gemini_controlled_generation_response_schema_4]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.Schema;
import com.google.cloud.vertexai.api.Type;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;
import java.util.Arrays;

public class ControlledGenerationSchema4 {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "genai-java-demos";
    String location = "us-central1";
    String modelName = "gemini-1.5-pro-001";

    controlGenerationWithJsonSchema4(projectId, location, modelName);
  }

  // Generate responses that are always valid JSON and comply with a JSON schema
  public static String controlGenerationWithJsonSchema4(
      String projectId, String location, String modelName)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      Schema itemSchema = Schema.newBuilder()
          .setType(Type.OBJECT)
          .putProperties("to_discard", Schema.newBuilder().setType(Type.INTEGER).build())
          .putProperties("subcategory", Schema.newBuilder().setType(Type.STRING).build())
          .putProperties("safe_handling", Schema.newBuilder().setType(Type.INTEGER).build())
          .putProperties("item_category", Schema.newBuilder()
              .setType(Type.STRING)
              .addAllEnum(Arrays.asList(
                  "clothing", "winter apparel", "specialized apparel", "furniture",
                  "decor", "tableware", "cookware", "toys"))
              .build())
          .putProperties("for_resale", Schema.newBuilder().setType(Type.INTEGER).build())
          .putProperties("condition", Schema.newBuilder()
              .setType(Type.STRING)
              .addAllEnum(Arrays.asList(
                  "new in package", "like new", "gently used", "used", "damaged", "soiled"))
              .build())
          .build();

      GenerationConfig generationConfig = GenerationConfig.newBuilder()
          .setResponseMimeType("application/json")
          .setResponseSchema(Schema.newBuilder()
              .setType(Type.ARRAY)
              .setItems(itemSchema)
              .build())
          .build();

      GenerativeModel model = new GenerativeModel(modelName, vertexAI)
          .withGenerationConfig(generationConfig);

      GenerateContentResponse response = model.generateContent(
          "Item description:\n"
              + "The item is a long winter coat that has many tears all around the seams "
              + "and is falling apart.\n"
              + "It has large questionable stains on it."
      );

      String output = ResponseHandler.getText(response);
      System.out.println(output);
      return output;
    }
  }
}
// [END generativeaionvertexai_gemini_controlled_generation_response_schema_4]