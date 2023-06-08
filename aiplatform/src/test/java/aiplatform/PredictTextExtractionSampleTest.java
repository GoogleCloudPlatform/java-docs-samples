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

public class PredictTextExtractionSampleTest {
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  private static final String PROJECT = System.getenv("UCAIP_PROJECT_ID");
  private static final String INSTANCE =
      "{\"content\": \"Background: There is evidence that there have been significant changes \n"
          + "in Amazon rainforest vegetation over the last 21,000 years through the Last \n"
          + "Glacial Maximum (LGM) and subsequent deglaciation. Analyses of sediment \n"
          + "deposits from Amazon basin paleo lakes and from the Amazon Fan indicate that \n"
          + "rainfall in the basin during the LGM was lower than for the present, and this \n"
          + "was almost certainly associated with reduced moist tropical vegetation cover \n"
          + "in the basin. There is debate, however, over how extensive this reduction \n"
          + "was. Some scientists argue that the rainforest was reduced to small, isolated \n"
          + "refugia separated by open forest and grassland; other scientists argue that \n"
          + "the rainforest remained largely intact but extended less far to the north, \n"
          + "south, and east than is seen today. This debate has proved difficult to \n"
          + "resolve because the practical limitations of working in the rainforest mean \n"
          + "that data sampling is biased away from the center of the Amazon basin, and \n"
          + "both explanations are reasonably well supported by the available data.\n"
          + "\n"
          + "Q: What does LGM stands for?\n"
          + "A: Last Glacial Maximum.\n"
          + "\n"
          + "Q: What did the analysis from the sediment deposits indicate?\n"
          + "A: Rainfall in the basin during the LGM was lower than for the present.\n"
          + "\n"
          + "Q: What are some of scientists arguments?\n"
          + "A: The rainforest was reduced to small, isolated refugia separated by open forest and"
          + " grassland.\n"
          + "\n"
          + "Q: There have been major changes in Amazon rainforest vegetation over the last how"
          + " many years?\n"
          + "A: 21,000.\n"
          + "\n"
          + "Q: What caused changes in the Amazon rainforest vegetation?\n"
          + "A: The Last Glacial Maximum (LGM) and subsequent deglaciation\n"
          + "\n"
          + "Q: What has been analyzed to compare Amazon rainfall in the past and present?\n"
          + "A: Sediment deposits.\n"
          + "\n"
          + "Q: What has the lower rainfall in the Amazon during the LGM been attributed to?\n"
          + "A:\"}";
  private static final String PARAMETERS =
      "{\n"
          + "  \"temperature\": 0,\n"
          + "  \"maxDecodeSteps\": 32,\n"
          + "  \"topP\": 0,\n"
          + "  \"topK\": 1\n"
          + "}";
  private static final String PUBLISHER = "google";
  private static final String LOCATION = "us-central1";
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
  public void testPredictTextExtraction() throws IOException {
    // Act
    PredictTextExtractionSample.predictTextExtraction(
        INSTANCE, PARAMETERS, PROJECT, LOCATION, PUBLISHER, MODEL);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Predict Response");
  }
}
