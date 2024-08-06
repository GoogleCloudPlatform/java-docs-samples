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

// [START generativeaionvertexai_gemini_controlled_generation_response_schema_3]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.Schema;
import com.google.cloud.vertexai.api.Type;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;
import java.util.Arrays;

public class ControlledGenerationSchema3 {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "genai-java-demos";
    String location = "us-central1";
    String modelName = "gemini-1.5-pro-001";

    controlGenerationWithJsonSchema3(projectId, location, modelName);
  }

  // Generate responses that are always valid JSON and comply with a JSON schema
  public static String controlGenerationWithJsonSchema3(
      String projectId, String location, String modelName)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      GenerationConfig generationConfig = GenerationConfig.newBuilder()
          .setResponseMimeType("application/json")
          .setResponseSchema(Schema.newBuilder()
              .setType(Type.OBJECT)
              .putProperties("forecast", Schema.newBuilder()
                      .setType(Type.ARRAY)
                      .setItems(Schema.newBuilder()
                          .setType(Type.OBJECT)
                          .putProperties("Day", Schema.newBuilder()
                              .setType(Type.STRING)
                              .build())
                          .putProperties("Forecast", Schema.newBuilder()
                              .setType(Type.STRING)
                              .build())
                          .putProperties("Humidity", Schema.newBuilder()
                              .setType(Type.STRING)
                              .build())
                          .putProperties("Temperature", Schema.newBuilder()
                              .setType(Type.INTEGER)
                              .build())
                          .putProperties("Wind Speed", Schema.newBuilder()
                              .setType(Type.INTEGER)
                              .build())
                          .addAllRequired(Arrays.asList("Day", "Temperature", "Forecast"))
                          .build())
                      .build())
              )
          .build();

      GenerativeModel model = new GenerativeModel(modelName, vertexAI)
          .withGenerationConfig(generationConfig);

      GenerateContentResponse response = model.generateContent(
          "The week ahead brings a mix of weather conditions.\n"
              + "Sunday is expected to be sunny with a temperature of 77°F and a humidity level "
              + "of 50%. Winds will be light at around 10 km/h.\n"
              + "Monday will see partly cloudy skies with a slightly cooler temperature of 72°F "
              + "and humidity increasing to 55%. Winds will pick up slightly to around 15 km/h.\n"
              + "Tuesday brings rain showers, with temperatures dropping to 64°F and humidity"
              + "rising to 70%. Expect stronger winds at 20 km/h.\n"
              + "Wednesday may see thunderstorms, with a temperature of 68°F and high humidity "
              + "of 75%. Winds will be gusty at 25 km/h.\n"
              + "Thursday will be cloudy with a temperature of 66°F and moderate humidity at 60%. "
              + "Winds will ease slightly to 18 km/h.\n"
              + "Friday returns to partly cloudy conditions, with a temperature of 73°F and lower "
              + "humidity at 45%. Winds will be light at 12 km/h.\n"
              + "Finally, Saturday rounds off the week with sunny skies, a temperature of 80°F, "
              + "and a humidity level of 40%. Winds will be gentle at 8 km/h."
      );

      String output = ResponseHandler.getText(response);
      System.out.println(output);
      return output;
    }
  }
}
// [END generativeaionvertexai_gemini_controlled_generation_response_schema_3]