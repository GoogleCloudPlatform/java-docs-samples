/*
 * Copyright 2018 Google Inc.
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

package com.example.contactcenterinsights;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.contactcenterinsights.v1.Conversation;
import com.google.cloud.contactcenterinsights.v1.ConversationDataSource;
import com.google.cloud.contactcenterinsights.v1.GcsSource;
import com.google.common.collect.ImmutableList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateAnalyzeConversationTests {
  private static final String PROJECT_ID = System.getenv("PROJECT_ID");
  private static final String VOICE_TRANSCRIPT_URI = System.getenv("VOICE_TRANSCRIPT_URI");
  private static final String CHAT_TRANSCRIPT_URI = System.getenv("CHAT_TRANSCRIPT_URI");
  private ByteArrayOutputStream bout;
  private PrintStream out;

  private static void requireEnvVar(String varName) {
    assertNotNull(
        "Environment variable " + varName + " is required to perform these tests.",
        System.getenv(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("PROJECT_ID");
    requireEnvVar("VOICE_TRANSCRIPT_URI");
    requireEnvVar("CHAT_TRANSCRIPT_URI");
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  public Conversation getConversation(String transcriptUri, Conversation.Medium medium) {
    return Conversation.newBuilder()
        .setDataSource(
            ConversationDataSource.newBuilder()
                .setGcsSource(GcsSource.newBuilder().setTranscriptUri(transcriptUri).build())
                .build())
        .setMedium(medium)
        .build();
  }

  @Test
  public void testCreateAnalyzeConversationOk() {

    CreateAnalyzeConversation.createAnalyzeConversation(
        PROJECT_ID,
        "us-central1",
        "",
        getConversation(VOICE_TRANSCRIPT_URI, Conversation.Medium.PHONE_CALL));

    String output = bout.toString();
    assertThat(output).contains("Analysis created:");
  }

  @Test
  public void testCreateAnalyzeChatConversationOk() {

    CreateAnalyzeConversation.createAnalyzeConversation(
        PROJECT_ID,
        "us-central1",
        "",
        getConversation(CHAT_TRANSCRIPT_URI, Conversation.Medium.CHAT));

    String output = bout.toString();
    assertThat(output).contains("Analysis created:");
  }

  @Test
  public void testCreateAnalyzeConversationCustomConversationIdOk() {
    String conversationId = "custom-conversation-id";

    CreateAnalyzeConversation.createAnalyzeConversation(
        PROJECT_ID,
        "us-central1",
        conversationId,
        getConversation(VOICE_TRANSCRIPT_URI, Conversation.Medium.PHONE_CALL));

    String output = bout.toString();
    assertThat(output).contains("Analysis created:");
  }
}
