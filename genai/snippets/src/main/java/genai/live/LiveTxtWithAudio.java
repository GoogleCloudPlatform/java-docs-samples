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

// [START googlegenaisdk_live_txt_with_audio]

import static com.google.genai.types.Modality.Known.TEXT;

import com.google.genai.AsyncSession;
import com.google.genai.Client;
import com.google.genai.types.Blob;
import com.google.genai.types.Content;
import com.google.genai.types.LiveConnectConfig;
import com.google.genai.types.LiveSendRealtimeInputParameters;
import com.google.genai.types.LiveServerContent;
import com.google.genai.types.LiveServerMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class LiveTxtWithAudio {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.0-flash-live-preview-04-09";
    generateContent(modelId);
  }

  // Shows how to get text responses from audio input.
  public static String generateContent(String modelId) throws IOException {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client = Client.builder().location("us-central1").vertexAI(true).build()) {

      String audioUrl = "https://storage.googleapis.com/generativeai-downloads/data/16000.wav";
      byte[] audioBytes = downloadAudioFile(audioUrl);
      System.out.printf("> Answer to this audio url %s\n", audioUrl);

      // Connects to the live server.
      CompletableFuture<AsyncSession> sessionFuture =
          client.async.live.connect(
              modelId, LiveConnectConfig.builder().responseModalities(TEXT).build());

      // Sends content and receives response from the live session.
      CompletableFuture<String> responseFuture =
          sessionFuture.thenCompose(
              session -> {
                // A future that completes when the model signals the end of its turn.
                CompletableFuture<Void> turnComplete = new CompletableFuture<>();
                // A variable to concatenate the text response from the model.
                StringBuilder serverResponse = new StringBuilder();
                // Starts receiving messages from the live session.
                session.receive(
                    message -> handleLiveServerMessage(message, turnComplete, serverResponse));
                // Sends content to the live session and waits for the turn to complete.
                return sendContent(session, audioBytes)
                    .thenCompose(unused -> turnComplete)
                    .thenCompose(
                        unused -> session.close().thenApply(result -> serverResponse.toString()));
              });

      String response = responseFuture.join();
      System.out.println(response);
      // Example response:
      // > Answer to this audio url
      // https://storage.googleapis.com/generativeai-downloads/data/16000.wav
      //
      // Yeah, I can hear you loud and clear. What's on your mind?
      return response;
    }
  }

  // Download the audio file and return a byte array.
  private static byte[] downloadAudioFile(String audioUrl) throws IOException {
    URL url = new URL(audioUrl);
    try (InputStream in = url.openStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
      return out.toByteArray();
    }
  }

  // Sends content to the live session.
  private static CompletableFuture<Void> sendContent(AsyncSession session, byte[] audioBytes) {
    return session.sendRealtimeInput(
        LiveSendRealtimeInputParameters.builder()
            .media(Blob.builder().data(audioBytes).mimeType("audio/pcm;rate=16000").build())
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
// [END googlegenaisdk_live_txt_with_audio]
