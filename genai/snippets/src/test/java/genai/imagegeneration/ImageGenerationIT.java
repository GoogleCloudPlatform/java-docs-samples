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

import com.google.genai.types.Image;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ImageGenerationIT {

  private static final String IMAGGEN_4_MODEL = "imagen-4.0-generate-001";
  private static final String VIRTUAL_TRY_ON_MODEL = "virtual-try-on-preview-08-04";

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
  public void testImageGenVirtualTryOnWithTextAndImage() throws IOException {
    Image image =
        ImageGenVirtualTryOnWithTextAndImage.generateContent(
            VIRTUAL_TRY_ON_MODEL, "resources/output/man_in_sweater.png");

    assertThat(image).isNotNull();
    assertThat(image.imageBytes()).isPresent();
    assertThat(image.imageBytes().get().length).isGreaterThan(0);
  }

  @Test
  public void testImageGenWithText() throws IOException {
    Image image =
        ImageGenWithText.generateImage(IMAGGEN_4_MODEL, "resources/output/dog_newspaper.png");

    assertThat(image).isNotNull();
    assertThat(image.imageBytes()).isPresent();
    assertThat(image.imageBytes().get().length).isGreaterThan(0);
  }
}