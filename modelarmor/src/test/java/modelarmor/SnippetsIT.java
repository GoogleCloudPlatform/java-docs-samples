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

import com.google.cloud.modelarmor.v1.Template;
import com.google.common.base.Strings;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration (system) tests for {@link Snippets}. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class SnippetsIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION = "us-central1";
  private static final String MA_REGIONAL_ENDPOINT =
      String.format("modelarmor.%s.rep.googleapis.com:443", LOCATION);
  private static final String DLP_REGIONAL_ENDPOINT =
      String.format("dlp.%s.rep.googleapis.com:443", LOCATION);
  private static final String INSPECT_TEMPLATE_ID =
      "model-armour-inspect-template-" + UUID.randomUUID().toString();
  private static final String DEIDENTIFY_TEMPLATE_ID =
      "model-armour-deidentify-template-" + UUID.randomUUID().toString();
  private static Template TEST_MODELARMOR_TEMPLATE;
  private static Template TEST_MODELARMOR_TEMPLATE_NAME;
  private static String TEMPLATE_ID;

  private ByteArrayOutputStream stdOut;

  @BeforeClass
  public static void beforeAll() throws Exception {
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT_LOCATION", Strings.isNullOrEmpty(LOCATION));
  }

  @AfterClass
  public static void afterAll() throws Exception {
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    TEMPLATE_ID = "test-model-armor-" + UUID.randomUUID().toString();
  }

  @After
  public void afterEach() throws Exception {
    stdOut = null;
    System.setOut(null);
  }

  @Test
  public void testDeleteModelArmorTemplate() throws Exception {
    CreateTemplate.createTemplate(PROJECT_ID, LOCATION, TEMPLATE_ID);
    DeleteTemplate.deleteTemplate(PROJECT_ID, LOCATION, TEMPLATE_ID);
    assertThat(stdOut.toString()).contains("Deleted template");
  }

  @Test
  public void testGetModelArmorTemplate() throws Exception {
    CreateTemplate.createTemplate(PROJECT_ID, LOCATION, TEMPLATE_ID);
    GetTemplate.getTemplate(PROJECT_ID, LOCATION, TEMPLATE_ID);
    assertThat(stdOut.toString()).contains("Retrieved template");
    DeleteTemplate.deleteTemplate(PROJECT_ID, LOCATION, TEMPLATE_ID);
  }

  @Test
  public void testListModelArmorTemplates() throws Exception {
    CreateTemplate.createTemplate(PROJECT_ID, LOCATION, TEMPLATE_ID);
    ListTemplates.listTemplates(PROJECT_ID, LOCATION);
    assertThat(stdOut.toString()).contains("Retrieved Templates");
    DeleteTemplate.deleteTemplate(PROJECT_ID, LOCATION, TEMPLATE_ID);
  }

  @Test
  public void testListTemplatesWithFilter() throws Exception {
    CreateTemplate.createTemplate(PROJECT_ID, LOCATION, TEMPLATE_ID);
    ListTemplatesWithFilter.listTemplatesWithFilter(PROJECT_ID, LOCATION, TEMPLATE_ID);
    assertThat(stdOut.toString()).contains("Template with filter");
    DeleteTemplate.deleteTemplate(PROJECT_ID, LOCATION, TEMPLATE_ID);
  }
}
