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

// [START googlegenaisdk_live_transcribe_with_audio]

import static com.google.genai.types.Modality.Known.AUDIO;

import com.google.genai.AsyncSession;
import com.google.genai.Client;
import com.google.genai.types.AudioTranscriptionConfig;
import com.google.genai.types.Content;
import com.google.genai.types.LiveConnectConfig;
import com.google.genai.types.LiveSendClientContentParameters;
import com.google.genai.types.LiveServerContent;
import com.google.genai.types.LiveServerMessage;
import com.google.genai.types.Part;
import com.google.genai.types.Transcription;
import java.util.concurrent.CompletableFuture;

public class LiveTranscribeWithAudio {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-live-2.5-flash-preview-native-audio";
    generateContent(modelId);
  }

  // Shows how to transcribe audio
  public static void generateContent(String modelId) {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client = Client.builder().location("us-central1").vertexAI(true).build()) {

      // Connects to the live server.
      CompletableFuture<AsyncSession> sessionFuture =
          client.async.live.connect(
              modelId,
              LiveConnectConfig.builder()
                  .responseModalities(AUDIO)
                  .inputAudioTranscription(AudioTranscriptionConfig.builder().build())
                  .outputAudioTranscription(AudioTranscriptionConfig.builder().build())
                  .build());

      // Sends and receives messages from the live server.
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
      // Output transcript: Yes, I'm here
      // Output transcript: How can I help you today?
      // ...
      // The model is done generating.
    }
  }

  // Sends content to the live server.
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
        .ifPresent(
            serverContent -> {
              serverContent
                  .modelTurn()
                  .ifPresent(modelTurn -> System.out.println("Model turn: " + modelTurn.parts()));

              serverContent
                  .inputTranscription()
                  .flatMap(Transcription::text)
                  .ifPresent(text -> System.out.println("Input transcript: " + text));

              serverContent
                  .outputTranscription()
                  .flatMap(Transcription::text)
                  .ifPresent(text -> System.out.println("Output transcript: " + text));

              if (serverContent.turnComplete().orElse(false)) {
                System.out.println("The model is done generating.");
                turnComplete.complete(null);
              }
            });
  }
}
// [END googlegenaisdk_live_transcribe_with_audio]
