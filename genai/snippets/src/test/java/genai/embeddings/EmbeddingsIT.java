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

package genai.embeddings;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.genai.types.EmbedContentResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EmbeddingsIT {

  private static final String GEMINI_EMBEDDING = "gemini-embedding-001";
  private ByteArrayOutputStream bout;

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
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testEmbeddingsDocRetrievalWithTxt() {
    EmbedContentResponse response = EmbeddingsDocRetrievalWithTxt.embedContent(GEMINI_EMBEDDING);
    assertThat(response.toString()).isNotEmpty();
    assertThat(response.embeddings()).isPresent();
    assertThat(response.embeddings().get()).isNotEmpty();
    assertThat(response.metadata()).isPresent();

    String output = bout.toString();
    assertThat(output).contains("statistics");
    assertThat(output).contains("tokenCount");
  }
}
