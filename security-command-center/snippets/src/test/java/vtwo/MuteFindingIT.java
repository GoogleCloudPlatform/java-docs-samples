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
import com.google.cloud.securitycenter.v2.Finding.Mute;
import com.google.cloud.securitycenter.v2.ListFindingsRequest;
import com.google.cloud.securitycenter.v2.ListFindingsResponse.ListFindingsResult;
import com.google.cloud.securitycenter.v2.MuteConfig;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SecurityCenterClient.ListFindingsPagedResponse;
import com.google.cloud.securitycenter.v2.Source;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import vtwo.findings.CreateFindings;
import vtwo.muteconfig.BulkMuteFindings;
import vtwo.muteconfig.CreateMuteRule;
import vtwo.muteconfig.DeleteMuteRule;
import vtwo.muteconfig.GetMuteRule;
import vtwo.muteconfig.ListMuteRules;
import vtwo.muteconfig.SetMuteFinding;
import vtwo.muteconfig.SetUnmuteFinding;
import vtwo.muteconfig.UpdateMuteRule;
import vtwo.source.CreateSource;

// Test v2 Mute config samples.
@RunWith(JUnit4.class)
public class MuteFindingIT {

  // TODO(Developer): Replace the below variables.
  private static final String PROJECT_ID = System.getenv("SCC_PROJECT_ID");
  private static final String ORGANIZATION_ID = System.getenv("SCC_PROJECT_ORG_ID");
  private static final String LOCATION = "global";
  private static final String MUTE_RULE_CREATE = "random-mute-id-" + UUID.randomUUID();
  private static final String MUTE_RULE_UPDATE = "random-mute-id-" + UUID.randomUUID();
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 120000; // 2 minutes
  private static Source SOURCE;
  // The findings will be used to test bulk mute.
  private static Finding FINDING_1;
  private static Finding FINDING_2;
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
    requireEnvVar("SCC_PROJECT_ID");
    requireEnvVar("SCC_PROJECT_ORG_ID");

    // Create mute rules.
    CreateMuteRule.createMuteRule(PROJECT_ID, LOCATION, MUTE_RULE_CREATE);
    CreateMuteRule.createMuteRule(PROJECT_ID, LOCATION, MUTE_RULE_UPDATE);

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
    TimeUnit.MINUTES.sleep(3);
  }

  @AfterClass
  public static void cleanUp() throws IOException {
    final PrintStream out = System.out;
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    DeleteMuteRule.deleteMuteRule(PROJECT_ID, LOCATION, MUTE_RULE_CREATE);
    assertThat(stdOut.toString()).contains("Mute rule deleted successfully: " + MUTE_RULE_CREATE);
    DeleteMuteRule.deleteMuteRule(PROJECT_ID, LOCATION, MUTE_RULE_UPDATE);
    assertThat(stdOut.toString()).contains("Mute rule deleted successfully: " + MUTE_RULE_UPDATE);
    stdOut = null;
    System.setOut(out);
  }

  public static ListFindingsPagedResponse getAllFindings(String sourceName) throws IOException {
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      ListFindingsRequest request = ListFindingsRequest.newBuilder()
          .setParent(sourceName)
          .build();

      return client.listFindings(request);
    }
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
  public void testGetMuteRule() throws IOException {
    MuteConfig muteConfig = GetMuteRule.getMuteRule(PROJECT_ID, LOCATION, MUTE_RULE_CREATE);
    assertThat(muteConfig.getName()).contains(MUTE_RULE_CREATE);
  }

  @Test
  public void testListMuteRules() throws IOException {
    ListMuteRules.listMuteRules(PROJECT_ID, LOCATION);
    assertThat(stdOut.toString()).contains(MUTE_RULE_CREATE);
    assertThat(stdOut.toString()).contains(MUTE_RULE_UPDATE);
  }

  @Test
  public void testUpdateMuteRules() throws IOException {
    UpdateMuteRule.updateMuteRule(PROJECT_ID, LOCATION, MUTE_RULE_UPDATE);
    MuteConfig muteConfig = GetMuteRule.getMuteRule(PROJECT_ID, LOCATION, MUTE_RULE_UPDATE);
    assertThat(muteConfig.getDescription()).contains("Updated mute config description");
  }

  @Test
  public void testMuteUnmuteFinding() throws IOException {
    Finding finding = SetMuteFinding.setMute(FINDING_1.getName());
    assertThat(finding.getMute()).isEqualTo(Mute.MUTED);
    finding = SetUnmuteFinding.setUnmute(FINDING_1.getName());
    assertThat(finding.getMute()).isEqualTo(Mute.UNMUTED);
  }

  @Test
  public void testBulkMuteFindings() throws IOException, ExecutionException, InterruptedException {
    // Mute findings that belong to this project.
    BulkMuteFindings.bulkMute(PROJECT_ID, LOCATION,
        String.format("resource.project_display_name=\"%s\"", PROJECT_ID));

    // Get all findings in the source to check if they are muted.
    ListFindingsPagedResponse response =
        getAllFindings(
            String.format("projects/%s/sources/%s", PROJECT_ID, SOURCE.getName().split("/")[3]));
    for (ListFindingsResult finding : response.iterateAll()) {
      Assert.assertEquals(finding.getFinding().getMute(), Mute.MUTED);
    }
  }
}
