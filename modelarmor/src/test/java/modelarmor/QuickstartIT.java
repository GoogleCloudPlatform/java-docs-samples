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

import com.google.cloud.modelarmor.v1.DeleteTemplateRequest;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.ModelArmorSettings;
import com.google.cloud.modelarmor.v1.TemplateName;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class QuickstartIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION_ID = "us-central1"; // Or your preferred region
  private static final String TEMPLATE_ID = "java-quickstart-" + UUID.randomUUID().toString();

  private static String requireEnvVar(String varName) {
    String value = System.getenv(varName);
    assertNotNull(
            "Environment variable " + varName + " is required to perform these tests.",
            System.getenv(varName));
    return value;
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
    requireEnvVar("GOOGLE_CLOUD_PROJECT_LOCATION");
  }


  @AfterClass
  public static void afterAll() throws Exception {
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", LOCATION_ID);

    ModelArmorSettings.Builder builder = ModelArmorSettings.newBuilder();
    ModelArmorSettings modelArmorSettings = builder.setEndpoint(apiEndpoint).build();
    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
      // Delete the template created by quickstart.
      String templateName = TemplateName.of(PROJECT_ID, LOCATION_ID, TEMPLATE_ID).toString();

      client.deleteTemplate(
          DeleteTemplateRequest.newBuilder()
              .setName(templateName)
              .build());
    }
  }

  @Test
  public void quickstart_test() throws Exception {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream redirected = new ByteArrayOutputStream();

    System.setOut(new PrintStream(redirected));

    try {
      new Quickstart().quickstart(PROJECT_ID, LOCATION_ID, TEMPLATE_ID);
      assertThat(redirected.toString()).contains("Result for User Prompt Sanitization");
      assertThat(redirected.toString()).contains("Result for Model Response Sanitization");
    } finally {
      System.setOut(originalOut);
    }
  }
}
