/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aiplatform;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class PredictTextClassificationSampleTest {
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  private static final String PROJECT = System.getenv("UCAIP_PROJECT_ID");
  private static final String INSTANCE =
      "{ \"content\": \"What is the topic for a given news headline?\n"
          + "- business\n"
          + "- entertainment\n"
          + "- health\n"
          + "- sports\n"
          + "- technology\n"
          + "\n"
          + "Text: Pixel 7 Pro Expert Hands On Review, the Most Helpful Google Phones.\n"
          + "The answer is: technology\n"
          + "\n"
          + "Text: Quit smoking?\n"
          + "The answer is: health\n"
          + "\n"
          + "Text: Roger Federer reveals why he touched Rafael Nadals hand while they were crying\n"
          + "The answer is: sports\n"
          + "\n"
          + "Text: Business relief from Arizona minimum-wage hike looking more remote\n"
          + "The answer is: business\n"
          + "\n"
          + "Text: #TomCruise has arrived in Bari, Italy for #MissionImpossible.\n"
          + "The answer is: entertainment\n"
          + "\n"
          + "Text: CNBC Reports Rising Digital Profit as Print Advertising Falls\n"
          + "The answer is:\"}";
  private static final String PARAMETERS =
      "{\n"
          + "  \"temperature\": 0,\n"
          + "  \"maxDecodeSteps\": 5,\n"
          + "  \"topP\": 0,\n"
          + "  \"topK\": 1\n"
          + "}";
  private static final String PUBLISHER = "google";
  private static final String MODEL = "text-bison@001";

  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;

  private static void requireEnvVar(String varName) {
    String errorMessage =
        String.format("Environment variable '%s' is required to perform these tests.", varName);
    assertNotNull(errorMessage, System.getenv(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("UCAIP_PROJECT_ID");
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @Test
  public void testPredictTextClassification() throws IOException {
    // Act
    PredictTextClassificationSample.predictTextClassification(
        INSTANCE, PARAMETERS, PROJECT, PUBLISHER, MODEL);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Predict Response");
  }
}
