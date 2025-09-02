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

package genai.textgeneration;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TextGenerationIT {

  private static final String GEMINI_FLASH = "gemini-2.5-flash";
  private static final String LOCAL_IMG_1 = "resources/latte.jpg";
  private static final String LOCAL_IMG_2 = "resources/scones.jpg";

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
  }

  @Test
  public void testTextGenerationWithTextStream() {
    String prompt = "Why is the sky blue?";
    String response = TextGenerationWithTextStream.generateContent(GEMINI_FLASH, prompt);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testTextGenerationWithSystemInstruction() {
    String response = TextGenerationWithSystemInstruction.generateContent(GEMINI_FLASH);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testTextGenerationWithText() {
    String response = TextGenerationWithText.generateContent(GEMINI_FLASH);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testTextGenerationWithTextAndImage() {
    String response = TextGenerationWithTextAndImage.generateContent(GEMINI_FLASH);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testTextGenerationWithVideo() {

    String prompt =
        " Analyze the provided video file, including its audio.\n"
            + " Summarize the main points of the video concisely.\n"
            + " Create a chapter breakdown with timestamps for key sections or topics discussed.";

    String response = TextGenerationWithVideo.generateContent(GEMINI_FLASH, prompt);
    assertThat(response).isNotEmpty();
    assertThat(response).ignoringCase().contains("Tokyo");
    assertThat(response).ignoringCase().contains("Pixel");
  }

  @Test
  public void testTextGenerationWithMultiImage() throws IOException {

    String gcsFileImagePath = "gs://cloud-samples-data/generative-ai/image/scones.jpg";

    String response =
        TextGenerationWithMultiImage.generateContent(
            GEMINI_FLASH, gcsFileImagePath, LOCAL_IMG_1);

    assertThat(response).isNotEmpty();
  }

  @Test
  public void testTextGenerationAsyncWithText() {
    String response = TextGenerationAsyncWithText.generateContent(GEMINI_FLASH);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testTextGenerationWithMultiLocalImage() throws IOException {
    String response =
            TextGenerationWithMultiLocalImage.generateContent(
                    GEMINI_FLASH, LOCAL_IMG_1, LOCAL_IMG_2);

    assertThat(response).isNotEmpty();
  }

  @Test
  public void testTextGenerationWithMuteVideo() {
    String response = TextGenerationWithMuteVideo.generateContent(GEMINI_FLASH);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testTextGenerationWithPdf() {
    String response = TextGenerationWithPdf.generateContent(GEMINI_FLASH);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testTextGenerationWithYoutubeVideo() {
    String response = TextGenerationWithYoutubeVideo.generateContent(GEMINI_FLASH);
    assertThat(response).isNotEmpty();
  }
  
  @Test
  public void testTextGenerationWithRouting() throws IOException {
    String textPrompt =
        "What's a good name for a flower shop that specializes in selling bouquets of"
            + " dried flowers?";
    String featureSelectionPreference = "PRIORITIZE_COST";
    String response =
        TextGenerationWithRouting.generateContent(textPrompt, featureSelectionPreference);
    assertThat(response).isNotEmpty();
  }
  
  @Test
  public void testTextGenerationWithRoutingAndTextStream() throws IOException {
    String textPrompt =
        "What's a good name for a flower shop that specializes in selling bouquets of"
            + " dried flowers?";
    String featureSelectionPreference = "PRIORITIZE_COST";

    String response =
        TextGenerationWithRoutingAndTextStream.generateContent(
          textPrompt, featureSelectionPreference);
    assertThat(response).isNotEmpty();
  } 
}
