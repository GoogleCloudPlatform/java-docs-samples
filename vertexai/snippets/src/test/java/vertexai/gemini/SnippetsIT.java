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
  private static final String GEMINI_FLASH = "gemini-1.5-flash-001";
  private static final String GEMINI_PRO = "gemini-1.5-pro-001";
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
  public void testChatSession() throws IOException {
    ChatDiscussion.chatDiscussion(PROJECT_ID, LOCATION, GEMINI_FLASH);
    assertThat(bout.toString()).contains("Chat Ended.");
  }

  @Test
  public void testMultimodalMultiImage() throws IOException {
    MultimodalMultiImage.multimodalMultiImage(PROJECT_ID, LOCATION, GEMINI_FLASH);
    assertThat(bout.toString()).contains("city: Rio de Janeiro, Landmark: Christ the Redeemer");
  }

  @Test
  public void testMultimodalQuery() throws Exception {
    String imageUri =
        "https://storage.googleapis.com/cloud-samples-data/vertex-ai/llm/prompts/landmark1.png";
    String dataImageBase64 = Base64.getEncoder().encodeToString(readImageFile(imageUri));
    String output =
        MultimodalQuery.multimodalQuery(PROJECT_ID, LOCATION, GEMINI_FLASH, dataImageBase64);
    assertThat(output).isNotEmpty();
  }

  @Test
  public void testMultimodalVideoInput() throws IOException {
    MultimodalVideoInput.multimodalVideoInput(PROJECT_ID, LOCATION, GEMINI_FLASH);
    assertThat(bout.toString()).contains("Zoo");
  }

  @Test
  public void testMultiTurnMultimodal() throws IOException {
    MultiTurnMultimodal.multiTurnMultimodal(PROJECT_ID, LOCATION, GEMINI_FLASH);
    assertThat(bout.toString()).contains("scones");
  }

  @Test
  public void testSimpleQuestionAnswer() throws Exception {
    String output = QuestionAnswer.simpleQuestion(PROJECT_ID, LOCATION, GEMINI_FLASH);
    assertThat(output).isNotEmpty();
    assertThat(output).contains("Rayleigh scattering");
  }

  @Test
  public void testQuickstart() throws IOException {
    String output = Quickstart.quickstart(PROJECT_ID, LOCATION, GEMINI_FLASH);
    assertThat(output).contains("scones");
  }

  @Test
  public void testSingleTurnMultimodal() throws IOException {
    String imageUri =
        "https://storage.googleapis.com/cloud-samples-data/vertex-ai/llm/prompts/landmark1.png";
    String dataImageBase64 = Base64.getEncoder().encodeToString(readImageFile(imageUri));
    SingleTurnMultimodal.generateContent(
        PROJECT_ID, LOCATION, GEMINI_FLASH, "What is this image", dataImageBase64);
    assertThat(bout.toString()).contains("Colosseum");
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
  public void testSafetySettings() throws Exception {
    String textPrompt = "Hello World!";

    String output = WithSafetySettings.safetyCheck(PROJECT_ID, LOCATION, GEMINI_FLASH, textPrompt);
    assertThat(output).isNotEmpty();
    assertThat(output).contains("reasons?");
  }

  @Test
  public void testTokenCount() throws Exception {
    int tokenCount = GetTokenCount.getTokenCount(PROJECT_ID, LOCATION, GEMINI_FLASH);
    assertThat(tokenCount).isEqualTo(6);
  }

  @Test
  public void testMediaTokenCount() throws Exception {
    int tokenCount = GetMediaTokenCount.getMediaTokenCount(PROJECT_ID, LOCATION, GEMINI_FLASH);
    assertThat(tokenCount).isEqualTo(16822);
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
  public void testComplexFunctionCalling() throws Exception {
    String textPrompt = "What is the weather like in Boston?";

    String answer =
        ComplexFunctionCalling.complexFunctionCalling(
            PROJECT_ID, LOCATION, GEMINI_FLASH, textPrompt);
    assertThat(answer).ignoringCase().contains("Boston");
    assertThat(answer).ignoringCase().contains("Partly Cloudy");
    assertThat(answer).ignoringCase().contains("temperature");
    assertThat(answer).ignoringCase().contains("65");
  }
  
  @Test
  public void testAutomaticFunctionCalling() throws Exception {
    String textPrompt = "What's the weather in Paris?";

    String answer =
        AutomaticFunctionCalling.automaticFunctionCalling(
            PROJECT_ID, LOCATION, GEMINI_FLASH, textPrompt);
    assertThat(answer).ignoringCase().contains("raining");
  }

  @Test
  public void testAudioInputSummary() throws IOException {
    String output = AudioInputSummarization.summarizeAudio(PROJECT_ID, LOCATION, GEMINI_FLASH);

    assertThat(output).ignoringCase().contains("Pixel");
    assertThat(output).ignoringCase().contains("feature");
  }

  @Test
  public void testAudioInputTranscription() throws IOException {
    String output = AudioInputTranscription.transcribeAudio(PROJECT_ID, LOCATION, GEMINI_FLASH);

    assertThat(output).ignoringCase().contains("Pixel");
    assertThat(output).ignoringCase().contains("feature");
  }

  @Test
  public void testVideoAudioInput() throws IOException {
    String output = VideoInputWithAudio.videoAudioInput(PROJECT_ID, LOCATION, GEMINI_FLASH);

    assertThat(output).ignoringCase().contains("Pixel");
    assertThat(output).ignoringCase().contains("Tokyo");
  }

  @Test
  public void testAllModalityInputs() throws IOException {
    String output = MultimodalAllInput.multimodalAllInput(PROJECT_ID, LOCATION, GEMINI_FLASH);

    assertThat(output).ignoringCase().contains("0:4");
  }

  @Test
  public void testPdfInput() throws IOException {
    String output = PdfInput.pdfInput(PROJECT_ID, LOCATION, GEMINI_FLASH);

    assertThat(output).ignoringCase().contains("Gemini");
  }

  @Test
  public void testSystemInstruction() throws Exception {
    String output = WithSystemInstruction.translateToFrench(PROJECT_ID, LOCATION, GEMINI_FLASH);

    assertThat(output).ignoringCase().contains("bagels");
    assertThat(output).ignoringCase().contains("aime");
  }

  @Test
  public void testGroundingWithPublicData() throws Exception {
    String output =
        GroundingWithPublicData.groundWithPublicData(PROJECT_ID, LOCATION, GEMINI_FLASH);

    assertThat(output).ignoringCase().contains("Rayleigh");
  }

  @Test
  public void testGroundingWithPrivateData() throws Exception {
    String output =
        GroundingWithPrivateData.groundWithPrivateData(
            PROJECT_ID,
            LOCATION,
            GEMINI_FLASH,
            String.format(
                "projects/%s/locations/global/collections/default_collection/dataStores/%s",
                PROJECT_ID, DATASTORE_ID));

    assertThat(output).ignoringCase().contains("DMV");
  }

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

  private class Recipe {
    @SerializedName("recipe_name")
    public String recipeName;
  }

  @Test
  public void testControlledGenerationWithMimeType() throws Exception {
    String output = ControlledGenerationMimeType
        .controlGenerationWithMimeType(PROJECT_ID, LOCATION, GEMINI_FLASH);
    Recipe[] recipes = new Gson().fromJson(output, Recipe[].class);

    assertThat(recipes).isNotEmpty();
    assertThat(recipes[0].recipeName).isNotEmpty();
  }

  @Test
  public void testControlledGenerationWithJsonSchema() throws Exception {
    String output = ControlledGenerationSchema
        .controlGenerationWithJsonSchema(PROJECT_ID, LOCATION, GEMINI_PRO);
    Recipe[] recipes = new Gson().fromJson(output, Recipe[].class);

    assertThat(recipes).isNotEmpty();
    assertThat(recipes[0].recipeName).isNotEmpty();
  }

  private class Review {
    public int rating;
    public String flavor;
  }

  @Test
  public void testControlledGenerationWithJsonSchema2() throws Exception {
    String output = ControlledGenerationSchema2
        .controlGenerationWithJsonSchema2(PROJECT_ID, LOCATION, GEMINI_PRO);
    Review[] recipes = new Gson().fromJson(output, Review[].class);

    assertThat(recipes).hasLength(2);
    assertThat(recipes[0].flavor).isNotEmpty();
    assertThat(recipes[0].rating).isEqualTo(4);
    assertThat(recipes[1].flavor).isNotEmpty();
    assertThat(recipes[1].rating).isEqualTo(1);
  }

  private class WeatherForecast {
    public DayForecast[] forecast;
  }

  private class DayForecast {
    @SerializedName("Day")
    public String day;
    @SerializedName("Forecast")
    public String forecast;
    @SerializedName("Humidity")
    public String humidity;
    @SerializedName("Temperature")
    public int temperature;
    @SerializedName("Wind Speed")
    public int windSpeed;
  }

  @Test
  public void testControlledGenerationWithJsonSchema3() throws Exception {
    String output = ControlledGenerationSchema3
        .controlGenerationWithJsonSchema3(PROJECT_ID, LOCATION, GEMINI_PRO);
    WeatherForecast weatherForecast = new Gson().fromJson(output, WeatherForecast.class);

    assertThat(weatherForecast.forecast).hasLength(7);

    assertThat(weatherForecast.forecast[0].day).ignoringCase().isEqualTo("Sunday");
    assertThat(weatherForecast.forecast[0].forecast).ignoringCase().isEqualTo("Sunny");
    assertThat(weatherForecast.forecast[0].temperature).isEqualTo(77);
    assertThat(weatherForecast.forecast[0].humidity).ignoringCase().isEqualTo("50%");
    assertThat(weatherForecast.forecast[0].windSpeed).isEqualTo(10);

    assertThat(weatherForecast.forecast[1].day).ignoringCase().isEqualTo("Monday");
    assertThat(weatherForecast.forecast[1].forecast).ignoringCase().isEqualTo("Partly Cloudy");
    assertThat(weatherForecast.forecast[1].temperature).isEqualTo(72);
    assertThat(weatherForecast.forecast[1].humidity).ignoringCase().isEqualTo("55%");
    assertThat(weatherForecast.forecast[1].windSpeed).isEqualTo(15);

    assertThat(weatherForecast.forecast[2].day).ignoringCase().isEqualTo("Tuesday");
    assertThat(weatherForecast.forecast[2].forecast).ignoringCase().contains("Rain");
    assertThat(weatherForecast.forecast[2].temperature).isEqualTo(64);
    assertThat(weatherForecast.forecast[2].humidity).ignoringCase().isEqualTo("70%");
    assertThat(weatherForecast.forecast[2].windSpeed).isEqualTo(20);

    assertThat(weatherForecast.forecast[3].day).ignoringCase().isEqualTo("Wednesday");
    assertThat(weatherForecast.forecast[3].forecast).ignoringCase().contains("Thunder");
    assertThat(weatherForecast.forecast[3].temperature).isEqualTo(68);
    assertThat(weatherForecast.forecast[3].humidity).ignoringCase().isEqualTo("75%");
    assertThat(weatherForecast.forecast[3].windSpeed).isEqualTo(25);

    assertThat(weatherForecast.forecast[4].day).ignoringCase().isEqualTo("Thursday");
    assertThat(weatherForecast.forecast[4].forecast).ignoringCase().isEqualTo("Cloudy");
    assertThat(weatherForecast.forecast[4].temperature).isEqualTo(66);
    assertThat(weatherForecast.forecast[4].humidity).ignoringCase().isEqualTo("60%");
    assertThat(weatherForecast.forecast[4].windSpeed).isEqualTo(18);

    assertThat(weatherForecast.forecast[5].day).ignoringCase().isEqualTo("Friday");
    assertThat(weatherForecast.forecast[5].forecast).ignoringCase().isEqualTo("Partly Cloudy");
    assertThat(weatherForecast.forecast[5].temperature).isEqualTo(73);
    assertThat(weatherForecast.forecast[5].humidity).ignoringCase().isEqualTo("45%");
    assertThat(weatherForecast.forecast[5].windSpeed).isEqualTo(12);

    assertThat(weatherForecast.forecast[6].day).ignoringCase().isEqualTo("Saturday");
    assertThat(weatherForecast.forecast[6].forecast).ignoringCase().isEqualTo("Sunny");
    assertThat(weatherForecast.forecast[6].temperature).isEqualTo(80);
    assertThat(weatherForecast.forecast[6].humidity).ignoringCase().isEqualTo("40%");
    assertThat(weatherForecast.forecast[6].windSpeed).isEqualTo(8);
  }

  private class Item {
    @SerializedName("to_discard")
    public Integer toDiscard;
    public String subcategory;
    @SerializedName("safe_handling")
    public Integer safeHandling;
    @SerializedName("item_category")
    public String itemCategory;
    @SerializedName("for_resale")
    public Integer forResale;
    public String condition;
  }

  @Test
  public void testControlledGenerationWithJsonSchema4() throws Exception {
    String output = ControlledGenerationSchema4
        .controlGenerationWithJsonSchema4(PROJECT_ID, LOCATION, GEMINI_PRO);
    Item[] items = new Gson().fromJson(output, Item[].class);

    assertThat(items).isNotEmpty();
  }

  private class Obj {
    public String object;
  }

  @Test
  public void testControlledGenerationWithJsonSchema6() throws Exception {
    String output = ControlledGenerationSchema6
        .controlGenerationWithJsonSchema6(PROJECT_ID, LOCATION, GEMINI_PRO);

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

  @Test
  public void testGeminiTranslate() throws Exception {
    String output = GeminiTranslate.geminiTranslate(
        PROJECT_ID, LOCATION, GEMINI_PRO, TEXT_TO_TRANSLATE, TARGET_LANGUAGE_CODE);

    assertThat(output).ignoringCase().contains("Bonjour");
    assertThat(output).ignoringCase().contains("aujourd'hui");
  }
}
