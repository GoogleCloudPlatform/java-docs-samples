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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.modelarmor.v1.Template;
import com.google.cloud.modelarmor.v1.TemplateName;
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

/** Integration (system) tests for {@link Snippets}. */
@RunWith(JUnit4.class)
public class SnippetsIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION_ID = System.getenv()
      .getOrDefault("GOOGLE_CLOUD_PROJECT_LOCATION", "us-central1");
  private static String TEST_TEMPLATE_ID;
  private static String TEST_TEMPLATE_NAME;
  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  private static String requireEnvVar(String varName) {
    String value = System.getenv(varName);
    assertNotNull("Environment variable " + varName + " is required to run these tests.",
        System.getenv(varName));
    return value;
  }

  @BeforeClass
  public static void beforeAll() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @AfterClass
  public static void afterAll() throws IOException {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
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
  public void testGetModelArmorTemplate() throws IOException {
    CreateTemplate.createTemplate(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID);
    Template retrievedTemplate = GetTemplate.getTemplate(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID);

    assertThat(stdOut.toString()).contains("Retrieved template:");
    assertEquals(retrievedTemplate.getName(), TEST_TEMPLATE_NAME);
  }

  @Test
  public void testListModelArmorTemplates() throws IOException {
    CreateTemplate.createTemplate(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID);

    ListTemplates.listTemplates(PROJECT_ID, LOCATION_ID);

    boolean templatePresentInList = false;
    for (Template template : ListTemplates.listTemplates(PROJECT_ID, LOCATION_ID).iterateAll()) {
      if (TEST_TEMPLATE_NAME.equals(template.getName())) {
        templatePresentInList = true;
      }
    }
    assertTrue(templatePresentInList);
  }

  @Test
  public void testListTemplatesWithFilter() throws IOException {
    CreateTemplate.createTemplate(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID);
    String filter = "name=\"projects/" + PROJECT_ID + "/locations/" + LOCATION_ID + "/"
        + TEST_TEMPLATE_ID + "\"";

    ListTemplatesWithFilter.listTemplatesWithFilter(PROJECT_ID, LOCATION_ID, filter);

    boolean templatePresentInList = false;
    for (Template template : ListTemplates.listTemplates(PROJECT_ID, LOCATION_ID).iterateAll()) {
      if (TEST_TEMPLATE_NAME.equals(template.getName())) {
        templatePresentInList = true;
      }
    }
    assertTrue(templatePresentInList);
  }
}
