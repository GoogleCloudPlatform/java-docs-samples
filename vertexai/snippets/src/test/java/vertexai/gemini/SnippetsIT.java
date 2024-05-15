/*
 * Copyright 2023 Google LLC
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

// Tests for Gemini code samples.

package vertexai.gemini;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import javax.net.ssl.HttpsURLConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SnippetsIT {

  public static final String GEMINI_ULTRA_VISION = "gemini-1.0-ultra-vision";
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION = "us-central1";
  private static final String GEMINI_PRO_VISION = "gemini-1.0-pro-vision-001";
  private static final String GEMINI_PRO = "gemini-1.0-pro-002";
  private static final String GEMINI_PRO_1_5 = "gemini-1.5-pro-preview-0409";
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 120000; // 2 minutes

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule =
      new MultipleAttemptsRule(MAX_ATTEMPT_COUNT, INITIAL_BACKOFF_MILLIS);

  private final PrintStream printStream = System.out;
  private ByteArrayOutputStream bout;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void setUp() throws IOException {
    try (PrintStream out = System.out) {
      ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
      System.setOut(new PrintStream(stdOut));

      requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
      requireEnvVar("GOOGLE_CLOUD_PROJECT");

      stdOut.close();
      System.setOut(out);
    }
  }

  // Reads the image data from the given URL.
  public static byte[] readImageFile(String url) throws IOException {
    if (url == null || url.isEmpty()) {
      throw new IllegalArgumentException("Invalid URL: " + url);
    }
    URL urlObj = new URL(url);
    HttpsURLConnection connection = null;
    InputStream inputStream = null;
    ByteArrayOutputStream outputStream = null;

    try {
      connection = (HttpsURLConnection) urlObj.openConnection();
      connection.setRequestMethod("GET");

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        inputStream = connection.getInputStream();
        outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
      } else {
        throw new IOException("Error fetching file: " + responseCode);
      }
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
      if (outputStream != null) {
        outputStream.close();
      }
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  @Before
  public void beforeEach() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void afterEach() {
    System.out.flush();
    System.setOut(printStream);
  }

  @Test
  public void testChatSession() throws IOException {
    ChatDiscussion.chatDiscussion(PROJECT_ID, LOCATION, GEMINI_PRO);
    assertThat(bout.toString()).contains("Chat Ended.");
  }

  @Test
  public void testMultimodalMultiImage() throws IOException {
    MultimodalMultiImage.multimodalMultiImage(PROJECT_ID, LOCATION, GEMINI_PRO_VISION);
    assertThat(bout.toString()).contains("city: Rio de Janeiro, Landmark: Christ the Redeemer");
  }

  @Test
  public void testMultimodalQuery() throws Exception {
    String imageUri =
        "https://storage.googleapis.com/cloud-samples-data/vertex-ai/llm/prompts/landmark1.png";
    String dataImageBase64 = Base64.getEncoder().encodeToString(readImageFile(imageUri));
    String output =
        MultimodalQuery.multimodalQuery(PROJECT_ID, LOCATION, GEMINI_PRO_VISION, dataImageBase64);
    assertThat(output).isNotEmpty();
  }

  @Test
  public void testMultimodalVideoInput() throws IOException {
    MultimodalVideoInput.multimodalVideoInput(PROJECT_ID, LOCATION, GEMINI_PRO_VISION);
    assertThat(bout.toString()).contains("Zoo");
  }

  @Ignore("Don't test until ultra launch")
  @Test
  public void testMultiTurnMultimodal() throws IOException {
    MultiTurnMultimodal.multiTurnMultimodal(PROJECT_ID, LOCATION, GEMINI_ULTRA_VISION);
    assertThat(bout.toString()).contains("scones");
  }

  @Test
  public void testSimpleQuestionAnswer() throws Exception {
    String output = QuestionAnswer.simpleQuestion(PROJECT_ID, LOCATION, GEMINI_PRO_VISION);
    assertThat(output).isNotEmpty();
    assertThat(output).contains("Rayleigh scattering");
  }

  @Test
  public void testQuickstart() throws IOException {
    String output = Quickstart.quickstart(PROJECT_ID, LOCATION, GEMINI_PRO_VISION);
    assertThat(output).contains("Colosseum");
  }

  @Test
  public void testSingleTurnMultimodal() throws IOException {
    String imageUri =
        "https://storage.googleapis.com/cloud-samples-data/vertex-ai/llm/prompts/landmark1.png";
    String dataImageBase64 = Base64.getEncoder().encodeToString(readImageFile(imageUri));
    SingleTurnMultimodal.generateContent(
        PROJECT_ID, LOCATION, GEMINI_PRO_VISION, "What is this image", dataImageBase64);
    assertThat(bout.toString()).contains("Colosseum");
  }

  @Test
  public void testStreamingQuestions() throws Exception {
    StreamingQuestionAnswer.streamingQuestion(PROJECT_ID, LOCATION, GEMINI_PRO_VISION);
    assertThat(bout.toString()).contains("Rayleigh scattering");
  }

  @Test
  public void testTextInput() throws Exception {
    String textPrompt =
        "What's a good name for a flower shop that specializes in selling bouquets of"
            + " dried flowers?";
    String output = TextInput.textInput(PROJECT_ID, LOCATION, GEMINI_PRO, textPrompt);
    assertThat(output).isNotEmpty();
  }

  @Test
  public void testSafetySettings() throws Exception {
    String textPrompt = "Hello World!";

    String output =
        WithSafetySettings.safetyCheck(PROJECT_ID, LOCATION, GEMINI_PRO_VISION, textPrompt);
    assertThat(output).isNotEmpty();
    assertThat(output).contains("reasons?");
  }

  @Test
  public void testTokenCount() throws Exception {
    String textPrompt = "Why is the sky blue?";

    int tokenCount =
        GetTokenCount.getTokenCount(PROJECT_ID, LOCATION, GEMINI_PRO_VISION, textPrompt);
    assertThat(tokenCount).isEqualTo(6);
  }

  @Test
  public void testFunctionCalling() throws Exception {
    String textPrompt = "What's the weather in Paris?";

    String answer =
        FunctionCalling.whatsTheWeatherLike(PROJECT_ID, LOCATION, GEMINI_PRO, textPrompt);
    assertThat(answer).ignoringCase().contains("Paris");
    assertThat(answer).ignoringCase().contains("sunny");
  }

  @Test
  public void testAudioInputSummary() throws IOException {
    String output = AudioInputSummarization.summarizeAudio(PROJECT_ID, LOCATION, GEMINI_PRO_1_5);

    assertThat(output).ignoringCase().contains("Pixel");
    assertThat(output).ignoringCase().contains("feature");
  }

  @Test
  public void testAudioInputTranscription() throws IOException {
    String output = AudioInputTranscription.transcribeAudio(PROJECT_ID, LOCATION, GEMINI_PRO_1_5);

    assertThat(output).ignoringCase().contains("Pixel");
    assertThat(output).ignoringCase().contains("feature");
  }

  @Test
  public void testVideoAudioInput() throws IOException {
    String output = VideoInputWithAudio.videoAudioInput(PROJECT_ID, LOCATION, GEMINI_PRO_1_5);

    assertThat(output).ignoringCase().contains("Pixel");
    assertThat(output).ignoringCase().contains("Tokyo");
  }

  @Test
  public void testAllModalityInputs() throws IOException {
    String output = MultimodalAllInput.multimodalAllInput(PROJECT_ID, LOCATION, GEMINI_PRO_1_5);

    assertThat(output).ignoringCase().contains("0:49");
  }

  @Test
  public void testPdfInput() throws IOException {
    String output = PdfInput.pdfInput(PROJECT_ID, LOCATION, GEMINI_PRO_1_5);

    assertThat(output).ignoringCase().contains("Gemini");
  }

  @Test
  public void testSystemInstruction() throws Exception {
    String output = WithSystemInstruction.translateToFrench(PROJECT_ID, LOCATION, GEMINI_PRO_1_5);

    assertThat(output).ignoringCase().contains("bagels");
    assertThat(output).ignoringCase().contains("aime");
  }
}
