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

package v2;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.securitycenter.v2.Finding;
import com.google.cloud.securitycenter.v2.Finding.Mute;
import com.google.cloud.securitycenter.v2.ListFindingsRequest;
import com.google.cloud.securitycenter.v2.ListFindingsResponse.ListFindingsResult;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SecurityCenterClient.ListFindingsPagedResponse;
import com.google.cloud.securitycenter.v2.Source;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import v2.muteconfig.BulkMuteFindingsV2;
import v2.muteconfig.CreateMuteRuleV2;
import v2.muteconfig.DeleteMuteRuleV2;
import v2.muteconfig.GetMuteRuleV2;
import v2.muteconfig.ListMuteRulesV2;
import v2.muteconfig.SetMuteFindingV2;
import v2.muteconfig.SetUnmuteFindingV2;
import v2.muteconfig.UpdateMuteRuleV2;

// Test v2 Mute config samples.
@RunWith(JUnit4.class)
public class MuteFindingIT {

  // TODO(Developer): Replace the below variables.
  private static final String PROJECT_ID = System.getenv("SCC_PROJECT_ID");
  private static final String ORGANIZATION_ID = System.getenv("SCC_PROJECT_ORG_ID");
  private static final String LOCATION = "global";
  private static final String MUTE_RULE_CREATE = "random-mute-id-" + UUID.randomUUID();
  private static final String MUTE_RULE_UPDATE = "random-mute-id-" + UUID.randomUUID();
  private static Source SOURCE;
  // The findings will be used to test bulk mute.
  private static Finding FINDING_1;
  private static Finding FINDING_2;
  private static ByteArrayOutputStream stdOut;

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
    requireEnvVar("SCC_PROJECT_ID");
    requireEnvVar("SCC_PROJECT_ORG_ID");

    // Create mute rules.
    CreateMuteRuleV2.createMuteRule(PROJECT_ID, LOCATION, MUTE_RULE_CREATE);
    CreateMuteRuleV2.createMuteRule(PROJECT_ID, LOCATION, MUTE_RULE_UPDATE);
    // Create source.
    SOURCE = Util.createSource(ORGANIZATION_ID);
    // Create findings within the source.
    String uuid = UUID.randomUUID().toString().split("-")[0];
    FINDING_1 = Util.createFinding(SOURCE.getName(), "test-finding-mute-v2-" + uuid,
        Optional.empty());
    FINDING_2 = Util.createFinding(SOURCE.getName(), "test-finding-mute-v2-" + uuid,
        Optional.empty());

    stdOut = null;
    System.setOut(out);
  }

  @AfterClass
  public static void cleanUp() {
    final PrintStream out = System.out;
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    DeleteMuteRuleV2.deleteMuteRule(PROJECT_ID, LOCATION, MUTE_RULE_CREATE);
    assertThat(stdOut.toString()).contains("Mute rule deleted successfully: " + MUTE_RULE_CREATE);
    DeleteMuteRuleV2.deleteMuteRule(PROJECT_ID, LOCATION, MUTE_RULE_UPDATE);
    assertThat(stdOut.toString()).contains("Mute rule deleted successfully: " + MUTE_RULE_UPDATE);
    stdOut = null;
    System.setOut(out);
  }

  public static ListFindingsPagedResponse getAllFindings(String sourceName) throws IOException {
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      ListFindingsRequest request = ListFindingsRequest.newBuilder().setParent(sourceName).build();

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
  public void testGetMuteRule() {
    GetMuteRuleV2.getMuteRule(PROJECT_ID, LOCATION, MUTE_RULE_CREATE);
    assertThat(stdOut.toString()).contains("Retrieved the mute config: ");
    assertThat(stdOut.toString()).contains(MUTE_RULE_CREATE);
  }

  @Test
  public void testListMuteRules() {
    ListMuteRulesV2.listMuteRules(PROJECT_ID, LOCATION);
    assertThat(stdOut.toString()).contains(MUTE_RULE_CREATE);
    assertThat(stdOut.toString()).contains(MUTE_RULE_UPDATE);
  }

  @Test
  public void testUpdateMuteRules() {
    UpdateMuteRuleV2.updateMuteRule(PROJECT_ID, LOCATION, MUTE_RULE_UPDATE);
    GetMuteRuleV2.getMuteRule(PROJECT_ID, LOCATION, MUTE_RULE_UPDATE);
    assertThat(stdOut.toString()).contains("Updated mute config description");
  }

  @Test
  public void testSetMuteFinding() {
    SetMuteFindingV2.setMute(PROJECT_ID, LOCATION, SOURCE.getName(), FINDING_1.getName());
    assertThat(stdOut.toString())
        .contains("Mute value for the finding " + FINDING_1.getName() + " is: " + "MUTED");
    SetUnmuteFindingV2.setUnmute(PROJECT_ID, LOCATION, SOURCE.getName(), FINDING_1.getName());
    assertThat(stdOut.toString())
        .contains("Mute value for the finding " + FINDING_1.getName() + " is: " + "UNMUTED");
  }

  @Test
  public void testBulkMuteFindings() throws IOException {
    // Mute findings that belong to this project.
    BulkMuteFindingsV2.bulkMute(PROJECT_ID, LOCATION,
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
