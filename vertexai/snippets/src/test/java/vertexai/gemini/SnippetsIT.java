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
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SnippetsIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION = "us-central1";
  private static final String GEMINI_FLASH = "gemini-2.0-flash-001";
  private static final String GEMINI_FLASH_1_5 = "gemini-2.0-flash-001";
  private static final String DATASTORE_ID = "grounding-test-datastore_1716831150046";
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 120000;
  private static final String TARGET_LANGUAGE_CODE = "fr";
  private static final String TEXT_TO_TRANSLATE =  "Hello! How are you doing today?";


  // 2 minutes

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
  public void testSimpleQuestionAnswer() throws Exception {
    String output = QuestionAnswer.simpleQuestion(PROJECT_ID, LOCATION, GEMINI_FLASH);
    assertThat(output).isNotEmpty();
  }

  @Test
  public void testQuickstart() throws IOException {
    String output = Quickstart.quickstart(PROJECT_ID, LOCATION, GEMINI_FLASH);
    assertThat(output).isNotEmpty();
  }

  @Test
  public void testStreamingQuestions() throws Exception {
    StreamingQuestionAnswer.streamingQuestion(PROJECT_ID, LOCATION, GEMINI_FLASH);
    assertThat(bout.toString()).isNotEmpty();
  }

  @Test
  public void testTextInput() throws Exception {
    String textPrompt =
        "What's a good name for a flower shop that specializes in selling bouquets of"
            + " dried flowers?";
    String output = TextInput.textInput(PROJECT_ID, LOCATION, GEMINI_FLASH, textPrompt);
    assertThat(output).isNotEmpty();
  }

  @Test
  public void testTokenCount() throws Exception {
    int tokenCount = GetTokenCount.getTokenCount(PROJECT_ID, LOCATION, GEMINI_FLASH);
    assertThat(tokenCount).isEqualTo(6);
  }

  @Test
  public void testMediaTokenCount() throws Exception {
    int tokenCount = GetMediaTokenCount.getMediaTokenCount(PROJECT_ID, LOCATION, GEMINI_FLASH);
    assertThat(tokenCount).isInstanceOf(int.class);
  }

  @Test
  public void testFunctionCalling() throws Exception {
    String textPrompt = "What's the weather in Paris?";

    String answer =
        FunctionCalling.whatsTheWeatherLike(PROJECT_ID, LOCATION, GEMINI_FLASH, textPrompt);
    assertThat(answer).ignoringCase().contains("Paris");
    assertThat(answer).ignoringCase().contains("sunny");
  }

  @Test
  public void testVideoAudioInput() throws IOException {
    String output = VideoInputWithAudio.videoAudioInput(PROJECT_ID, LOCATION, GEMINI_FLASH);

    assertThat(output).ignoringCase().contains("Pixel");
    assertThat(output).ignoringCase().contains("Tokyo");
  }

  // @Test
  // public void testGroundingWithPublicData() throws Exception {
  //   String output =
  //       GroundingWithPublicData.groundWithPublicData(PROJECT_ID, LOCATION, GEMINI_FLASH_1_5);

  //   assertThat(output).ignoringCase().contains("Rayleigh");
  // }

  // @Test
  // public void testGroundingWithPrivateData() throws Exception {
  //   String output =
  //       GroundingWithPrivateData.groundWithPrivateData(
  //           PROJECT_ID,
  //           LOCATION,
  //           GEMINI_FLASH,
  //           String.format(
  //               "projects/%s/locations/global/collections/default_collection/dataStores/%s",
  //               PROJECT_ID, DATASTORE_ID));

  //   assertThat(output).ignoringCase().contains("DMV");
  // }

  @Test
  public void testMultimodalStreaming() throws Exception {
    StreamingMultimodal.streamingMultimodal(PROJECT_ID, LOCATION, GEMINI_FLASH);
    assertThat(bout.toString()).ignoringCase().contains("no");
  }

  @Test
  public void testMultimodalNonStreaming() throws Exception {
    String output = Multimodal.nonStreamingMultimodal(PROJECT_ID, LOCATION, GEMINI_FLASH);

    assertThat(output).ignoringCase().contains("no");
  }

  private class Obj {
    public String object;
  }

  @Test
  public void testControlledGenerationWithJsonSchema6() throws Exception {
    String output = ControlledGenerationSchema6
        .controlGenerationWithJsonSchema6(PROJECT_ID, LOCATION, GEMINI_FLASH);

    Obj[] objects = new Gson().fromJson(output, Obj[].class);
    String recognizedObjects = Arrays.stream(objects)
        .map(obj -> obj.object.toLowerCase())
        .collect(Collectors.joining(" "));

    assertThat(recognizedObjects).isNotEmpty();
    assertThat(recognizedObjects).contains("globe");
    assertThat(recognizedObjects).contains("keyboard");
    assertThat(recognizedObjects).contains("passport");
    assertThat(recognizedObjects).contains("pot");
  }

}
