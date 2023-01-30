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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.dialogflow.v2.AnswerRecordName;
import com.google.cloud.dialogflow.v2.ConversationName;
import com.google.cloud.dialogflow.v2.ConversationProfileName;
import com.google.cloud.dialogflow.v2.ConversationProfilesClient;
import com.google.cloud.dialogflow.v2.Participant.Role;
import com.google.cloud.dialogflow.v2.ParticipantName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class UpdateAnswerRecordTest {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String CONVERSATION_PROFILE_DISPLAY_NAME = UUID.randomUUID().toString();
  private static final String TEST_KNOWLEDGE_BASE_ID = "MTA0NTkyOTg0MjMwMjE1MDI0NjQ";
  private static final String LOCATION = "global";
  private static final String NAME_PREFIX_IN_OUTPUT = "Name: ";
  private ConversationProfileName conversationProfileName;
  private ConversationName conversationName;
  private ParticipantName endUserName;
  private ParticipantName humanAgentName;
  private AnswerRecordName answerRecordName;
  private ByteArrayOutputStream bout;
  private PrintStream newOutputStream;
  private PrintStream originalOutputStream;

  private static void requireEnvVar(String varName) {
    assertNotNull(System.getenv(varName));
  }

  // Extract the name of a newly created resource from latest "Name: %s\n" in sample code output
  private static String getResourceNameFromOutputString(String output) {
    return output.substring(
        output.lastIndexOf(NAME_PREFIX_IN_OUTPUT) + NAME_PREFIX_IN_OUTPUT.length(),
        output.length() - 1);
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void setUp()
      throws IOException, ApiException, InterruptedException, ExecutionException, TimeoutException {
    originalOutputStream = System.out;
    bout = new ByteArrayOutputStream();
    newOutputStream = new PrintStream(bout);
    System.setOut(newOutputStream);

    // Create a conversation profile
    ConversationProfileManagement.createConversationProfileArticleSuggestion(
        PROJECT_ID,
        CONVERSATION_PROFILE_DISPLAY_NAME,
        LOCATION,
        Optional.of(TEST_KNOWLEDGE_BASE_ID));
    String output = bout.toString();
    assertThat(output).contains(NAME_PREFIX_IN_OUTPUT);
    conversationProfileName =
        ConversationProfileName.parse(getResourceNameFromOutputString(output));

    // Create a conversation
    ConversationManagement.createConversation(
        PROJECT_ID, LOCATION, conversationProfileName.getConversationProfile());
    output = bout.toString();
    assertThat(output).contains(NAME_PREFIX_IN_OUTPUT);
    conversationName = ConversationName.parse(getResourceNameFromOutputString(output));

    // Create a END_USER participant
    ParticipantManagement.createParticipant(
        PROJECT_ID, LOCATION, conversationName.getConversation(), Role.END_USER);
    output = bout.toString();
    assertThat(output).contains(NAME_PREFIX_IN_OUTPUT);
    endUserName = ParticipantName.parse(getResourceNameFromOutputString(output));

    // Create a HUMAN_AGENT participant
    ParticipantManagement.createParticipant(
        PROJECT_ID, LOCATION, conversationName.getConversation(), Role.HUMAN_AGENT);
    output = bout.toString();
    assertThat(output).contains(NAME_PREFIX_IN_OUTPUT);
    humanAgentName = ParticipantName.parse(getResourceNameFromOutputString(output));
  }

  @After
  public void tearDown() throws IOException {
    // Delete the created conversation profile
    try (ConversationProfilesClient conversationProfilesClient =
        ConversationProfilesClient.create()) {
      conversationProfilesClient.deleteConversationProfile(conversationProfileName.toString());
    }

    System.setOut(originalOutputStream);
  }

  @Test
  public void testUpdateAnswerRecord() throws IOException {
    // Send AnalyzeContent Requests
    ParticipantManagement.analyzeContent(
        PROJECT_ID,
        LOCATION,
        conversationName.getConversation(),
        humanAgentName.getParticipant(),
        "Hi, what can I help with?");
    ParticipantManagement.analyzeContent(
        PROJECT_ID,
        LOCATION,
        conversationName.getConversation(),
        endUserName.getParticipant(),
        "I want to return my order.");
    String output = bout.toString();
    assertThat(output).contains("Message Content: ");
    answerRecordName = AnswerRecordName.parse(getResourceNameFromOutputString(output));

    // Update a latest answer record
    AnswerRecordManagement.updateAnswerRecord(
        answerRecordName.getProject(),
        answerRecordName.getLocation(),
        answerRecordName.getAnswerRecord(),
        true);
    output = bout.toString();
    assertThat(getResourceNameFromOutputString(output)).contains(answerRecordName.toString());
    assertThat(output).contains("Clicked: true");
  }
}
