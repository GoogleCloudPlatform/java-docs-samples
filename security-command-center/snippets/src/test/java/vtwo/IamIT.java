/*
 * Copyright 2024 Google LLC
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

package vtwo;


import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.securitycenter.v2.Source;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import vtwo.iam.GetIamPolicies;
import vtwo.iam.SetIamPolices;
import vtwo.iam.TestIamPermissions;

@RunWith(JUnit4.class)
public class IamIT {

  // TODO(Developer): Replace the below variables.
  private static final String ORGANIZATION_ID = System.getenv("SCC_PROJECT_ORG_ID");
  private static final String USER_EMAIL = "someuser@domain.com";
  private static final String PERMISSION = "securitycenter.findings.update";
  private static Source SOURCE;
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 120000; // 2 minutes
  private static ByteArrayOutputStream stdOut;
  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(
      MAX_ATTEMPT_COUNT,
      INITIAL_BACKOFF_MILLIS);

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

    // Create source.
    SOURCE = Util.createSource(ORGANIZATION_ID);

    stdOut = null;
    System.setOut(out);
    TimeUnit.MINUTES.sleep(1);
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

  @Test
  public void testIamPermissions() throws IOException {
    TestIamPermissions.testIamPermissions(ORGANIZATION_ID, SOURCE.getName().split("/")[3],
        PERMISSION);

    assertThat(stdOut.toString()).contains("IAM Permission: ");
  }

  @Test
  public void testGetIamPolicies() throws IOException {
    GetIamPolicies.getIamPolicySource(ORGANIZATION_ID, SOURCE.getName().split("/")[3]);

    assertThat(stdOut.toString()).contains("Policy: ");
  }

  @Test
  public void testSetIamPolices() throws IOException {
    SetIamPolices.setIamPolicySource(ORGANIZATION_ID, SOURCE.getName().split("/")[3], USER_EMAIL);

    assertThat(stdOut.toString()).contains("Set iam policy: ");
  }
}
