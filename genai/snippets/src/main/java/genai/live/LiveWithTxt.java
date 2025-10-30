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

// [START googlegenaisdk_live_with_txt]

import static com.google.genai.types.Modality.Known.TEXT;

import com.google.genai.AsyncSession;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.LiveConnectConfig;
import com.google.genai.types.LiveSendClientContentParameters;
import com.google.genai.types.LiveServerContent;
import com.google.genai.types.LiveServerMessage;
import com.google.genai.types.Part;
import java.util.concurrent.CompletableFuture;

public class LiveWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.0-flash-live-preview-04-09";
    generateContent(modelId);
  }

  // Shows how to send a text prompt and receive messages from the live server.
  public static void generateContent(String modelId) {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("us-central1")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1beta1").build())
            .build()) {

      // Connects to the live server.
      CompletableFuture<AsyncSession> sessionFuture =
          client.async.live.connect(
              modelId, LiveConnectConfig.builder().responseModalities(TEXT).build());

      // Sends and receives messages from the server.
      sessionFuture
          .thenCompose(
              session -> {
                // A future that completes when the server signals the end of its turn.
                CompletableFuture<Void> turnComplete = new CompletableFuture<>();
                // Starts receiving messages from the live server.
                session.receive(message -> handleLiveServerMessage(message, turnComplete));
                // Sends content to the server and waits for the turn to complete.
                return sendContent(session)
                    .thenCompose(unused -> turnComplete)
                    .thenCompose(unused -> session.close());
              })
          .join();
      // Example output:
      // > Hello? Gemini, are you there?
      // Output: Yes
      // Output: , I'm here. How can I help you today?
      // The model is done generating.
    }
  }

  // Sends content to the server.
  private static CompletableFuture<Void> sendContent(AsyncSession session) {
    String textInput = "Hello? Gemini, are you there?";
    System.out.printf("> %s\n", textInput);
    return session.sendClientContent(
        LiveSendClientContentParameters.builder()
            .turns(Content.builder().role("user").parts(Part.fromText(textInput)).build())
            .turnComplete(true)
            .build());
  }

  // Prints response messages from the server and signals `turnComplete` when the server is done.
  private static void handleLiveServerMessage(
      LiveServerMessage message, CompletableFuture<Void> turnComplete) {
    message
        .serverContent()
        .flatMap(LiveServerContent::modelTurn)
        .flatMap(Content::parts)
        .ifPresent(
            parts ->
                parts.forEach(
                    part -> part.text().ifPresent(text -> System.out.println("Output: " + text))));

    if (message.serverContent().flatMap(LiveServerContent::turnComplete).orElse(false)) {
      System.out.println("The model is done generating.");
      turnComplete.complete(null);
    }
  }
}
// [END googlegenaisdk_live_with_txt]
