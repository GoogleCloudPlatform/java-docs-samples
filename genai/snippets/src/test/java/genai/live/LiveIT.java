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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.genai.AsyncLive;
import com.google.genai.AsyncSession;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.LiveConnectConfig;
import com.google.genai.types.LiveServerContent;
import com.google.genai.types.LiveServerMessage;
import com.google.genai.types.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
public class LiveIT {

  private static final String GEMINI_FLASH_LIVE_PREVIEW = "gemini-2.0-flash-live-preview-04-09";
  private static final String GEMINI_FLASH_LIVE_PREVIEW_NATIVE_AUDIO =
      "gemini-live-2.5-flash-preview-native-audio";
  private static final String GEMINI_FLASH_LIVE_PREVIEW_NATIVE_AUDIO_09_2025 =
      "gemini-live-2.5-flash-preview-native-audio-09-2025";
  private ByteArrayOutputStream bout;
  private PrintStream out;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
  }

  @Test
  public void testLiveAudioWithTxt() {
    LiveAudioWithTxt.generateContent(GEMINI_FLASH_LIVE_PREVIEW);
    String output = bout.toString();
    assertThat(output).contains("> Hello? Gemini, are you there?");
    assertThat(output).contains("Successfully saved audio to: ");
  }

  @Test
  public void testLiveCodeExecWithTxt() {
    String response = LiveCodeExecWithTxt.generateContent(GEMINI_FLASH_LIVE_PREVIEW);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testLiveConversationAudioWithAudio() throws IOException {
    LiveConversationAudioWithAudio.generateContent(GEMINI_FLASH_LIVE_PREVIEW_NATIVE_AUDIO_09_2025);
    String output = bout.toString();
    assertThat(output).contains("Input transcription:");
    assertThat(output).contains("Output transcription:");
    assertThat(output).contains("Successfully saved audio to:");
  }

  @Test
  public void testLiveFuncCallWithTxt() {
    LiveFuncCallWithTxt.generateContent(GEMINI_FLASH_LIVE_PREVIEW);
    String output = bout.toString();
    assertThat(output).contains("> Turn off the lights please");
    assertThat(output).contains("Function name: turn_off_the_lights");
    assertThat(output).contains("result=ok");
  }

  @Test
  public void testLiveGroundGoogSearchWithTxt() {
    String response = LiveGroundGoogSearchWithTxt.generateContent(GEMINI_FLASH_LIVE_PREVIEW);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testLiveGroundRagEngineWithTxt() throws NoSuchFieldException, IllegalAccessException {

    Client.Builder mockedBuilder = mock(Client.Builder.class, RETURNS_SELF);
    Client mockedClient = mock(Client.class);
    Client.Async mockedAsync = mock(Client.Async.class);
    AsyncLive mockedLive = mock(AsyncLive.class);
    AsyncSession mockedSession = mock(AsyncSession.class);

    try (MockedStatic<Client> mockedStatic = mockStatic(Client.class)) {
      mockedStatic.when(Client::builder).thenReturn(mockedBuilder);
      when(mockedBuilder.build()).thenReturn(mockedClient);

      // Using reflection because async and live are final fields and cannot be mocked.
      Field asyncField = Client.class.getDeclaredField("async");
      asyncField.setAccessible(true);
      asyncField.set(mockedClient, mockedAsync);

      Field liveField = Client.Async.class.getDeclaredField("live");
      liveField.setAccessible(true);
      liveField.set(mockedAsync, mockedLive);

      when(mockedClient.async.live.connect(anyString(), any(LiveConnectConfig.class)))
          .thenReturn(CompletableFuture.completedFuture(mockedSession));

      when(mockedSession.sendClientContent(any()))
          .thenReturn(CompletableFuture.completedFuture(null));

      when(mockedSession.close()).thenReturn(CompletableFuture.completedFuture(null));

      // Simulates the server's behavior
      doAnswer(
              invocation -> {
                LiveServerMessage textMessage = mock(LiveServerMessage.class);
                LiveServerContent textServerContent = mock(LiveServerContent.class);
                Content textContent = mock(Content.class);
                Part textPart = mock(Part.class);

                // Sends a text message.
                when(textMessage.serverContent()).thenReturn(Optional.of(textServerContent));
                when(textServerContent.modelTurn()).thenReturn(Optional.of(textContent));
                when(textContent.parts()).thenReturn(Optional.of(List.of(textPart)));
                when(textPart.text()).thenReturn(Optional.of("The newest model is Gemini."));
                // The turn is not complete yet in this message.
                when(textServerContent.turnComplete()).thenReturn(Optional.of(false));

                // Gets the message handler.
                Consumer<LiveServerMessage> messageHandler = invocation.getArgument(0);
                // Sends the message to the message handler.
                messageHandler.accept(textMessage);

                // Simulates server sending the final "turn complete" message.
                LiveServerMessage completeMessage = mock(LiveServerMessage.class);
                LiveServerContent completeServerContent = mock(LiveServerContent.class);

                when(completeMessage.serverContent())
                    .thenReturn(Optional.of(completeServerContent));
                when(completeServerContent.modelTurn()).thenReturn(Optional.empty());
                // The turn is complete.
                when(completeServerContent.turnComplete()).thenReturn(Optional.of(true));
                messageHandler.accept(completeMessage);
                return null;
              })
          .when(mockedSession)
          .receive(any());

      String response =
          LiveGroundRagEngineWithTxt.generateContent(GEMINI_FLASH_LIVE_PREVIEW, "test-rag-corpus");

      assertThat(response).contains("The newest model is Gemini");
      verify(mockedSession).close();
    }
  }

  @Test
  public void testLiveStructuredOutputWithTxt() throws GeneralSecurityException, IOException {
    Optional<LiveStructuredOutputWithTxt.CalendarEvent> response =
        LiveStructuredOutputWithTxt.generateContent(
            System.getenv("GOOGLE_CLOUD_PROJECT"), "us-central1", "openapi");
    assertThat(response).isPresent();
    assertThat(response.get().name).isNotEmpty();
    assertThat(response.get().date).isNotEmpty();
    assertThat(response.get().participants).isNotEmpty();
    String output = bout.toString();
    assertThat(output).contains("name=science fair date=Friday participants=[Alice, Bob]");
  }

  @Test
  public void testLiveTranscribeWithAudio() {
    String response =
        LiveTranscribeWithAudio.generateContent(GEMINI_FLASH_LIVE_PREVIEW_NATIVE_AUDIO);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testLiveTxtWithAudio() throws IOException {
    String response = LiveTxtWithAudio.generateContent(GEMINI_FLASH_LIVE_PREVIEW);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testLiveWithTxt() {
    String response = LiveWithTxt.generateContent(GEMINI_FLASH_LIVE_PREVIEW);
    assertThat(response).isNotEmpty();
  }
}
