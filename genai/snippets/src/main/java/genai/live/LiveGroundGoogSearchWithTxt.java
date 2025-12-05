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

// [START googlegenaisdk_live_ground_googsearch_with_txt]

import static com.google.genai.types.Modality.Known.TEXT;

import com.google.genai.AsyncSession;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GoogleSearch;
import com.google.genai.types.LiveConnectConfig;
import com.google.genai.types.LiveSendClientContentParameters;
import com.google.genai.types.LiveServerContent;
import com.google.genai.types.LiveServerMessage;
import com.google.genai.types.Part;
import com.google.genai.types.Tool;
import java.util.concurrent.CompletableFuture;

public class LiveGroundGoogSearchWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.0-flash-live-preview-04-09";
    generateContent(modelId);
  }

  // Shows how to generate content with the Google Search tool and a text input.
  public static String generateContent(String modelId) {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client = Client.builder().location("us-central1").vertexAI(true).build()) {

      // Connects to the live server.
      CompletableFuture<AsyncSession> sessionFuture =
          client.async.live.connect(
              modelId,
              LiveConnectConfig.builder()
                  .responseModalities(TEXT)
                  .tools(Tool.builder().googleSearch(GoogleSearch.builder().build()).build())
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
      // > When did the last Brazil vs. Argentina soccer match happen?
      //
      // The most recent Brazil vs. Argentina soccer match was on March 25, 2025,
      // as part of the 2026 World Cup qualifiers. Argentina won 4-1.
      return response;
    }
  }

  // Sends content to the live session.
  private static CompletableFuture<Void> sendContent(AsyncSession session) {
    String textInput = "When did the last Brazil vs. Argentina soccer match happen?";
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
        .ifPresent(parts -> parts.forEach(part -> part.text().ifPresent(response::append)));
    // Checks if the model's turn is over.
    if (message.serverContent().flatMap(LiveServerContent::turnComplete).orElse(false)) {
      turnComplete.complete(null);
    }
  }
}
// [END googlegenaisdk_live_ground_googsearch_with_txt]
