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
import com.google.cloud.dialogflow.v2.AudioEncoding;
import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.InputAudioConfig;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.protobuf.ByteString;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
// [END dialogflow_import_libraries]


/**
 * DialogFlow API Detect Intent sample with audio files.
 */
public class DetectIntentAudio {

  // [START dialogflow_detect_intent_audio]
  /**
   * Returns the result of detect intent with an audio file as input.
   *
   * Using the same `session_id` between requests allows continuation of the conversation.
   * @param projectId Project/Agent Id.
   * @param audioFilePath Path to the audio file.
   * @param sessionId Identifier of the DetectIntent session.
   * @param languageCode Language code of the query.
   */
  public static void detectIntentAudio(String projectId, String audioFilePath, String sessionId,
      String languageCode)
      throws Exception {
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

      // Build the query with the InputAudioConfig
      QueryInput queryInput = QueryInput.newBuilder().setAudioConfig(inputAudioConfig).build();

      // Read the bytes from the audio file
      byte[] inputAudio = Files.readAllBytes(Paths.get(audioFilePath));

      // Build the DetectIntentRequest
      DetectIntentRequest request = DetectIntentRequest.newBuilder()
          .setSession(session.toString())
          .setQueryInput(queryInput)
          .setInputAudio(ByteString.copyFrom(inputAudio))
          .build();

      // Performs the detect intent request
      DetectIntentResponse response = sessionsClient.detectIntent(request);

      // Display the query result
      QueryResult queryResult = response.getQueryResult();
      System.out.println("====================");
      System.out.format("Query Text: '%s'\n", queryResult.getQueryText());
      System.out.format("Detected Intent: %s (confidence: %f)\n",
          queryResult.getIntent().getDisplayName(), queryResult.getIntentDetectionConfidence());
      System.out.format("Fulfillment Text: '%s'\n", queryResult.getFulfillmentText());
    }
  }
  // [END dialogflow_detect_intent_audio]

  // [START run_application]
  public static void main(String[] args) throws Exception {
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
      System.out.println("mvn exec:java -DDetectIntentAudio "
          + "-Dexec.args='--projectId PROJECT_ID --audioFilePath resources/book_a_room.wav "
          + "--sessionId SESSION_ID'\n");

      System.out.println("Commands: audioFilePath");
      System.out.println("\t--audioFilePath <audioFilePath> - Path to the audio file");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println("Optional Commands:");
      System.out.println("\t--languageCode <language-code> - Language Code of the query (Defaults "
          + "to \"en-US\".)");
      System.out.println("\t--sessionId <sessionId> - Identifier of the DetectIntent session "
          + "(Defaults to a random UUID.)");
    }

    detectIntentAudio(projectId, audioFilePath, sessionId, languageCode);
  }
  // [END run_application]
}
