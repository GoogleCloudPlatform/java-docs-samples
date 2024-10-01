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
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EditImageInpaintingInsertMaskSampleTest {

  private static final String PROJECT = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String INPUT_FILE = "resources/woman.png";
  private static final String MASK_FILE = "resources/woman_inpainting_insert_mask.png";
  private static final String PROMPT = "hat";

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
  public void testEditImageInpaintingInsertMaskSample() throws IOException {
    PredictResponse response =
        EditImageInpaintingInsertMaskSample.editImageInpaintingInsertMask(
            PROJECT, "us-central1", INPUT_FILE, MASK_FILE, PROMPT);
    assertThat(response).isNotNull();

    Boolean imageBytes = false;
    for (Value prediction : response.getPredictionsList()) {
      Map<String, Value> fieldsMap = prediction.getStructValue().getFieldsMap();
      if (fieldsMap.containsKey("bytesBase64Encoded")) {
        imageBytes = true;
        break;
      }
    }
    assertThat(imageBytes).isTrue();
  }
}
