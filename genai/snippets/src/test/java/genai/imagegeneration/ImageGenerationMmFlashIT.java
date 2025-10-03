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

package genai.imagegeneration;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ImageGenerationMmFlashIT {

  private static final String GEMINI_FLASH_IMAGE = "gemini-2.5-flash-image";
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
  public void testImageGenMmFlashEditImageWithTextAndImage() throws IOException {
    String outputFile = "resources/output/bw-example-image.png";
    ImageGenMmFlashEditImageWithTextAndImage.generateContent(GEMINI_FLASH_IMAGE, outputFile);
    assertThat(bout.toString()).contains("Content written to: " + outputFile);
  }

  @Test
  public void testImageGenMmFlashLocaleAwareWithText() throws IOException {
    String outputFile = "resources/output/example-breakfast-meal.png";
    ImageGenMmFlashLocaleAwareWithText.generateContent(GEMINI_FLASH_IMAGE, outputFile);
    assertThat(bout.toString()).contains("Content written to: " + outputFile);
  }

  @Test
  public void testImageGenMmFlashMultipleImagesWithText() throws IOException {
    List<String> images = ImageGenMmFlashMultipleImagesWithText.generateContent(GEMINI_FLASH_IMAGE);
    assertThat(images).isNotEmpty();
  }

  @Test
  public void testImageGenMmFlashTextAndImageWithText() throws IOException {
    String outputFile = "resources/output/paella-recipe.md";
    ImageGenMmFlashTextAndImageWithText.generateContent(GEMINI_FLASH_IMAGE, outputFile);
    assertThat(bout.toString()).contains("Content written to: " + outputFile);
  }

  @Test
  public void testImageGenMmFlashWithText() throws IOException {
    String outputFile = "resources/output/example-image-eiffel-tower.png";
    ImageGenMmFlashWithText.generateContent(GEMINI_FLASH_IMAGE, outputFile);
    assertThat(bout.toString()).contains("Content written to: " + outputFile);
  }
}
