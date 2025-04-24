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
import static org.junit.Assert.assertNotNull;

import com.google.cloud.modelarmor.v1.FilterConfig;
import com.google.cloud.modelarmor.v1.FloorSetting;
import com.google.cloud.modelarmor.v1.FloorSettingName;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.UpdateFloorSettingRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
  private static final String FOLDER_ID = System.getenv("MA_FOLDER_ID");
  private static final String ORGANIZATION_ID = System.getenv("MA_ORG_ID");
  private static String projectFloorSettingName = FloorSettingName
      .ofProjectLocationName(PROJECT_ID, "global").toString();
  private static String folderFloorSettingName = FloorSettingName
      .ofFolderLocationName(FOLDER_ID, "global").toString();
  private static String organizationFloorSettingName = FloorSettingName
      .ofOrganizationLocationName(ORGANIZATION_ID, "global").toString();
  private static String[] floorSettingNames;
  private ByteArrayOutputStream stdOut;
  private PrintStream originalOut;

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
    requireEnvVar("MA_FOLDER_ID");
    requireEnvVar("MA_ORG_ID");
  }

  @AfterClass
  public static void afterAll() throws IOException {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
    requireEnvVar("MA_FOLDER_ID");
    requireEnvVar("MA_ORG_ID");

    floorSettingNames = new String[] {
        projectFloorSettingName, folderFloorSettingName, organizationFloorSettingName
    };

    resetFloorSettings();
  }

  @Before
  public void beforeEach() {
    originalOut = System.out;
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void afterEach() throws IOException {
    System.setOut(originalOut);
    stdOut = null;
  }

  private static void resetFloorSettings() throws IOException {
    try (ModelArmorClient client = ModelArmorClient.create()) {
      for (String name : floorSettingNames) {
        FloorSetting floorSetting = FloorSetting.newBuilder()
            .setName(name)
            .setFilterConfig(FilterConfig.newBuilder().build())
            .setEnableFloorSettingEnforcement(false)
            .build();

        UpdateFloorSettingRequest request = UpdateFloorSettingRequest.newBuilder()
            .setFloorSetting(floorSetting)
            .build();

        client.updateFloorSetting(request);
      }
    }
  }

  @Test
  public void testGetFolderFloorSetting() throws IOException {
    GetFolderFloorSetting.getFolderFloorSetting(FOLDER_ID);
    assertThat(stdOut.toString()).contains("Fetched floor setting for folder:");
  }

  @Test
  public void testGetOrganizationFloorSetting() throws IOException {
    GetOrganizationFloorSetting.getOrganizationFloorSetting(ORGANIZATION_ID);
    assertThat(stdOut.toString()).contains("Fetched floor setting for organization:");
  }

  @Test
  public void testGetProjectFloorSetting() throws IOException {
    GetProjectFloorSetting.getProjectFloorSetting(PROJECT_ID);
    assertThat(stdOut.toString()).contains("Fetched floor setting for project:");
  }

  @Test
  public void testUpdateFolderFloorSetting() throws IOException {
    UpdateFolderFloorSetting.updateFolderFloorSetting(FOLDER_ID);
    assertThat(stdOut.toString()).contains("Updated floor setting for folder:");
  }

  @Test
  public void testUpdateOrganizationFloorSetting() throws IOException {
    UpdateOrganizationsFloorSetting.updateOrganizationFloorSetting(ORGANIZATION_ID);
    assertThat(stdOut.toString()).contains("Updated floor setting for organization:");
  }

  @Test
  public void testUpdateProjectFloorSetting() throws IOException {
    UpdateProjectFloorSetting.updateProjectFloorSetting(PROJECT_ID);
    assertThat(stdOut.toString()).contains("Updated floor setting for project:");
  }
}
