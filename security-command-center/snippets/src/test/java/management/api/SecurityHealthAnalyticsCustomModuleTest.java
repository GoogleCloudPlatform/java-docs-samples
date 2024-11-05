/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package management.api;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertNotNull;

import com.google.cloud.securitycentermanagement.v1.ListSecurityHealthAnalyticsCustomModulesRequest;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient.ListSecurityHealthAnalyticsCustomModulesPagedResponse;
import com.google.cloud.securitycentermanagement.v1.SecurityHealthAnalyticsCustomModule;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.common.base.Strings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SecurityHealthAnalyticsCustomModuleTest {

  private static final String ORGANIZATION_ID = System.getenv("SCC_PROJECT_ORG_ID");
  private static final String LOCATION = "global";
  private static final String CUSTOM_MODULE_DISPLAY_NAME = "java_sample_custom_module_test";
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 120000; // 2 minutes
  private static ByteArrayOutputStream stdOut;

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule =
      new MultipleAttemptsRule(MAX_ATTEMPT_COUNT, INITIAL_BACKOFF_MILLIS);

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void setUp() throws IOException, InterruptedException {
    final PrintStream out = System.out;
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("SCC_PROJECT_ORG_ID");

    // Perform cleanup before running tests
    cleanupExistingCustomModules();

    stdOut = null;
    System.setOut(out);
    TimeUnit.MINUTES.sleep(3);
  }

  @AfterClass
  public static void cleanUp() {
    final PrintStream out = System.out;
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    stdOut = null;
    System.setOut(out);
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }

  // cleanupExistingCustomModules clean up all the existing custom module
  public static void cleanupExistingCustomModules() throws IOException {
    String parent = String.format("organizations/%s/locations/%s", ORGANIZATION_ID, LOCATION);

    try (SecurityCenterManagementClient client = SecurityCenterManagementClient.create()) {

      // create the request
      ListSecurityHealthAnalyticsCustomModulesRequest request =
          ListSecurityHealthAnalyticsCustomModulesRequest.newBuilder().setParent(parent).build();

      // calls the API
      ListSecurityHealthAnalyticsCustomModulesPagedResponse response =
          client.listSecurityHealthAnalyticsCustomModules(request);

      // Iterate over the response and delete custom module one by one which start with
      // java_sample_custom_module
      for (SecurityHealthAnalyticsCustomModule module : response.iterateAll()) {
        if (module.getDisplayName().startsWith("java_sample_custom_module")) {
          String customModuleId = extractCustomModuleId(module.getName());
          deleteCustomModule(parent, customModuleId);
        }
      }
    }
  }

  // extractCustomModuleID extracts the custom module Id from the full name
  public static String extractCustomModuleId(String customModuleFullName) {
    if (!Strings.isNullOrEmpty(customModuleFullName)) {
      String[] result = customModuleFullName.split("/");
      if (result.length > 0) {
        return result[result.length - 1];
      }
    }
    return "";
  }

  // createCustomModule method is for creating the custom module
  public static SecurityHealthAnalyticsCustomModule createCustomModule(
      String parent, String customModuleDisplayName) throws IOException {
    if (!Strings.isNullOrEmpty(parent) && !Strings.isNullOrEmpty(customModuleDisplayName)) {
      SecurityHealthAnalyticsCustomModule response =
          CreateSecurityHealthAnalyticsCustomModule.createSecurityHealthAnalyticsCustomModule(
              parent, customModuleDisplayName);
      return response;
    }
    return null;
  }

  // deleteCustomModule method is for deleting the custom module
  public static void deleteCustomModule(String parent, String customModuleId) throws IOException {
    if (!Strings.isNullOrEmpty(parent) && !Strings.isNullOrEmpty(customModuleId)) {
      DeleteSecurityHealthAnalyticsCustomModule.deleteSecurityHealthAnalyticsCustomModule(
          parent, customModuleId);
    }
  }

  @Test
  public void testCreateSecurityHealthAnalyticsCustomModule() throws IOException {

    String parent = String.format("organizations/%s/locations/%s", ORGANIZATION_ID, LOCATION);

    // creating the custom module
    SecurityHealthAnalyticsCustomModule response =
        CreateSecurityHealthAnalyticsCustomModule.createSecurityHealthAnalyticsCustomModule(
            parent, CUSTOM_MODULE_DISPLAY_NAME);

    // assert that response is not null
    assertNotNull(response);

    // assert that created module display name is matching with the name passed
    assertThat(response.getDisplayName()).isEqualTo(CUSTOM_MODULE_DISPLAY_NAME);
  }

  @Test
  public void testDeleteSecurityHealthAnalyticsCustomModule() throws IOException {

    String parent = String.format("organizations/%s/locations/%s", ORGANIZATION_ID, LOCATION);

    // create the custom module
    SecurityHealthAnalyticsCustomModule response =
        createCustomModule(parent, CUSTOM_MODULE_DISPLAY_NAME);

    // extracting the custom module id from the full name
    String customModuleId = extractCustomModuleId(response.getName());

    // delete the custom module with the custom module id
    DeleteSecurityHealthAnalyticsCustomModule.deleteSecurityHealthAnalyticsCustomModule(
        parent, customModuleId);

    // assert that std output is matching with the string passed
    assertThat(stdOut.toString())
        .contains("SecurityHealthAnalyticsCustomModule deleted : " + customModuleId);
  }

  @Test
  public void testListSecurityHealthAnalyticsCustomModules() throws IOException {

    String parent = String.format("organizations/%s/locations/%s", ORGANIZATION_ID, LOCATION);

    // create the custom module
    createCustomModule(parent, CUSTOM_MODULE_DISPLAY_NAME);

    // call the API, list all the custom modules
    ListSecurityHealthAnalyticsCustomModulesPagedResponse response =
        ListSecurityHealthAnalyticsCustomModules.listSecurityHealthAnalyticsCustomModules(parent);

    // assert that response is not null
    assertNotNull(response);

    // list should have the custom module which we have created
    assertThat(stdOut.toString()).contains("Custom module name : " + CUSTOM_MODULE_DISPLAY_NAME);
  }

  @Test
  public void testGetSecurityHealthAnalyticsCustomModule() throws IOException {

    String parent = String.format("organizations/%s/locations/%s", ORGANIZATION_ID, LOCATION);

    // create the custom module
    SecurityHealthAnalyticsCustomModule createCustomModuleResponse =
        createCustomModule(parent, CUSTOM_MODULE_DISPLAY_NAME);

    // extracting the custom module id from the full name
    String customModuleId = extractCustomModuleId(createCustomModuleResponse.getName());

    // get the custom module with the custom module id
    SecurityHealthAnalyticsCustomModule getCustomModuleResponse =
        GetSecurityHealthAnalyticsCustomModule.getSecurityHealthAnalyticsCustomModule(
            parent, customModuleId);

    // assert that custom module name is matching with the name passed
    assertThat(getCustomModuleResponse.getDisplayName()).isEqualTo(CUSTOM_MODULE_DISPLAY_NAME);

    // assert that custom module id is matching with the id passed
    assertThat(extractCustomModuleId(getCustomModuleResponse.getName())).isEqualTo(customModuleId);
  }
}
