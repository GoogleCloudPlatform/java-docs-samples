/*
  Copyright 2018, Google, Inc.

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
import com.google.cloud.dialogflow.v2beta1.DetectIntentRequest;
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.v2beta1.KnowledgeAnswers;
import com.google.cloud.dialogflow.v2beta1.KnowledgeAnswers.Answer;
import com.google.cloud.dialogflow.v2beta1.KnowledgeBaseName;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.QueryParameters;
import com.google.cloud.dialogflow.v2beta1.QueryResult;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;
import com.google.cloud.dialogflow.v2beta1.TextInput;
import com.google.cloud.dialogflow.v2beta1.TextInput.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// [END dialogflow_import_libraries]

/** DialogFlow API Detect Intent sample with querying knowledge connector. */
public class DetectIntentKnowledge {

  // [START dialogflow_detect_intent_knowledge]
  /**
   * Returns the result of detect intent with text as input.
   *
   * <p>Using the same `session_id` between requests allows continuation of the conversation.
   *
   * @param projectId Project/Agent Id.
   * @param knowledgeBaseId Knowledge base Id.
   * @param sessionId Identifier of the DetectIntent session.
   * @param languageCode Language code of the query.
   * @param texts The texts to be processed.
   * @throws Exception
   */
  public static void detectIntentKnowledge(
      String projectId,
      String knowledgeBaseId,
      String sessionId,
      String languageCode,
      List<String> texts)
      throws Exception {
    // Instantiates a client
    try (SessionsClient sessionsClient = SessionsClient.create()) {
      // Set the session name using the sessionId (UUID) and projectID (my-project-id)
      SessionName session = SessionName.of(projectId, sessionId);
      System.out.println("Session Path: " + session.toString());

      // Detect intents for each text input
      for (String text : texts) {
        // Set the text and language code (en-US) for the query
        Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);
        // Build the query with the TextInput
        QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

        KnowledgeBaseName knowledgeBaseName = KnowledgeBaseName.of(projectId, knowledgeBaseId);
        QueryParameters queryParameters =
            QueryParameters.newBuilder()
                .addKnowledgeBaseNames(knowledgeBaseName.toString())
                .build();

        DetectIntentRequest detectIntentRequest =
            DetectIntentRequest.newBuilder()
                .setSession(session.toString())
                .setQueryInput(queryInput)
                .setQueryParams(queryParameters)
                .build();
        // Performs the detect intent request
        DetectIntentResponse response = sessionsClient.detectIntent(detectIntentRequest);

        // Display the query result
        QueryResult queryResult = response.getQueryResult();

        System.out.format("Knowledge results:\n");
        System.out.format("====================\n");
        System.out.format("Query Text: '%s'\n", queryResult.getQueryText());
        System.out.format(
            "Detected Intent: %s (confidence: %f)\n",
            queryResult.getIntent().getDisplayName(), queryResult.getIntentDetectionConfidence());
        System.out.format("Fulfillment Text: '%s'\n", queryResult.getFulfillmentText());
        KnowledgeAnswers knowledgeAnswers = queryResult.getKnowledgeAnswers();
        for (Answer answer : knowledgeAnswers.getAnswersList()) {
          System.out.format(" - Answer: '%s'\n", answer.getAnswer());
          System.out.format(" - Confidence: '%s'\n", answer.getMatchConfidence());
        }
      }
    }
  }

  // [END dialogflow_detect_intent_knowledge]

  // [START run_application]
  public static void main(String[] args) throws Exception {
    ArrayList<String> texts = new ArrayList<>();
    String projectId = "";
    String sessionId = UUID.randomUUID().toString();
    String languageCode = "en-US";
    String knowledgeBaseId = "";

    try {
      String command = args[0];
      if (command.equals("--projectId")) {
        projectId = args[1];
      }
      command = args[2];
      if (command.equals("--knowledgeBaseId")) {
        knowledgeBaseId = args[3];
      }

      for (int i = 4; i < args.length; i++) {
        switch (args[i]) {
          case "--sessionId":
            sessionId = args[++i];
            break;
          case "--languageCode":
            languageCode = args[++i];
            break;
          default:
            texts.add(args[i]);
            break;
        }
      }

    } catch (Exception e) {
      System.out.println("Usage:");
      System.out.println(
          "mvn exec:java -DDetectIntentKnowledge "
              + "-Dexec.args=\"--projectId PROJECT_ID --knowledgeBaseId KNOWLEDGE_BASE_ID --sessionId SESSION_ID "
              + "'Where can I find pricing information?\"\n");
      System.out.println("Commands: ");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println("\t--knowledgeBaseId <knowledgeBaseId> - KnowledgeBase Id ");
      System.out.println(
          "\tText: \"hello\" \"book a meeting room\" \"Mountain View\" \"tomorrow\" "
              + "\"10am\" \"2 hours\" \"10 people\" \"A\" \"yes\"");
      System.out.println("Optional Commands:");
      System.out.println(
          "\t--languageCode <languageCode> - Language Code of the query (Defaults "
              + "to \"en-US\".)");
      System.out.println(
          "\t--sessionId <sessionId> - Identifier of the DetectIntent session "
              + "(Defaults to a random UUID.)");
    }

    detectIntentKnowledge(projectId, knowledgeBaseId, sessionId, languageCode, texts);
  }
  // [END run_application]
}
