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

package modelarmor;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.api.gax.rpc.NotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SnippetsIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION_ID = System.getenv()
      .getOrDefault("GOOGLE_CLOUD_PROJECT_LOCATION", "us-central1");
  private static String TEST_TEMPLATE_ID;

  private ByteArrayOutputStream stdOut;

  private static String requireEnvVar(String varName) {
    String value = System.getenv(varName);
    assertNotNull("Environment variable " + varName + " is required to perform these tests.",
        System.getenv(varName));
    return value;
  }

  @BeforeClass
  public static void beforeAll() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
    requireEnvVar("GOOGLE_CLOUD_PROJECT_LOCATION");
  }

  @AfterClass
  public static void afterAll() throws IOException {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
    requireEnvVar("GOOGLE_CLOUD_PROJECT_LOCATION");
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    TEST_TEMPLATE_ID = randomId();
  }

  @After
  public void afterEach() throws IOException {
    try {
      DeleteTemplate.deleteTemplate(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID);
    } catch (NotFoundException e) {
      // Ignore not found error - template already deleted.
    }

    stdOut = null;
    System.setOut(null);
  }

  private static String randomId() {
    Random random = new Random();
    return "java-ma-" + random.nextLong();
  }

  @Test
  public void testSanitizeUserPrompt() throws IOException {
    CreateTemplate.createTemplate(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID);
    String userPrompt = "Unsafe user prompt";

    SanitizeUserPrompt.sanitizeUserPrompt(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID, userPrompt);

    assertThat(stdOut.toString()).contains("Result for the provided user prompt:");
  }

  @Test
  public void testSanitizeModelResponse() throws IOException {
    CreateTemplate.createTemplate(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID);
    String modelResponse = "Unsanitized model output";

    SanitizeModelResponse.sanitizeModelResponse(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID,
        modelResponse);

    assertThat(stdOut.toString()).contains("Result for the provided model response:");
  }

  @Test
  public void testScreenPdfFile() throws IOException {
    CreateTemplate.createTemplate(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID);
    String pdfFilePath = "src/main/resources/test_sample.pdf";

    ScreenPdfFile.screenPdfFile(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID, pdfFilePath);

    assertThat(stdOut.toString()).contains("Result for the provided PDF file:");
  }
}
