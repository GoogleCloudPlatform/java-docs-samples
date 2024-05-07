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
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.google.cloud.securitycenter.v2.Finding;
import com.google.cloud.securitycenter.v2.SecurityMarks;
import com.google.cloud.securitycenter.v2.Source;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import vtwo.findings.CreateFindings;
import vtwo.marks.AddMarkToFinding;
import vtwo.marks.DeleteAndUpdateMarks;
import vtwo.marks.DeleteMarks;
import vtwo.marks.ListFindingMarksWithFilter;
import vtwo.source.CreateSource;

@RunWith(JUnit4.class)
public class SecurityMarkIT {

  // TODO: Replace the below variables.
  private static final String ORGANIZATION_ID = System.getenv("SCC_PROJECT_ORG_ID");
  private static final String LOCATION = "global";
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 120000; // 2 minutes
  private static Source SOURCE;
  private static Finding FINDING_1;
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

    // Create source.
    SOURCE = CreateSource.createSource(ORGANIZATION_ID);

    // Create findings within the source.
    String uuid = UUID.randomUUID().toString().split("-")[0];
    FINDING_1 = CreateFindings.createFinding(ORGANIZATION_ID, LOCATION, "testfindingv2" + uuid,
        SOURCE.getName().split("/")[3], Optional.of("MEDIUM_RISK_ONE"));

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

  @AfterClass
  public static void cleanUp() throws IOException {
    final PrintStream out = System.out;
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    stdOut = null;
    System.setOut(out);
  }

  @Test
  public void testAddMarksToFinding() throws IOException {
    SecurityMarks response = AddMarkToFinding.addMarksToFinding(
        ORGANIZATION_ID, SOURCE.getName().split("/")[3], LOCATION,
        FINDING_1.getName().split("/")[7]);

    assertTrue(response.getMarksOrThrow("key_a").contains("value_a"));
  }

  @Test
  public void testDeleteSecurityMark() throws IOException {
    SecurityMarks response = DeleteMarks.deleteMarks(
        ORGANIZATION_ID, SOURCE.getName().split("/")[3], LOCATION,
        FINDING_1.getName().split("/")[7]);

    assertFalse(response.containsMarks("key_a"));
  }

  @Test
  public void testDeleteAndUpdateMarks() throws IOException {
    SecurityMarks response = DeleteAndUpdateMarks.deleteAndUpdateMarks(
        ORGANIZATION_ID, SOURCE.getName().split("/")[3], LOCATION,
        FINDING_1.getName().split("/")[7]);

    // Assert update for key_a
    assertTrue(response.getMarksOrThrow("key_a").contains("new_value_for_a"));

    // Assert deletion for key_b
    assertFalse(response.getMarksMap().containsKey("key_b"));
  }

  @Test
  public void testListFindingsWithQueryMarks() throws IOException {
    List<Finding> response = ListFindingMarksWithFilter.listFindingsWithQueryMarks(
        ORGANIZATION_ID, SOURCE.getName().split("/")[3], LOCATION);

    assertThat(response.stream().map(Finding::getName)).contains(FINDING_1.getName());
  }
}
