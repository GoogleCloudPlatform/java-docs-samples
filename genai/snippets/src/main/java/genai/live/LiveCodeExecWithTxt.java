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

// [START googlegenaisdk_live_code_exec_with_txt]

import static com.google.genai.types.Modality.Known.TEXT;

import com.google.genai.AsyncSession;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.LiveConnectConfig;
import com.google.genai.types.LiveSendClientContentParameters;
import com.google.genai.types.LiveServerContent;
import com.google.genai.types.LiveServerMessage;
import com.google.genai.types.Part;
import com.google.genai.types.Tool;
import com.google.genai.types.ToolCodeExecution;
import java.util.concurrent.CompletableFuture;

public class LiveCodeExecWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.0-flash-live-preview-04-09";
    generateContent(modelId);
  }

  // Shows how to generate content with the Code Execution tool and a text input.
  public static String generateContent(String modelId) {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client = Client.builder().location("us-central1").vertexAI(true).build()) {

      // Connects to the live server.
      CompletableFuture<AsyncSession> sessionFuture =
          client.async.live.connect(
              modelId,
              LiveConnectConfig.builder()
                  .responseModalities(TEXT)
                  .tools(Tool.builder().codeExecution(ToolCodeExecution.builder().build()).build())
                  .build());

      // Sends and receives messages from the live session.
      CompletableFuture<String> responseFuture =
          sessionFuture.thenCompose(
              session -> {
                // A future that completes when the model signals the end of its turn.
                CompletableFuture<Void> turnComplete = new CompletableFuture<>();
                // A variable to concatenate the text response from the model.
                StringBuilder modelResponse = new StringBuilder();
                // Starts receiving messages from the live session.
                session.receive(
                    message -> handleLiveServerMessage(message, turnComplete, modelResponse));
                // Sends content to the live session and waits for the turn to complete.
                return sendContent(session)
                    .thenCompose(unused -> turnComplete)
                    .thenCompose(
                        unused -> session.close().thenApply(result -> modelResponse.toString()));
              });

      String response = responseFuture.join();
      System.out.println(response);
      // Example output:
      // > Compute the largest prime palindrome under 100000
      //
      // Okay, I understand. I need to find the largest prime number that is also a palindrome
      // and is less than 100000. Here's my plan...
      return response;
    }
  }

  // Sends content to the live session.
  private static CompletableFuture<Void> sendContent(AsyncSession session) {
    String textInput = "Compute the largest prime palindrome under 100000";
    System.out.printf("> %s\n", textInput);
    return session.sendClientContent(
        LiveSendClientContentParameters.builder()
            .turns(Content.builder().role("user").parts(Part.fromText(textInput)).build())
            .turnComplete(true)
            .build());
  }

  // Concatenates the response messages from the model and signals
  // `turnComplete` when the model is done generating the response.
  private static void handleLiveServerMessage(
      LiveServerMessage message, CompletableFuture<Void> turnComplete, StringBuilder response) {
    message
        .serverContent()
        .flatMap(LiveServerContent::modelTurn)
        .flatMap(Content::parts)
        .ifPresent(
            parts ->
                parts.forEach(
                    part -> {
                      part.text().ifPresent(response::append);
                      part.executableCode().ifPresent(code -> System.out.println("code: " + code));
                      part.codeExecutionResult()
                          .ifPresent(result -> System.out.println("result: " + result));
                    }));
    // Checks if the model's turn is over.
    if (message.serverContent().flatMap(LiveServerContent::turnComplete).orElse(false)) {
      turnComplete.complete(null);
    }
  }
}
// [END googlegenaisdk_live_code_exec_with_txt]
