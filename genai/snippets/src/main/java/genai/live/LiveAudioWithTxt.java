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

// [START googlegenaisdk_live_audio_with_txt]

import static com.google.genai.types.Modality.Known.AUDIO;

import com.google.genai.AsyncSession;
import com.google.genai.Client;
import com.google.genai.types.Blob;
import com.google.genai.types.Content;
import com.google.genai.types.LiveConnectConfig;
import com.google.genai.types.LiveSendClientContentParameters;
import com.google.genai.types.LiveServerContent;
import com.google.genai.types.LiveServerMessage;
import com.google.genai.types.Part;
import com.google.genai.types.PrebuiltVoiceConfig;
import com.google.genai.types.SpeechConfig;
import com.google.genai.types.VoiceConfig;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class LiveAudioWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-live-2.5-flash-native-audio";
    generateContent(modelId);
  }

  // Shows how to get voice responses from text input.
  public static void generateContent(String modelId) {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client = Client.builder().location("us-central1").vertexAI(true).build()) {

      LiveConnectConfig liveConnectConfig =
          LiveConnectConfig.builder()
              .responseModalities(AUDIO)
              .speechConfig(
                  SpeechConfig.builder()
                      .voiceConfig(
                          VoiceConfig.builder()
                              .prebuiltVoiceConfig(
                                  PrebuiltVoiceConfig.builder().voiceName("Aoede").build())
                              .build())
                      .build())
              .build();

      // Connects to the live server.
      CompletableFuture<AsyncSession> sessionFuture =
          client.async.live.connect(modelId, liveConnectConfig);

      // Sends content and receives response from the live server.
      sessionFuture
          .thenCompose(
              session -> {
                // A future that completes when the model signals the end of its turn.
                CompletableFuture<Void> turnComplete = new CompletableFuture<>();
                // A buffer to collect all incoming audio chunks.
                ByteArrayOutputStream audioBuffer = new ByteArrayOutputStream();
                // Starts receiving messages from the live session.
                session.receive(
                    message -> handleLiveServerMessage(message, turnComplete, audioBuffer));
                // Sends content to the live session and waits for the turn to complete.
                return sendContent(session)
                    .thenCompose(unused -> turnComplete)
                    .thenAccept(
                        unused -> {
                          byte[] audio = audioBuffer.toByteArray();
                          if (audio.length > 0) {
                            saveAudioToFile(audio);
                          }
                        })
                    .thenCompose(unused -> session.close());
              })
          .join();
      // Example response:
      // > Answer to this audio url
      // Successfully saved audio to...
    }
  }

  // Sends content to the live session.
  private static CompletableFuture<Void> sendContent(AsyncSession session) {
    String textInput = "Hello? Gemini, are you there?";
    System.out.printf("> %s\n", textInput);
    return session.sendClientContent(
        LiveSendClientContentParameters.builder()
            .turns(Content.builder().role("user").parts(Part.fromText(textInput)).build())
            .turnComplete(true)
            .build());
  }

  // Writes the inline data response to the audio buffer and signals
  // `turnComplete` when the model is done generating the response.
  private static void handleLiveServerMessage(
      LiveServerMessage message,
      CompletableFuture<Void> turnComplete,
      ByteArrayOutputStream audioBuffer) {
    message
        .serverContent()
        .flatMap(LiveServerContent::modelTurn)
        .flatMap(Content::parts)
        .ifPresent(
            parts ->
                parts.forEach(
                    part -> {
                      // When an audio blob is present, write its data to the buffer.
                      part.inlineData()
                          .flatMap(Blob::data)
                          .ifPresent(
                              data -> {
                                try {
                                  audioBuffer.write(data);
                                } catch (IOException e) {
                                  System.out.println(
                                      "Error writing to audio buffer: " + e.getMessage());
                                }
                              });
                    }));

    // Checks if the model's turn is over.
    if (message.serverContent().flatMap(LiveServerContent::turnComplete).orElse(false)) {
      turnComplete.complete(null);
    }
  }

  private static void saveAudioToFile(byte[] audioData) {
    try {
      // Defines the audio format.
      AudioFormat format = new AudioFormat(24000, 16, 1, true, false);
      // Creates an AudioInputStream from the raw audio data and the format.
      AudioInputStream audioStream =
          new AudioInputStream(
              new ByteArrayInputStream(audioData),
              format,
              audioData.length / format.getFrameSize());

      Path outputPath = Paths.get("resources/output/output_audio.wav");
      AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputPath.toFile());
      System.out.println("Successfully saved audio to: " + outputPath.toAbsolutePath());
    } catch (IOException e) {
      System.err.println("Error saving audio file: " + e.getMessage());
    }
  }
}
// [END googlegenaisdk_live_audio_with_txt]
