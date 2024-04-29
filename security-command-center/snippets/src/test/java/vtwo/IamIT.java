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
import com.google.iam.v1.Policy;
import com.google.iam.v1.TestIamPermissionsResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import vtwo.iam.GetIamPolicies;
import vtwo.iam.SetIamPolices;
import vtwo.iam.TestIamPermissions;
import vtwo.source.CreateSource;

public class IamIT {

  private static final String ORGANIZATION_ID = System.getenv("SCC_PROJECT_ORG_ID");
  private static final String USER_EMAIL = "someuser@domain.com";
  private static final String USER_PERMISSION = "securitycenter.findings.update";
  private static final String USER_ROLE = "roles/securitycenter.findingsEditor";
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
  public static void setUp() throws IOException {
    final PrintStream out = System.out;
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");

    // Create source.
    SOURCE = CreateSource.createSource(ORGANIZATION_ID);

    stdOut = null;
    System.setOut(out);
  }

  @Test
  public void testIamPermissions() {
    TestIamPermissionsResponse testIamPermissions = TestIamPermissions.testIamPermissions(
        ORGANIZATION_ID, SOURCE.getName().split("/")[3],
        USER_PERMISSION);

    assertThat(testIamPermissions.toString()).contains(USER_PERMISSION);
  }

  @Test
  public void testGetIamPolicies() {
    Policy policy = GetIamPolicies.getIamPolicySource(ORGANIZATION_ID,
        SOURCE.getName().split("/")[3]);

    assertThat(policy).isNotNull();
    assertThat(policy).isNotEqualTo(Policy.getDefaultInstance());
  }

  @Test
  public void testSetIamPolices() {
    Policy policyUpdated = SetIamPolices.setIamPolicySource(ORGANIZATION_ID,
        SOURCE.getName().split("/")[3], USER_EMAIL,
        USER_ROLE);

    assertThat(policyUpdated).isNotNull();
    assertThat(policyUpdated).isNotEqualTo(Policy.getDefaultInstance());
  }
}
