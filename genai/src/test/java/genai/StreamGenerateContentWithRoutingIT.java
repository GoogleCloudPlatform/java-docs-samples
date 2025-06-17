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

// Tests for Gemini code samples.

package genai;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StreamGenerateContentWithRoutingIT {

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

      requireEnvVar("GOOGLE_GENAI_USE_VERTEXAI");
      requireEnvVar("GOOGLE_CLOUD_PROJECT");
      requireEnvVar("GOOGLE_CLOUD_LOCATION");

      stdOut.close();
      System.setOut(out);
    }
  }

  @Test
  public void testStreamGenerateContentWithRouting() throws Exception {
    String textPrompt =
        "What's a good name for a flower shop that specializes in selling bouquets of"
            + " dried flowers?";

    String featureSelectionPreference = "PRIORITIZE_QUALITY";

    String output =
        StreamGenerateContentWithRouting.generateContentStream(
            textPrompt, featureSelectionPreference);
    assertThat(output).isNotEmpty();
  }
}
