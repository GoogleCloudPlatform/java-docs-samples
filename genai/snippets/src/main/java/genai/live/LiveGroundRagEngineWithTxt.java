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

// [START googlegenaisdk_live_ground_ragengine_with_txt]

import com.google.genai.AsyncSession;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.LiveConnectConfig;
import com.google.genai.types.LiveSendClientContentParameters;
import com.google.genai.types.LiveServerContent;
import com.google.genai.types.LiveServerMessage;
import com.google.genai.types.Part;
import com.google.genai.types.Retrieval;
import com.google.genai.types.Tool;
import com.google.genai.types.VertexRagStore;
import com.google.genai.types.VertexRagStoreRagResource;
import java.util.concurrent.CompletableFuture;

public class LiveGroundRagEngineWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample
    String modelId = "gemini-2.0-flash-live-preview-04-09";
    String ragCorpus = "projects/{project}/locations/{location}/ragCorpora/{rag_corpus}";
    generateContent(modelId, ragCorpus);
  }

  // Shows how to use Vertex AI RAG Engine for grounding and the Live API.
  public static String generateContent(String modelId, String ragCorpus) {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client = Client.builder().location("us-central1").vertexAI(true).build()) {

      // Sets the Vertex RAG Store for grounding
      VertexRagStore vertexRagStore =
          VertexRagStore.builder()
              .ragResources(VertexRagStoreRagResource.builder().ragCorpus(ragCorpus).build())
              .storeContext(true)
              .build();

      LiveConnectConfig liveConnectConfig =
          LiveConnectConfig.builder()
              .responseModalities("TEXT")
              .tools(
                  Tool.builder()
                      .retrieval(Retrieval.builder().vertexRagStore(vertexRagStore).build())
                      .build())
              .build();

      // Connects to the live server.
      CompletableFuture<AsyncSession> sessionFuture =
          client.async.live.connect(modelId, liveConnectConfig);

      // Sends content and receives response from the live session.
      CompletableFuture<String> responseFuture =
          sessionFuture.thenCompose(
              session -> {
                // A future that completes when the model signals the end of its turn.
                CompletableFuture<Void> turnComplete = new CompletableFuture<>();
                // A variable to concatenate the text responses from model.
                StringBuilder serverResponse = new StringBuilder();
                // Starts receiving messages from the live session.
                session.receive(
                    message -> handleLiveServerMessage(message, turnComplete, serverResponse));
                // Sends content to the live session and waits for the turn to complete.
                return sendContent(session)
                    .thenCompose(unused -> turnComplete)
                    .thenCompose(
                        unused -> session.close().thenApply(result -> serverResponse.toString()));
              });

      String response = responseFuture.join();
      System.out.println(response);
      // Example response:
      // > What are the newest gemini model?
      // The newest Gemini model was launched in December 2023.
      // It is a multimodal model that understands and combines different
      // types of information like text, code, audio, images, and video.
      return response;
    }
  }

  // Sends content to the live session.
  private static CompletableFuture<Void> sendContent(AsyncSession session) {
    String textInput = "What are the newest gemini model?";
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
      LiveServerMessage message,
      CompletableFuture<Void> turnComplete,
      StringBuilder serverResponse) {
    message
        .serverContent()
        .flatMap(LiveServerContent::modelTurn)
        .flatMap(Content::parts)
        .ifPresent(parts -> parts.forEach(part -> part.text().ifPresent(serverResponse::append)));
    // Checks if the model's turn is over.
    if (message.serverContent().flatMap(LiveServerContent::turnComplete).orElse(false)) {
      turnComplete.complete(null);
    }
  }
}
// [END googlegenaisdk_live_ground_ragengine_with_txt]
