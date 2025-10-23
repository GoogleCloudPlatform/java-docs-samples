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

package genai.tools;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
public class ToolsIT {

  private static final String GEMINI_FLASH = "gemini-2.5-flash";
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
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
  public void testGenerateContentWithFunctionDescription() {

    String prompt =
        "At Stellar Sounds, a music label, 2024 was a rollercoaster. \"Echoes of the Night,\""
            + " a debut synth-pop album, \n surprisingly sold 350,000 copies, while veteran"
            + " rock band \"Crimson Tide's\" latest, \"Reckless Hearts,\" \n lagged at"
            + " 120,000. Their up-and-coming indie artist, \"Luna Bloom's\" EP, \"Whispers "
            + "of Dawn,\" \n secured 75,000 sales. The biggest disappointment was the "
            + "highly-anticipated rap album \"Street Symphony\" \n only reaching 100,000"
            + " units. Overall, Stellar Sounds moved over 645,000 units this year, revealing"
            + " unexpected \n trends in music consumption.";

    String response = ToolFunctionDescriptionWithText.generateContent(GEMINI_FLASH, prompt);

    assertThat(response).isNotEmpty();
    assertThat(response).contains("get_album_sales");
    assertThat(response).contains("copies_sold=350000");
    assertThat(response).contains("album_name=Echoes of the Night");
  }

  @Test
  public void testToolsCodeExecWithText() {
    String response = ToolsCodeExecWithText.generateContent(GEMINI_FLASH);
    assertThat(response).isNotEmpty();
    assertThat(bout.toString()).contains("Code:");
    assertThat(bout.toString()).contains("Outcome:");
  }

  @Test
  public void testToolsCodeExecWithTextLocalImage() throws IOException {
    String response = ToolsCodeExecWithTextLocalImage.generateContent(GEMINI_FLASH);
    assertThat(response).isNotEmpty();
    assertThat(bout.toString()).contains("Code:");
    assertThat(bout.toString()).contains("Outcome:");
  }

  @Test
  public void testToolsGoogleMapsCoordinatesWithTxt() {
    String response = ToolsGoogleMapsCoordinatesWithTxt.generateContent(GEMINI_FLASH);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testToolsGoogleSearchAndUrlContextWithTxt() {
    String url = "https://www.google.com/search?q=events+in+New+York";
    String response = ToolsGoogleSearchAndUrlContextWithTxt.generateContent(GEMINI_FLASH, url);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testToolsGoogleSearchWithText() {
    String response = ToolsGoogleSearchWithText.generateContent(GEMINI_FLASH);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testToolsUrlContextWithTxt() {
    String url1 = "https://cloud.google.com/vertex-ai/generative-ai/docs";
    String url2 = "https://cloud.google.com/docs/overview";
    String response = ToolsUrlContextWithTxt.generateContent(GEMINI_FLASH, url1, url2);
    assertThat(response).isNotEmpty();
    String output = bout.toString();
    assertThat(output).contains("UrlContextMetadata");
    assertThat(output).contains("urlRetrievalStatus");
    assertThat(output).contains("URL_RETRIEVAL_STATUS_SUCCESS");
    assertThat(output).contains(url1);
    assertThat(output).contains(url2);
  }

  @Test
  public void testToolsVaisWithText() throws NoSuchFieldException, IllegalAccessException {
    String response =
        "The process for making an appointment to renew your driver's license"
            + " varies depending on your location.";

    String datastore =
        String.format(
            "projects/%s/locations/global/collections/default_collection/"
                + "dataStores/grounding-test-datastore",
            PROJECT_ID);

    Client.Builder mockedBuilder = mock(Client.Builder.class, RETURNS_SELF);
    Client mockedClient = mock(Client.class);
    Models mockedModels = mock(Models.class);
    GenerateContentResponse mockedResponse = mock(GenerateContentResponse.class);

    try (MockedStatic<Client> mockedStatic = mockStatic(Client.class)) {
      mockedStatic.when(Client::builder).thenReturn(mockedBuilder);
      when(mockedBuilder.build()).thenReturn(mockedClient);

      // Using reflection because 'models' is a final field and cannot be mockable directly
      Field field = Client.class.getDeclaredField("models");
      field.setAccessible(true);
      field.set(mockedClient, mockedModels);

      when(mockedClient.models.generateContent(
              anyString(), anyString(), any(GenerateContentConfig.class)))
          .thenReturn(mockedResponse);
      when(mockedResponse.text()).thenReturn(response);

      String generatedResponse = ToolsVaisWithText.generateContent(GEMINI_FLASH, datastore);

      verify(mockedClient.models, times(1))
          .generateContent(anyString(), anyString(), any(GenerateContentConfig.class));
      assertThat(generatedResponse).isNotEmpty();
      assertThat(response).isEqualTo(generatedResponse);
    }
  }
}
