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

import com.google.common.base.Strings;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class SnippetsIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION = "us-central1";
  private static final String FOLDER_ID = "folder-id";
  private static final String ORGANIZATION_ID = "organization-id";

  private ByteArrayOutputStream stdOut;

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
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void afterEach() throws Exception {
    stdOut = null;
    System.setOut(null);
  }

  @Test
  public void testGetFolderFloorSettings() throws Exception {
    GetFolderFloorSettings.getFolderFloorSettings(FOLDER_ID);
    assertThat(stdOut.toString()).contains("Folder Floor Settings");
  }

  @Test
  public void testGetOrganizationFloorSettings() throws Exception {
    GetOrganizationFloorSettings.getOrganizationFloorSettings(ORGANIZATION_ID);
    assertThat(stdOut.toString()).contains("Organization Floor Settings");
  }

  @Test
  public void testGetProjectFloorSettings() throws Exception {
    GetProjectFloorSettings.getProjectFloorSettings(PROJECT_ID);
    assertThat(stdOut.toString()).contains("Project Floor Settings");
  }

  @Test
  public void testUpdateFolderFloorSettings() throws Exception {
    UpdateFolderFloorSettings.updateFolderFloorSettings(FOLDER_ID);
    assertThat(stdOut.toString()).contains("Updated Folder Floor Settings");
  }

  @Test
  public void testUpdateOrganizationFloorSettings() throws Exception {
    UpdateOrganizationsFloorSettings.updateOrganizationFloorSettings(ORGANIZATION_ID);
    assertThat(stdOut.toString()).contains("Updated Organization Floor Settings");
  }

  @Test
  public void testUpdateProjectFloorSettings() throws Exception {
    UpdateProjectFloorSettings.updateProjectFloorSettings(PROJECT_ID);
    assertThat(stdOut.toString()).contains("Updated Project Floor Settings");
  }
}
