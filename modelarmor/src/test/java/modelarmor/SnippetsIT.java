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
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.modelarmor.v1.SdpBasicConfig.SdpBasicConfigEnforcement;
import com.google.cloud.modelarmor.v1.Template;
import com.google.cloud.modelarmor.v1.TemplateName;
import com.google.common.base.Strings;

/** Integration (system) tests for {@link Snippets}. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class SnippetsIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION_ID = System.getenv().getOrDefault("GOOGLE_CLOUD_PROJECT_LOCATION",
      "us-central1");
  private static String TEST_TEMPLATE_ID;
  private static String TEST_TEMPLATE_NAME;
  private ByteArrayOutputStream stdOut;

  @BeforeClass
  public static void beforeAll() throws IOException {
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT_LOCATION", Strings.isNullOrEmpty(LOCATION_ID));
  }

  @AfterClass
  public static void afterAll() throws IOException {
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT_LOCATION", Strings.isNullOrEmpty(LOCATION_ID));
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    TEST_TEMPLATE_ID = randomId();
    TEST_TEMPLATE_NAME = TemplateName.of(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID).toString();
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
  public void testCreateModelArmorTemplate() throws IOException {
    Template createdTemplate = CreateTemplate.createTemplate(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID);

    assertThat(stdOut.toString()).contains("Created template:");
    assertEquals(createdTemplate.getName(), TEST_TEMPLATE_NAME);
  }

  @Test
  public void testCreateModelArmorTemplateWithBasicSDP() throws IOException {
    Template createdTemplate = CreateTemplateWithBasicSdp.createTemplateWithBasicSdp(PROJECT_ID, LOCATION_ID,
        TEST_TEMPLATE_ID);

    assertThat(stdOut.toString()).contains("Created template with basic SDP filter:");
    assertEquals(createdTemplate.getName(), TEST_TEMPLATE_NAME);
    assertEquals(SdpBasicConfigEnforcement.ENABLED,
        createdTemplate.getFilterConfig().getSdpSettings().getBasicConfig().getFilterEnforcement());
  }

  @Test
  public void testCreateModelArmorTemplateWithLabels() throws IOException {
    Template createdTemplate = CreateTemplateWithLabels.createTemplateWithLabels(PROJECT_ID, LOCATION_ID,
        TEST_TEMPLATE_ID);

    assertThat(stdOut.toString()).contains("Created template with labels:");
    assertEquals(createdTemplate.getName(), TEST_TEMPLATE_NAME);
  }

  @Test
  public void testCreateModelArmorTemplateWithMetadata() throws IOException {
    Template createdTemplate = CreateTemplateWithMetadata.createTemplateWithMetadata(PROJECT_ID, LOCATION_ID,
        TEST_TEMPLATE_ID);

    assertThat(stdOut.toString()).contains("Created template with metadata:");
    assertEquals(createdTemplate.getName(), TEST_TEMPLATE_NAME);
    assertEquals(true, createdTemplate.getTemplateMetadata().getIgnorePartialInvocationFailures());
    assertEquals(true, createdTemplate.getTemplateMetadata().getLogSanitizeOperations());
    assertEquals(500, createdTemplate.getTemplateMetadata().getCustomPromptSafetyErrorCode());
  }

  @Test
  public void testDeleteModelArmorTemplate() throws IOException {
    CreateTemplate.createTemplate(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID);
    DeleteTemplate.deleteTemplate(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID);

    assertThat(stdOut.toString()).contains("Deleted template:");
  }
}
