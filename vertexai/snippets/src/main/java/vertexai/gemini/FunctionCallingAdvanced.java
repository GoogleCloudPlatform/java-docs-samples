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

//[START generativeaionvertexai_gemini_function_calling_advanced]

import com.google.cloud.vertexai.VertexAI;
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
import java.util.Collections;
import java.util.List;

public class FunctionCallingAdvanced {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId =  "PROJECT_ID";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";

    functionCallingAdvanced(projectId, location, modelName);
  }

  // Function calling lets developers create descriptions of functions in their code, then pass
  // these descriptions to a language model in a request.
  public static void functionCallingAdvanced(String projectId, String location, String modelName)
      throws IOException {
    String firstTextPrompt = "I need to know if the Pixel 8 Pro is in stock";
    String secondTextPrompt = "Get the location of the closest store.";

    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {

      // Specify a function declaration and parameters for an API request
      FunctionDeclaration functionDeclaration1 = FunctionDeclaration.newBuilder()
          .setName("getProductSku")
          .setDescription("Get the available inventory for a Google products, e.g: Pixel phones,"
              + " Pixel Watches, Google Home etc")
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

      // Specify another function declaration and parameters for an API request
      FunctionDeclaration functionDeclaration2 = FunctionDeclaration.newBuilder()
          .setName("getStoreLocation")
          .setDescription("Get the location of the closest store")
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

      System.out.println("Functions declaration:");
      System.out.println(functionDeclaration1);
      System.out.println(functionDeclaration2);

      // Add functions to a "tool"
      Tool tool = Tool.newBuilder()
          .addFunctionDeclarations(functionDeclaration1)
          .addFunctionDeclarations(functionDeclaration2)
          .build();

      // Start a chat session from a model, with the use of the declared functions.
      GenerativeModel model = new GenerativeModel(modelName, vertexAI)
          .withTools(List.of(tool));
      ChatSession chat = model.startChat();

      System.out.printf("Ask the question: %s%n", firstTextPrompt);
      GenerateContentResponse response = chat.sendMessage(ContentMaker.fromMultiModalData(
          firstTextPrompt,
          PartMaker.fromMimeTypeAndData(
                  "getProductSku",
                  Collections.singletonMap("sku", "Pixel 8 Pro - SKU: 12345")
      )));

      System.out.println("\nPrint response: ");
      System.out.println(ResponseHandler.getContent(response));

      response = chat.sendMessage(ContentMaker.fromMultiModalData(
            secondTextPrompt,
              PartMaker.fromFunctionResponse(
                  "getStoreLocation",
                  Collections.singletonMap("store address", "123 Main Street, San Francisco CA")
              )
          )
      );

      System.out.println("Print response: ");
      String finalAnswer = ResponseHandler.getText(response);
      System.out.println(finalAnswer);
    }
  }
}
//[END generativeaionvertexai_gemini_function_calling_advanced]
