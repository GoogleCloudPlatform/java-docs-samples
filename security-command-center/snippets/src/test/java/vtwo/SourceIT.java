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
import static org.junit.Assert.assertTrue;

import com.google.cloud.securitycenter.v2.Finding;
import com.google.cloud.securitycenter.v2.Source;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.protobuf.Value;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import vtwo.findings.CreateFindings;
import vtwo.muteconfig.SetMuteFinding;
import vtwo.source.CreateSource;
import vtwo.source.GetSource;
import vtwo.source.ListSources;
import vtwo.source.UpdateFindingSource;
import vtwo.source.UpdateSource;

@RunWith(JUnit4.class)
public class SourceIT {

  // TODO: Replace the below variables.
  private static final String ORGANIZATION_ID = System.getenv("SCC_PROJECT_ORG_ID");
  private static final String LOCATION = "global";
  private static Source SOURCE;
  private static Finding FINDING;
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
    SOURCE = CreateSource.createSource(ORGANIZATION_ID);

    // Create findings within the source.
    String uuid = UUID.randomUUID().toString().split("-")[0];
    FINDING = CreateFindings.createFinding(ORGANIZATION_ID, LOCATION, "testfindingv2" + uuid,
        SOURCE.getName().split("/")[3], Optional.of("MEDIUM_RISK_ONE"));

    stdOut = null;
    System.setOut(out);
  }

  @AfterClass
  public static void cleanUp() throws IOException {
    final PrintStream out = System.out;
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Mute an individual finding.
    SetMuteFinding.setMute(FINDING.getName());

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

  @Test
  public void testListAllSources() throws IOException {
    List<Source> response = ListSources.listSources(ORGANIZATION_ID);

    assertThat(response.stream().map(Source::getName)).contains(SOURCE.getName());
  }

  @Test
  public void testGetSource() throws IOException {
    Source source = GetSource.getSource(ORGANIZATION_ID, SOURCE.getName().split("/")[3]);

    assertThat(source.getName()).isEqualTo(SOURCE.getName());
  }

  @Test
  public void testUpdateSource() throws IOException {
    Source source = UpdateSource.updateSource(ORGANIZATION_ID, SOURCE.getName().split("/")[3]);

    assertThat(source.getDisplayName()).contains("Updated Display Name");
  }

  @Test
  public void testUpdateFindingSource() throws IOException {
    Value stringValue = Value.newBuilder().setStringValue("value").build();

    assertTrue(UpdateFindingSource.updateFinding(ORGANIZATION_ID, LOCATION,
            SOURCE.getName().split("/")[3], FINDING.getName().split("/")[7])
        .getSourcePropertiesMap()
        .get("stringKey")
        .equals(stringValue));
  }

}
