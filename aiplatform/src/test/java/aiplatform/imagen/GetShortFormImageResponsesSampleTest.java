/*
 * Copyright 2024 Google LLC
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

package aiplatform.imagen;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.protobuf.Value;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GetShortFormImageResponsesSampleTest {

  private static final String PROJECT = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String INPUT_FILE = "resources/cat.png";
  private static final String PROMPT = "What breed of cat is this a picture of?";

  private static void requireEnvVar(String varName) {
    String errorMessage =
        String.format("Environment variable '%s' is required to perform these tests.", varName);
    assertNotNull(errorMessage, System.getenv(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Test
  public void testGetShortFormImageResponsesSample() throws IOException {
    PredictResponse response =
        GetShortFormImageResponsesSample.getShortFormImageResponses(
            PROJECT, "us-central1", INPUT_FILE, PROMPT);
    assertThat(response).isNotNull();

    for (Value prediction : response.getPredictionsList()) {
      assertThat(prediction.getStringValue().contains("tabby"));
    }
  }
}
