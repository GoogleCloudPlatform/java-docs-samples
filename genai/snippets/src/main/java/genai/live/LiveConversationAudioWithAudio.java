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

// [START googlegenaisdk_live_conversation_audio_with_audio]

import static com.google.genai.types.Modality.Known.AUDIO;

import com.google.genai.AsyncSession;
import com.google.genai.Client;
import com.google.genai.types.AudioTranscriptionConfig;
import com.google.genai.types.Blob;
import com.google.genai.types.Content;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.LiveConnectConfig;
import com.google.genai.types.LiveSendRealtimeInputParameters;
import com.google.genai.types.LiveServerMessage;
import com.google.genai.types.Transcription;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class LiveConversationAudioWithAudio {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-live-2.5-flash-native-audio";
    generateContent(modelId);
  }

  // Shows how to get an audio response from an audio input.
  public static void generateContent(String modelId) throws IOException {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("us-central1")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1beta1").build())
            .build()) {

      LiveConnectConfig liveConnectConfig =
          LiveConnectConfig.builder()
              // Set Model responses to be in Audio.
              .responseModalities(AUDIO)
              // To generate transcript for input audio.
              .inputAudioTranscription(AudioTranscriptionConfig.builder().build())
              // To generate transcript for output audio
              .outputAudioTranscription(AudioTranscriptionConfig.builder().build())
              .build();

      // Connects to the live server.
      CompletableFuture<AsyncSession> sessionFuture =
          client.async.live.connect(modelId, liveConnectConfig);

      String audioUrl =
          "https://storage.googleapis.com/cloud-samples-data/generative-ai/audio/hello_gemini_are_you_there.wav";
      byte[] audioBytes = downloadAudioFile(audioUrl);

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
                return sendAudio(session, audioBytes)
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
      // Example output:
      // Input transcription: Hello
      // Input transcription: .
      // Output transcription: Hello there!
      // Output transcription:  How can
      // Output transcription:  I help
      // Output transcription:  you today?
      // Successfully saved audio to...
    }
  }

  // Downloads the audio file and returns a byte array.
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
  private static CompletableFuture<Void> sendAudio(AsyncSession session, byte[] audioBytes) {
    return session.sendRealtimeInput(
        LiveSendRealtimeInputParameters.builder()
            .media(Blob.builder().data(audioBytes).mimeType("audio/pcm;rate=16000").build())
            .build());
  }

  // Prints the transcription and writes the inline data response to the audio buffer.
  // Signals `turnComplete` when the model is done generating the response.
  private static void handleLiveServerMessage(
      LiveServerMessage message,
      CompletableFuture<Void> turnComplete,
      ByteArrayOutputStream audioBuffer) {

    message
        .serverContent()
        .ifPresent(
            serverContent -> {
              serverContent
                  .inputTranscription()
                  .flatMap(Transcription::text)
                  .ifPresent(text -> System.out.println("Input transcription: " + text));

              serverContent
                  .outputTranscription()
                  .flatMap(Transcription::text)
                  .ifPresent(text -> System.out.println("Output transcription: " + text));

              serverContent
                  .modelTurn()
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
              if (serverContent.turnComplete().orElse(false)) {
                turnComplete.complete(null);
              }
            });
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

      Path outputPath = Paths.get("resources/output/example_model_response.wav");
      AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputPath.toFile());
      System.out.println("Successfully saved audio to: " + outputPath.toAbsolutePath());
    } catch (IOException e) {
      System.err.println("Error saving audio file: " + e.getMessage());
    }
  }
}
// [END googlegenaisdk_live_conversation_audio_with_audio]
