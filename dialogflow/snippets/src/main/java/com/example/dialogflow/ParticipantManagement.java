/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.dialogflow;

// [START dialogflow_create_participant]

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.dialogflow.v2.AnalyzeContentRequest;
import com.google.cloud.dialogflow.v2.AnalyzeContentResponse;
import com.google.cloud.dialogflow.v2.ArticleAnswer;
import com.google.cloud.dialogflow.v2.ConversationName;
import com.google.cloud.dialogflow.v2.FaqAnswer;
import com.google.cloud.dialogflow.v2.Participant;
import com.google.cloud.dialogflow.v2.Participant.Role;
import com.google.cloud.dialogflow.v2.ParticipantName;
import com.google.cloud.dialogflow.v2.ParticipantsClient;
import com.google.cloud.dialogflow.v2.SmartReplyAnswer;
import com.google.cloud.dialogflow.v2.SuggestionResult;
import com.google.cloud.dialogflow.v2.TextInput;
import java.io.IOException;
import java.util.List;

public class ParticipantManagement {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "my-location";

    // Set conversation id for the new participant,
    // See com.example.dialogflow.ConversationManagement sample code
    // for how to create a conversation.
    String conversationId = "my-conversation-id";
    Role role = Role.END_USER;

    // Create a participant
    createParticipant(projectId, location, conversationId, role);
  }

  // Create a participant with given role.
  public static void createParticipant(
      String projectId, String location, String conversationId, Role role)
      throws ApiException, IOException {
    try (ParticipantsClient participantsClient = ParticipantsClient.create()) {
      ConversationName conversationName =
          ConversationName.ofProjectLocationConversationName(projectId, location, conversationId);
      Participant participant = Participant.newBuilder().setRole(role).build();
      Participant newParticipant =
          participantsClient.createParticipant(conversationName, participant);
      System.out.println("====================");
      System.out.println("Participant Created:");
      System.out.format("Role: %s\n", newParticipant.getRole());
      System.out.format("Name: %s\n", newParticipant.getName());
    }
  }

  // Process suggestion results embedded in the response of an analyze content request.
  public static void processSuggestionResults(List<SuggestionResult> suggestionResults) {
    for (SuggestionResult suggestionResult : suggestionResults) {
      if (suggestionResult.hasError()) {
        System.out.format("Error: %s\n", suggestionResult.getError().getMessage());
      }
      switch (suggestionResult.getSuggestionResponseCase()) {
        case SUGGEST_ARTICLES_RESPONSE:
          for (ArticleAnswer articleAnswer :
              suggestionResult.getSuggestArticlesResponse().getArticleAnswersList()) {
            System.out.format("Article Suggestion Answer: %s\n", articleAnswer.getTitle());
            System.out.format("Answer Record Name: %s\n", articleAnswer.getAnswerRecord());
          }
          break;
        case SUGGEST_FAQ_ANSWERS_RESPONSE:
          for (FaqAnswer faqAnswer :
              suggestionResult.getSuggestFaqAnswersResponse().getFaqAnswersList()) {
            System.out.format("Faq Answer: %s\n", faqAnswer.getAnswer());
            System.out.format("Answer Record Name: %s\n", faqAnswer.getAnswerRecord());
          }
          break;
        case SUGGEST_SMART_REPLIES_RESPONSE:
          for (SmartReplyAnswer smartReplyAnswer :
              suggestionResult.getSuggestSmartRepliesResponse().getSmartReplyAnswersList()) {
            System.out.format("Smart Reply: %s\n", smartReplyAnswer.getReply());
            System.out.format("Answer Record Name: %s\n", smartReplyAnswer.getAnswerRecord());
          }
          break;
        case SUGGESTIONRESPONSE_NOT_SET:
          System.out.println("Suggestion Response is not set.");
          break;
        case ERROR:
          System.out.format("Error: %s\n", suggestionResult.getError().getMessage());
          break;
        default:
          System.out.println("Suggestion Response is not supported.");
          break;
      }
    }
  }

  // Analyze text message content from a participant.
  public static void analyzeContent(
      String projectId,
      String location,
      String conversationId,
      String participantId,
      String textInput)
      throws IOException {
    try (ParticipantsClient participantsClient = ParticipantsClient.create()) {
      ParticipantName participantName =
          ParticipantName.ofProjectLocationConversationParticipantName(
              projectId, location, conversationId, participantId);
      AnalyzeContentRequest request =
          AnalyzeContentRequest.newBuilder()
              .setParticipant(participantName.toString())
              .setTextInput(TextInput.newBuilder().setText(textInput))
              .build();
      AnalyzeContentResponse response = participantsClient.analyzeContent(request);
      System.out.println("====================");
      System.out.println("AnalyzeContent Requested:");
      System.out.format("Message Content: %s\n", response.getMessage().getContent());
      System.out.format("Reply Text: %s\n", response.getReplyText());

      processSuggestionResults(response.getHumanAgentSuggestionResultsList());
      processSuggestionResults(response.getEndUserSuggestionResultsList());
    }
  }
}
// [END dialogflow_create_participant]
