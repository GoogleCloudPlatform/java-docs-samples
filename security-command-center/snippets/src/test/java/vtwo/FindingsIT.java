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

import com.google.cloud.securitycenter.v2.Finding;
import com.google.cloud.securitycenter.v2.Finding.State;
import com.google.cloud.securitycenter.v2.Source;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import vtwo.findings.CreateFindings;
import vtwo.findings.GroupFindings;
import vtwo.findings.GroupFindingsWithFilter;
import vtwo.findings.ListAllFindings;
import vtwo.findings.ListFindingsWithFilter;
import vtwo.findings.SetFindingsByState;
import vtwo.source.CreateSource;

// Test v2 Findings samples.
@RunWith(JUnit4.class)
public class FindingsIT {

  // TODO(Developer): Replace the below variables.
  private static final String ORGANIZATION_ID = System.getenv("SCC_PROJECT_ORG_ID");
  private static final String LOCATION = "global";
  private static Source SOURCE;
  private static Finding FINDING_1;
  private static Finding FINDING_2;
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 240000; // 4 minutes

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
    SOURCE = CreateSource.createSource(ORGANIZATION_ID);

    // Create findings within the source.
    String uuid = UUID.randomUUID().toString().split("-")[0];
    FINDING_1 = CreateFindings.createFinding(ORGANIZATION_ID, LOCATION, "testfindingv2" + uuid,
        SOURCE.getName().split("/")[3], Optional.of("MEDIUM_RISK_ONE"));

    uuid = UUID.randomUUID().toString().split("-")[0];
    FINDING_2 = CreateFindings.createFinding(ORGANIZATION_ID, LOCATION, "testfindingv2" + uuid,
        SOURCE.getName().split("/")[3], Optional.empty());

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
  public void testListAllFindings() throws IOException {
    ListAllFindings.listAllFindings(ORGANIZATION_ID, SOURCE.getName().split("/")[3], LOCATION);

    assertThat(stdOut.toString()).contains(FINDING_1.getName());
    assertThat(stdOut.toString()).contains(FINDING_2.getName());
  }

  @Test
  public void testListFilteredFindings() throws IOException {
    ListFindingsWithFilter.listFilteredFindings(ORGANIZATION_ID, SOURCE.getName().split("/")[3],
        LOCATION);

    assertThat(stdOut.toString()).contains(FINDING_1.getName());
    assertThat(stdOut.toString()).doesNotContain(FINDING_2.getName());
  }

  @Test
  public void testGroupAllFindings() throws IOException {
    GroupFindings.groupFindings(ORGANIZATION_ID, SOURCE.getName().split("/")[3], LOCATION);

    assertThat(stdOut.toString()).contains("Listed grouped findings.");
  }

  @Test
  public void testGroupFilteredFindings() throws IOException {
    GroupFindingsWithFilter.groupFilteredFindings(ORGANIZATION_ID, SOURCE.getName().split("/")[3],
        LOCATION);

    assertThat(stdOut.toString()).contains("count: 1");
  }

  @Test
  public void testSetFindingsByStateInactive() throws IOException {
    Finding response = SetFindingsByState.setFindingState(ORGANIZATION_ID, LOCATION,
        SOURCE.getName().split("/")[3],
        FINDING_1.getName().split("/")[7]);

    assertThat(response.getState()).isEqualTo(State.INACTIVE);
  }

}
