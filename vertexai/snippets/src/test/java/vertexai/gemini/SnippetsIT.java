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

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION = "us-central1";
  private static final String GEMINI_PRO_VISION = "gemini-pro-vision";
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 120000; // 2 minutes
  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(
      MAX_ATTEMPT_COUNT,
      INITIAL_BACKOFF_MILLIS);
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;

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
    URL urlObj = new URL(url);
    HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
    connection.setRequestMethod("GET");

    int responseCode = connection.getResponseCode();

    if (responseCode == HttpURLConnection.HTTP_OK) {
      InputStream inputStream = connection.getInputStream();
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }

      return outputStream.toByteArray();
    } else {
      throw new RuntimeException("Error fetching file: " + responseCode);
    }
  }

  @Before
  public void beforeEach() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void afterEach() {
    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @Test
  public void testChatSession() throws IOException {
    ChatDiscussion.chatDiscussion(PROJECT_ID, LOCATION, GEMINI_PRO_VISION);
    assertThat(out.toString()).contains("Chat Ended.");
  }

  @Test
  public void testMultimodalMultiImage() throws IOException {
    MultimodalMultiImage.multimodalMultiImage(PROJECT_ID, LOCATION, GEMINI_PRO_VISION);
    assertThat(out.toString()).contains("city: Rio de Janeiro, Landmark: Christ the Redeemer");
  }

  @Test
  public void testMultimodalQuery() throws Exception {
    String imageUri = "https://storage.googleapis.com/generativeai-downloads/images/scones.jpg";
    String dataImageBase64 = Base64.getEncoder().encodeToString(readImageFile(imageUri));
    String output = MultimodalQuery.multimodalQuery(PROJECT_ID, LOCATION, GEMINI_PRO_VISION,
        dataImageBase64);
    assertThat(output).isNotEmpty();
  }

  @Test
  public void testMultimodalVideoInput() throws IOException {
    MultimodalVideoInput.multimodalVideoInput(PROJECT_ID, LOCATION, GEMINI_PRO_VISION);
    assertThat(out.toString()).contains("Zootopia");
  }

  @Test @Ignore("Don't test until ultra launch")
  public void testMultiTurnMultimodal() throws IOException {
    MultiTurnMultimodal.multiTurnMultimodal(PROJECT_ID, LOCATION, "gemini-ultra-vision");
    assertThat(out.toString()).contains("scones");
  }

  @Test
  public void testSimpleQuestionAnswer() throws Exception {
    String output = QuestionAnswer.simpleQuestion(PROJECT_ID, LOCATION, GEMINI_PRO_VISION);
    assertThat(output).isNotEmpty();
    assertThat(output).contains("Rayleigh scattering");
  }

  @Test
  public void testQuickstart() throws IOException {
    Quickstart.quickstart(PROJECT_ID, LOCATION, GEMINI_PRO_VISION);
    assertThat(out.toString()).contains("scones");
  }

  @Test
  public void testSingleTurnMultimodal() throws IOException {
    String imageUri = "https://storage.googleapis.com/generativeai-downloads/images/scones.jpg";
    String dataImageBase64 = Base64.getEncoder().encodeToString(readImageFile(imageUri));
    SingleTurnMultimodal.generateContent(PROJECT_ID, LOCATION, GEMINI_PRO_VISION,
        "What is this image", dataImageBase64);
    assertThat(out.toString()).contains("scones");
  }

  @Test
  public void testStreamingQuestions() throws Exception {
    StreamingQuestionAnswer.streamingQuestion(PROJECT_ID, LOCATION,
        GEMINI_PRO_VISION);
    assertThat(out.toString()).contains("Rayleigh scattering");
  }

  @Test
  public void testSafetySettings() throws Exception {
    String textPrompt = "";

    String output = WithSafetySettings.safetyCheck(PROJECT_ID, LOCATION, GEMINI_PRO_VISION,
        textPrompt);
    assertThat(output).isNotEmpty();
    assertThat(output).contains("reasons?");
  }
}
