/*
 * Copyright 2025 Google LLC
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

package genai.live;

// [START googlegenaisdk_live_func_call_with_txt]

import static com.google.genai.types.Modality.Known.TEXT;

import com.google.genai.AsyncSession;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.FunctionResponse;
import com.google.genai.types.LiveConnectConfig;
import com.google.genai.types.LiveSendClientContentParameters;
import com.google.genai.types.LiveSendToolResponseParameters;
import com.google.genai.types.LiveServerContent;
import com.google.genai.types.LiveServerMessage;
import com.google.genai.types.LiveServerToolCall;
import com.google.genai.types.Part;
import com.google.genai.types.Tool;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LiveFuncCallWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.s
    String modelId = "gemini-2.0-flash-live-preview-04-09";
    generateContent(modelId);
  }

  // Shows how to use function calling with the Live API.
  public static void generateContent(String modelId) {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client = Client.builder().location("us-central1").vertexAI(true).build()) {

      // Function definitions.
      List<FunctionDeclaration> functionDeclarations =
          List.of(
              FunctionDeclaration.builder().name("turn_on_the_lights").build(),
              FunctionDeclaration.builder().name("turn_off_the_lights").build());

      LiveConnectConfig liveConnectConfig =
          LiveConnectConfig.builder()
              .responseModalities(TEXT)
              .tools(Tool.builder().functionDeclarations(functionDeclarations).build())
              .build();

      // Connects to the live server.
      CompletableFuture<AsyncSession> sessionFuture =
          client.async.live.connect(modelId, liveConnectConfig);

      // Sends content and receives response from the live session.
      sessionFuture
          .thenCompose(
              session -> {
                // A future that completes when the model signals the end of its turn.
                CompletableFuture<Void> turnComplete = new CompletableFuture<>();
                // Starts receiving messages from the live session.
                session.receive(message -> handleFunctionCall(message, turnComplete, session));
                // Sends content to the live session and waits for the turn to complete.
                return sendContent(session)
                    .thenCompose(unused -> turnComplete)
                    .thenCompose(unused -> session.close());
              })
          .join();
      // Example response:
      // > Turn off the lights please
      // Function name: turn_off_the_lights
      // Optional[{result=ok}]
    }
  }

  // Sends content to the live session.
  private static CompletableFuture<Void> sendContent(AsyncSession session) {
    String textInput = "Turn off the lights please";
    System.out.printf("> %s\n", textInput);
    return session.sendClientContent(
        LiveSendClientContentParameters.builder()
            .turns(Content.builder().role("user").parts(Part.fromText(textInput)).build())
            .turnComplete(true)
            .build());
  }

  // Handles function call response from the live session and signals
  // `turnComplete` when the model is done generating the response.
  private static void handleFunctionCall(
      LiveServerMessage message, CompletableFuture<Void> turnComplete, AsyncSession session) {
    message
        .toolCall()
        .flatMap(LiveServerToolCall::functionCalls)
        .ifPresent(
            functionCalls -> {
              List<FunctionResponse> functionResponses = new ArrayList<>();
              functionCalls.forEach(
                  functionCall ->
                      functionCall
                          .name()
                          .ifPresent(
                              functionName -> {
                                System.out.println("Function name: " + functionName);
                                FunctionResponse functionResponse =
                                    FunctionResponse.builder()
                                        .name(functionName)
                                        .response(Map.of("result", "ok"))
                                        .build();
                                functionResponses.add(functionResponse);
                                System.out.println(functionResponse.response());
                              }));
              // Send the results of all executed functions back to the model.
              session.sendToolResponse(
                  LiveSendToolResponseParameters.builder()
                      .functionResponses(functionResponses)
                      .build());
            });

    // Checks if the model's turn is over.
    if (message.serverContent().flatMap(LiveServerContent::turnComplete).orElse(false)) {
      turnComplete.complete(null);
    }
  }
}
// [END googlegenaisdk_live_func_call_with_txt]
