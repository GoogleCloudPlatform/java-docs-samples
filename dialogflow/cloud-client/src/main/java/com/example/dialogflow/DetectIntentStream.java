/*
  Copyright 2017, Google, Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package com.example.dialogflow;

// [START dialogflow_import_libraries]
// Imports the Google Cloud client library
import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.cloud.dialogflow.v2.AudioEncoding;
import com.google.cloud.dialogflow.v2.InputAudioConfig;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.StreamingDetectIntentRequest;
import com.google.cloud.dialogflow.v2.StreamingDetectIntentResponse;
import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
// [END dialogflow_import_libraries]

/**
 * DialogFlow API Detect Intent sample with audio files processes as an audio stream.
 */
public class DetectIntentStream {

  // [START dialogflow_detect_intent_streaming]
  /**
   * Returns the result of detect intent with streaming audio as input.
   *
   * Using the same `session_id` between requests allows continuation of the conversation.
   * @param projectId Project/Agent Id.
   * @param audioFilePath The audio file to be processed.
   * @param sessionId Identifier of the DetectIntent session.
   * @param languageCode Language code of the query.
   */
  public static void detectIntentStream(String projectId, String audioFilePath, String sessionId,
      String languageCode) throws Throwable {
    // Start bi-directional StreamingDetectIntent stream.
    final CountDownLatch notification = new CountDownLatch(1);
    final List<Throwable> responseThrowables = new ArrayList<>();
    final List<StreamingDetectIntentResponse> responses = new ArrayList<>();

    // Instantiates a client
    try (SessionsClient sessionsClient = SessionsClient.create()) {
      // Set the session name using the sessionId (UUID) and projectID (my-project-id)
      SessionName session = SessionName.of(projectId, sessionId);
      System.out.println("Session Path: " + session.toString());

      // Note: hard coding audioEncoding and sampleRateHertz for simplicity.
      // Audio encoding of the audio content sent in the query request.
      AudioEncoding audioEncoding = AudioEncoding.AUDIO_ENCODING_LINEAR_16;
      int sampleRateHertz = 16000;

      // Instructs the speech recognizer how to process the audio content.
      InputAudioConfig inputAudioConfig = InputAudioConfig.newBuilder()
          .setAudioEncoding(audioEncoding) // audioEncoding = AudioEncoding.AUDIO_ENCODING_LINEAR_16
          .setLanguageCode(languageCode) // languageCode = "en-US"
          .setSampleRateHertz(sampleRateHertz) // sampleRateHertz = 16000
          .build();

      ApiStreamObserver<StreamingDetectIntentResponse> responseObserver =
          new ApiStreamObserver<StreamingDetectIntentResponse>() {
            @Override
            public void onNext(StreamingDetectIntentResponse response) {
              // Do something when receive a response
              responses.add(response);
            }

            @Override
            public void onError(Throwable t) {
              // Add error-handling
              responseThrowables.add(t);
            }

            @Override
            public void onCompleted() {
              // Do something when complete.
              notification.countDown();
            }
          };

      // Performs the streaming detect intent callable request
      ApiStreamObserver<StreamingDetectIntentRequest> requestObserver =
          sessionsClient.streamingDetectIntentCallable().bidiStreamingCall(responseObserver);

      // Build the query with the InputAudioConfig
      QueryInput queryInput = QueryInput.newBuilder().setAudioConfig(inputAudioConfig).build();

      try (FileInputStream audioStream = new FileInputStream(audioFilePath)) {
        // The first request contains the configuration
        StreamingDetectIntentRequest request = StreamingDetectIntentRequest.newBuilder()
            .setSession(session.toString())
            .setQueryInput(queryInput)
            .build();

        // Make the first request
        requestObserver.onNext(request);

        // Following messages: audio chunks. We just read the file in fixed-size chunks. In reality
        // you would split the user input by time.
        byte[] buffer = new byte[4096];
        int bytes;
        while ((bytes = audioStream.read(buffer)) != -1) {
          requestObserver.onNext(
              StreamingDetectIntentRequest.newBuilder()
                  .setInputAudio(ByteString.copyFrom(buffer, 0, bytes))
                  .build());
        }
      } catch (RuntimeException e) {
        // Cancel stream.
        requestObserver.onError(e);
      }
      // Half-close the stream.
      requestObserver.onCompleted();
      // Wait for the final response (without explicit timeout).
      notification.await();
      // Process errors/responses.
      if (!responseThrowables.isEmpty()) {
        throw responseThrowables.get(0);
      }
      if (responses.isEmpty()) {
        throw new RuntimeException("No response from Dialogflow.");
      }

      for (StreamingDetectIntentResponse response : responses) {
        if (response.hasRecognitionResult()) {
          System.out.format(
              "Intermediate transcript: '%s'\n", response.getRecognitionResult().getTranscript());
        }
      }

      // Display the last query result
      QueryResult queryResult = responses.get(responses.size() - 1).getQueryResult();
      System.out.println("====================");
      System.out.format("Query Text: '%s'\n", queryResult.getQueryText());
      System.out.format("Detected Intent: %s (confidence: %f)\n",
          queryResult.getIntent().getDisplayName(), queryResult.getIntentDetectionConfidence());
      System.out.format("Fulfillment Text: '%s'\n", queryResult.getFulfillmentText());
    }
  }
  // [END dialogflow_detect_intent_streaming]

  // [START run_application]
  public static void main(String[] args) throws Throwable {
    String audioFilePath = "";
    String projectId = "";
    String sessionId = UUID.randomUUID().toString();
    String languageCode = "en-US";

    try {
      String command = args[0];
      if (command.equals("--projectId")) {
        projectId = args[1];
      }

      command = args[2];
      if (command.equals("--audioFilePath")) {
        audioFilePath = args[3];
      }

      for (int i = 4; i < args.length; i += 2) {
        if (args[i].equals("--sessionId")) {
          sessionId = args[i + 1];
        } else if (args[i].equals("--languageCode")) {
          languageCode = args[i + 1];
        }
      }
    } catch (Exception e) {
      System.out.println("Usage:");
      System.out.println("mvn exec:java -DDetectIntentStream "
          + "-Dexec.args='--projectId PROJECT_ID  --audioFilePath resources/book_a_room.wav "
          + "--sessionId SESSION_ID'\n");

      System.out.println("Commands: --audioFilePath");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println("\t--audioFilePath <audioFilePath> - Path to the audio file");
      System.out.println("Optional Commands:");
      System.out.println("\t--languageCode <language-code> - Language Code of the query (Defaults "
          + "to \"en-US\".)");
      System.out.println("\t--sessionId <sessionId> - Identifier of the DetectIntent session "
          + "(Defaults to a random UUID.)");
    }

    detectIntentStream(projectId, audioFilePath, sessionId, languageCode);
  }
  // [END run_application]
}
