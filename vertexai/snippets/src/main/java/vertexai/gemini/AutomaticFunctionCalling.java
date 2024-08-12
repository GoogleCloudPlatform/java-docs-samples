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

// [START generativeaionvertexai_gemini_automatic_function_calling]
// [START aiplatform_gemini_automatic_function_calling]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.FunctionDeclaration;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Tool;
import com.google.cloud.vertexai.generativeai.AutomaticFunctionCallingResponder;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.FunctionDeclarationMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class AutomaticFunctionCalling {
  public static void main(String[] args) throws IOException, NoSuchMethodException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";

    String promptText = "What's the weather like in Paris?";

    automaticFunctionCalling(projectId, location, modelName, promptText);
  }

  // This is just a stub and can be substituted with any external functions with http calls.
  /** Callable function getCurrentWeather. */
  public static String getCurrentWeather(String location) {
    if (location.equals("Paris")) {
      return "raining";
    } else {
      return "sunny";
    }
  }

  // Use the Automatic Function Calling feature to auto-respond to model's Function Call requests.
  public static String automaticFunctionCalling(
      String projectId, String location, String modelName, String promptText)
      throws IOException, NoSuchMethodException {
    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      // Get the callable method instance
      Method function = AutomaticFunctionCalling.class.getMethod("getCurrentWeather", String.class);
      // Use the fromFunc helper method to create a FunctionDeclaration
      FunctionDeclaration functionDeclaration =
          FunctionDeclarationMaker.fromFunc(
              "Get the current weather in a given location", function, "location");
      System.out.printf("Function declaration: %s\n", functionDeclaration);

      // Add the function to a "tool"
      Tool tool = Tool.newBuilder().addFunctionDeclarations(functionDeclaration).build();

      // Instantiate an AutomaticFunctionCallingResponder and add the callable method
      AutomaticFunctionCallingResponder responder = new AutomaticFunctionCallingResponder();
      responder.addCallableFunction("getCurrentWeather", function, "location");

      // Start a chat session from a model, with the use of the declared function.
      GenerativeModel model =
          new GenerativeModel(modelName, vertexAI).withTools(Arrays.asList(tool));
      ChatSession chat = model.startChat();

      System.out.println(String.format("Ask the question: %s", promptText));
      // Send message with the responder, which auto-responds FunctionCalls and
      // returns the final text result
      GenerateContentResponse response =
          chat.withAutomaticFunctionCallingResponder(responder).sendMessage(promptText);

      // Check the final response
      String finalAnswer = ResponseHandler.getText(response);
      System.out.printf("Response: %s\n", finalAnswer);

      return finalAnswer;
    }
  }
}
 // [END aiplatform_gemini_automatic_function_calling]
 // [END generativeaionvertexai_gemini_automatic_function_calling]
