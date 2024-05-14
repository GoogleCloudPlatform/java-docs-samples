/* Copyright 2018 Google LLC
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.iam.admin.v1.ServiceAccount;
import com.google.iam.admin.v1.ServiceAccountKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServiceAccountTests {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String SERVICE_ACCOUNT =
          "service-account-" + UUID.randomUUID().toString().substring(0, 8);
  private static String SERVICE_ACCOUNT_KEY_ID;
  private ByteArrayOutputStream bout;
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
  public void beforeTest() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    bout.reset();
  }

  @Test
  public void stage1_testServiceAccountCreate() throws IOException {
    ServiceAccount serviceAccount = CreateServiceAccount
            .createServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);
    String got = bout.toString();
    assertThat(got, containsString("Created service account: " + SERVICE_ACCOUNT));
    assertNotNull(serviceAccount);
    assertThat(serviceAccount.getName(), containsString(SERVICE_ACCOUNT));

  }

  @Test
  public void stage1_testServiceAccountsList() throws IOException {
    IAMClient.ListServiceAccountsPagedResponse response =
            ListServiceAccounts.listServiceAccounts(PROJECT_ID);

    assertTrue(response.iterateAll().iterator().hasNext());
  }

  @Test
  public void stage2_testServiceAccountRename() throws IOException {
    String renameTo = "your-new-display-name";
    ServiceAccount serviceAccount = RenameServiceAccount
            .renameServiceAccount(PROJECT_ID, SERVICE_ACCOUNT, renameTo);
    String got = bout.toString();
    assertThat(got, containsString("Updated display name"));
    assertThat(got, containsString(renameTo));
    assertNotNull(serviceAccount);
    assertThat(renameTo, containsString(serviceAccount.getDisplayName()));
  }

  @Test
  public void stage2_testServiceAccountGet() throws IOException {
    ServiceAccount account = GetServiceAccount.getServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);

    assertTrue(account.getName().contains(SERVICE_ACCOUNT));
    assertEquals(PROJECT_ID, account.getProjectId());
  }

  @Test
  public void stage2_testServiceAccountKeyCreate() throws IOException {
    ServiceAccountKey key = CreateServiceAccountKey.createKey(PROJECT_ID, SERVICE_ACCOUNT);
    SERVICE_ACCOUNT_KEY_ID =  key.getName()
            .substring(key.getName().lastIndexOf("/") + 1)
            .trim();

    assertNotNull(SERVICE_ACCOUNT_KEY_ID);
  }

  @Test
  public void stage2_testServiceAccountKeyGet() throws IOException {
    ServiceAccountKey key = GetServiceAccountKey
            .getServiceAccountKey(PROJECT_ID, SERVICE_ACCOUNT, SERVICE_ACCOUNT_KEY_ID);

    assertTrue(key.getName().contains(SERVICE_ACCOUNT_KEY_ID));
    assertTrue(key.getName().contains(PROJECT_ID));
    assertTrue(key.getName().contains(SERVICE_ACCOUNT));
  }

  @Test
  public void stage2_testServiceAccountKeysList() throws IOException {
    List<ServiceAccountKey> keys = ListServiceAccountKeys.listKeys(PROJECT_ID, SERVICE_ACCOUNT);

    assertNotEquals(0, keys.size());
    assertTrue(keys.stream()
            .map(ServiceAccountKey::getName)
            .anyMatch(keyName -> keyName.contains(SERVICE_ACCOUNT_KEY_ID)));
  }

  @Test
  public void stage2_testServiceAccountKeyDisable() throws IOException {
    DisableServiceAccountKey
        .disableServiceAccountKey(PROJECT_ID, SERVICE_ACCOUNT, SERVICE_ACCOUNT_KEY_ID);
    ServiceAccountKey key = GetServiceAccountKey
            .getServiceAccountKey(PROJECT_ID, SERVICE_ACCOUNT, SERVICE_ACCOUNT_KEY_ID);

    assertTrue(key.getName().contains(SERVICE_ACCOUNT_KEY_ID));
    assertTrue(key.getDisabled());
  }

  @Test
  public void stage2_testServiceAccountKeyEnable() throws IOException {
    EnableServiceAccountKey
        .enableServiceAccountKey(PROJECT_ID, SERVICE_ACCOUNT, SERVICE_ACCOUNT_KEY_ID);
    ServiceAccountKey key = GetServiceAccountKey
            .getServiceAccountKey(PROJECT_ID, SERVICE_ACCOUNT, SERVICE_ACCOUNT_KEY_ID);

    assertTrue(key.getName().contains(SERVICE_ACCOUNT_KEY_ID));
    assertFalse(key.getDisabled());
  }

  @Test
  public void stage3_testServiceAccountKeyDelete() throws IOException {
    DeleteServiceAccountKey.deleteKey(PROJECT_ID, SERVICE_ACCOUNT, SERVICE_ACCOUNT_KEY_ID);
    String got = bout.toString();
    assertThat(got, containsString("Deleted key:"));

    bout.reset();
    ListServiceAccountKeys.listKeys(PROJECT_ID, SERVICE_ACCOUNT);
    got = bout.toString();
    assertThat(got, !containsString(SERVICE_ACCOUNT_KEY_ID).matches(got));
  }

  @Test
  public void stage4_testDisableServiceAccount() throws IOException {
    DisableServiceAccount.disableServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);
    ServiceAccount serviceAccount = GetServiceAccount
            .getServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);

    assertTrue(serviceAccount.getName().contains(SERVICE_ACCOUNT));
    assertEquals(PROJECT_ID, serviceAccount.getProjectId());
    assertTrue(SERVICE_ACCOUNT, serviceAccount.getDisabled());
  }

  @Test
  public void stage5_testEnableServiceAccount() throws IOException {
    EnableServiceAccount.enableServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);
    ServiceAccount serviceAccount = GetServiceAccount
            .getServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);

    assertTrue(serviceAccount.getName().contains(SERVICE_ACCOUNT));
    assertEquals(PROJECT_ID, serviceAccount.getProjectId());
    assertFalse(SERVICE_ACCOUNT, serviceAccount.getDisabled());
  }

  @Test
  public void stage6_testServiceAccountDelete() throws IOException {
    DeleteServiceAccount.deleteServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);
    String got = bout.toString();
    assertThat(got, containsString("Deleted service account:"));

    bout.reset();
    ListServiceAccounts.listServiceAccounts(PROJECT_ID);
    got = bout.toString();
    assertThat(got, !containsString(SERVICE_ACCOUNT).matches(got));
  }
}
