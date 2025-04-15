/* Copyright 2025 Google LLC
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.iam.admin.v1.ServiceAccount;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EnableServiceAccountIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private ByteArrayOutputStream bout;
  private String serviceAccountName;
  private final PrintStream originalOut = System.out;

  @Rule public MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  private static void requireEnvVar(String varName) {
    assertNotNull(
        System.getenv(varName),
        String.format("Environment variable '%s' is required to perform these tests.", varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void beforeTest() throws IOException, InterruptedException {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    // Set up test
    serviceAccountName = Util.generateServiceAccountName();
    Util.setUpTest_createServiceAccount(PROJECT_ID, serviceAccountName);
    Util.setUpTest_disableServiceAccount(PROJECT_ID, serviceAccountName);
  }

  @After
  public void tearDown() throws IOException {
    // Cleanup test
    Util.tearDownTest_deleteServiceAccount(PROJECT_ID, serviceAccountName);

    System.setOut(originalOut);
    bout.reset();
  }

  @Test
  public void testEnableServiceAccount() throws IOException, InterruptedException {
    // Act
    EnableServiceAccount.enableServiceAccount(PROJECT_ID, serviceAccountName);

    // Assert
    ServiceAccount serviceAccount = Util.test_getServiceAccount(PROJECT_ID, serviceAccountName);
    assertTrue(serviceAccount.getName().contains(serviceAccountName));
    assertEquals(PROJECT_ID, serviceAccount.getProjectId());
    assertFalse(serviceAccountName, serviceAccount.getDisabled());
  }
}
