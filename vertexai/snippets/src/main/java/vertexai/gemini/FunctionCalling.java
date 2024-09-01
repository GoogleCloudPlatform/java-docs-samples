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

// [START generativeaionvertexai_gemini_function_calling]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FunctionDeclaration;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Schema;
import com.google.cloud.vertexai.api.Tool;
import com.google.cloud.vertexai.api.Type;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class FunctionCalling {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";

    String promptText = "What's the weather like in Paris?";

    whatsTheWeatherLike(projectId, location, modelName, promptText);
  }

  // A request involving the interaction with an external tool
  public static String whatsTheWeatherLike(String projectId, String location,
                                           String modelName, String promptText)
      throws IOException {
    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {

      FunctionDeclaration functionDeclaration = FunctionDeclaration.newBuilder()
          .setName("getCurrentWeather")
          .setDescription("Get the current weather in a given location")
          .setParameters(
              Schema.newBuilder()
                  .setType(Type.OBJECT)
                  .putProperties("location", Schema.newBuilder()
                      .setType(Type.STRING)
                      .setDescription("location")
                      .build()
                  )
                  .addRequired("location")
                  .build()
          )
          .build();

      System.out.println("Function declaration:");
      System.out.println(functionDeclaration);

      // Add the function to a "tool"
      Tool tool = Tool.newBuilder()
          .addFunctionDeclarations(functionDeclaration)
          .build();

      // Start a chat session from a model, with the use of the declared function.
      GenerativeModel model = new GenerativeModel(modelName, vertexAI)
          .withTools(Arrays.asList(tool));
      ChatSession chat = model.startChat();

      System.out.println(String.format("Ask the question: %s", promptText));
      GenerateContentResponse response = chat.sendMessage(promptText);

      // The model will most likely return a function call to the declared
      // function `getCurrentWeather` with "Paris" as the value for the
      // argument `location`.
      System.out.println("\nPrint response: ");
      System.out.println(ResponseHandler.getContent(response));

      // Provide an answer to the model so that it knows what the result
      // of a "function call" is.
      Content content =
          ContentMaker.fromMultiModalData(
              PartMaker.fromFunctionResponse(
                  "getCurrentWeather",
                  Collections.singletonMap("currentWeather", "sunny")));
      System.out.println("Provide the function response: ");
      System.out.println(content);
      response = chat.sendMessage(content);

      // See what the model replies now
      System.out.println("Print response: ");
      String finalAnswer = ResponseHandler.getText(response);
      System.out.println(finalAnswer);

      return finalAnswer;
    }
  }
}
// [END generativeaionvertexai_gemini_function_calling]
