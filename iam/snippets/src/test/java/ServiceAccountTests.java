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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ServiceAccountTests {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private ByteArrayOutputStream bout;
  private final PrintStream originalOut = System.out;

  @Rule public MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  private static void requireEnvVar(String varName) {
    assertNotNull(
        System.getenv(varName),
        String.format("Environment variable '%s' is required to perform these tests.", varName));
  }

  private static String generateServiceAccountName() {
    return "service-account-" + UUID.randomUUID().toString().substring(0, 8);
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
  public void testServiceAccount_createServiceAccount() throws IOException {
    // Prepare
    String serviceAccountName = generateServiceAccountName();

    // Act
    ServiceAccount serviceAccount =
        CreateServiceAccount.createServiceAccount(PROJECT_ID, serviceAccountName);
    String got = bout.toString();

    // Assert
    assertThat(got, containsString("Created service account: " + serviceAccountName));
    assertNotNull(serviceAccount);
    assertThat(serviceAccount.getName(), containsString(serviceAccountName));

    // Cleanup
    Util.tearDownTest_deleteServiceAccount(PROJECT_ID, serviceAccountName);
  }

  @Test
  public void testServiceAccount_listServiceAccounts() throws IOException, InterruptedException {
    // Prepare
    String serviceAccountName = generateServiceAccountName();
    Util.setUpTest_createServiceAccount(PROJECT_ID, serviceAccountName);

    // Act
    IAMClient.ListServiceAccountsPagedResponse response =
        ListServiceAccounts.listServiceAccounts(PROJECT_ID);

    // Assert
    assertTrue(response.iterateAll().iterator().hasNext());

    // Cleanup
    Util.tearDownTest_deleteServiceAccount(PROJECT_ID, serviceAccountName);
  }

  @Test
  public void testServiceAccount_getServiceAccount() throws IOException, InterruptedException {
    // Prepare
    String serviceAccountName = generateServiceAccountName();
    Util.setUpTest_createServiceAccount(PROJECT_ID, serviceAccountName);

    // Act
    ServiceAccount account = GetServiceAccount.getServiceAccount(PROJECT_ID, serviceAccountName);

    // Assert
    assertTrue(account.getName().contains(serviceAccountName));
    assertEquals(PROJECT_ID, account.getProjectId());

    // Cleanup
    Util.tearDownTest_deleteServiceAccount(PROJECT_ID, serviceAccountName);
  }

  @Test
  public void testServiceAccount_renameServiceAccount() throws IOException, InterruptedException {
    // Prepare
    String serviceAccountName = generateServiceAccountName();
    Util.setUpTest_createServiceAccount(PROJECT_ID, serviceAccountName);
    String newServiceAccountName = "your-new-display-name";

    // Act
    ServiceAccount renamedServiceAccount =
        RenameServiceAccount.renameServiceAccount(
            PROJECT_ID, serviceAccountName, newServiceAccountName);

    // Assert
    String got = bout.toString();
    assertThat(got, containsString("Updated display name"));
    assertThat(got, containsString(newServiceAccountName));
    assertNotNull(renamedServiceAccount);
    assertThat(newServiceAccountName, containsString(renamedServiceAccount.getDisplayName()));

    // Cleanup
    Util.tearDownTest_deleteServiceAccount(PROJECT_ID, serviceAccountName);
  }

  @Test
  public void testServiceAccount_disableServiceAccount() throws IOException, InterruptedException {
    // Prepare
    String serviceAccountName = generateServiceAccountName();
    Util.setUpTest_createServiceAccount(PROJECT_ID, serviceAccountName);

    // Act
    DisableServiceAccount.disableServiceAccount(PROJECT_ID, serviceAccountName);

    // Assert
    ServiceAccount serviceAccount = Util.test_getServiceAccount(PROJECT_ID, serviceAccountName);
    assertTrue(serviceAccount.getName().contains(serviceAccountName));
    assertEquals(PROJECT_ID, serviceAccount.getProjectId());
    assertTrue(serviceAccountName, serviceAccount.getDisabled());

    // Cleanup
    Util.tearDownTest_deleteServiceAccount(PROJECT_ID, serviceAccountName);
  }

  @Test
  public void testServiceAccount_enableServiceAccount() throws IOException, InterruptedException {
    // Prepare
    String serviceAccountName = generateServiceAccountName();
    Util.setUpTest_createServiceAccount(PROJECT_ID, serviceAccountName);
    Util.setUpTest_disableServiceAccount(PROJECT_ID, serviceAccountName);

    // Act
    EnableServiceAccount.enableServiceAccount(PROJECT_ID, serviceAccountName);

    // Assert
    ServiceAccount serviceAccount = Util.test_getServiceAccount(PROJECT_ID, serviceAccountName);
    assertTrue(serviceAccount.getName().contains(serviceAccountName));
    assertEquals(PROJECT_ID, serviceAccount.getProjectId());
    assertFalse(serviceAccountName, serviceAccount.getDisabled());

    // Cleanup
    Util.tearDownTest_deleteServiceAccount(PROJECT_ID, serviceAccountName);
  }

  @Test
  public void testServiceAccount_deleteServiceAccount() throws IOException, InterruptedException {
    // Prepare
    String serviceAccountName = generateServiceAccountName();
    Util.setUpTest_createServiceAccount(PROJECT_ID, serviceAccountName);

    // Act
    DeleteServiceAccount.deleteServiceAccount(PROJECT_ID, serviceAccountName);

    // Assert
    String got = bout.toString();
    assertThat(got, containsString("Deleted service account:"));
    bout.reset();
    Util.test_listServiceAccounts(PROJECT_ID);
    got = bout.toString();
    assertThat(got, !containsString(serviceAccountName).matches(got));
  }

  @Test
  public void testServiceAccount_createKey() throws IOException, InterruptedException {
    // Prepare
    String serviceAccountName = generateServiceAccountName();
    Util.setUpTest_createServiceAccount(PROJECT_ID, serviceAccountName);

    // Act
    ServiceAccountKey key = CreateServiceAccountKey.createKey(PROJECT_ID, serviceAccountName);

    // Assert
    String serviceAccountKeyId = Util.getServiceAccountKeyIdFromKey(key);
    assertNotNull(serviceAccountKeyId);

    // Cleanup
    Util.tearDownTest_deleteServiceAccount(PROJECT_ID, serviceAccountName);
  }

  @Test
  public void testServiceAccount_listKeys() throws IOException, InterruptedException {
    // Prepare
    String serviceAccountName = generateServiceAccountName();
    Util.setUpTest_createServiceAccount(PROJECT_ID, serviceAccountName);
    ServiceAccountKey setupKey =
        Util.setUpTest_createServiceAccountKey(PROJECT_ID, serviceAccountName);
    String serviceAccountKeyId = Util.getServiceAccountKeyIdFromKey(setupKey);

    // Act
    List<ServiceAccountKey> keys = ListServiceAccountKeys.listKeys(PROJECT_ID, serviceAccountName);

    // Assert
    assertFalse(keys.isEmpty());
    assertTrue(keys.size() > 0);
    assertTrue(
        keys.stream()
            .map(ServiceAccountKey::getName)
            .anyMatch(keyName -> keyName.contains(serviceAccountKeyId)));

    // Cleanup
    Util.tearDownTest_deleteServiceAccount(PROJECT_ID, serviceAccountName);
  }

  @Test
  public void testServiceAccount_getKey() throws IOException, InterruptedException {
    // Prepare
    String serviceAccountName = generateServiceAccountName();
    Util.setUpTest_createServiceAccount(PROJECT_ID, serviceAccountName);
    ServiceAccountKey setupKey =
        Util.setUpTest_createServiceAccountKey(PROJECT_ID, serviceAccountName);
    String serviceAccountKeyId = Util.getServiceAccountKeyIdFromKey(setupKey);

    // Act
    ServiceAccountKey key =
        GetServiceAccountKey.getServiceAccountKey(
            PROJECT_ID, serviceAccountName, serviceAccountKeyId);

    // Assert
    assertTrue(key.getName().contains(serviceAccountKeyId));
    assertTrue(key.getName().contains(PROJECT_ID));
    assertTrue(key.getName().contains(serviceAccountName));

    // Cleanup
    Util.tearDownTest_deleteServiceAccount(PROJECT_ID, serviceAccountName);
  }

  @Test
  public void testServiceAccount_disableKey() throws IOException, InterruptedException {
    // Prepare
    String serviceAccountName = generateServiceAccountName();
    Util.setUpTest_createServiceAccount(PROJECT_ID, serviceAccountName);
    ServiceAccountKey setupKey =
        Util.setUpTest_createServiceAccountKey(PROJECT_ID, serviceAccountName);
    String serviceAccountKeyId = Util.getServiceAccountKeyIdFromKey(setupKey);

    // Act
    DisableServiceAccountKey.disableServiceAccountKey(
        PROJECT_ID, serviceAccountName, serviceAccountKeyId);

    // Assert
    ServiceAccountKey key =
        Util.test_getServiceAccountKey(PROJECT_ID, serviceAccountName, serviceAccountKeyId);
    assertTrue(key.getName().contains(serviceAccountKeyId));
    assertTrue(key.getDisabled());

    // Cleanup
    Util.tearDownTest_deleteServiceAccount(PROJECT_ID, serviceAccountName);
  }

  @Test
  public void testServiceAccount_enableKey() throws IOException, InterruptedException {
    // Prepare
    String serviceAccountName = generateServiceAccountName();
    Util.setUpTest_createServiceAccount(PROJECT_ID, serviceAccountName);
    ServiceAccountKey setupKey =
        Util.setUpTest_createServiceAccountKey(PROJECT_ID, serviceAccountName);
    String serviceAccountKeyId = Util.getServiceAccountKeyIdFromKey(setupKey);
    Util.setUpTest_disableServiceAccountKey(PROJECT_ID, serviceAccountName, serviceAccountKeyId);

    // Act
    EnableServiceAccountKey.enableServiceAccountKey(
        PROJECT_ID, serviceAccountName, serviceAccountKeyId);

    // Assert
    ServiceAccountKey key =
        Util.test_getServiceAccountKey(PROJECT_ID, serviceAccountName, serviceAccountKeyId);
    assertTrue(key.getName().contains(serviceAccountKeyId));
    assertFalse(key.getDisabled());

    // Cleanup
    Util.tearDownTest_deleteServiceAccount(PROJECT_ID, serviceAccountName);
  }

  @Test
  public void testServiceAccount_deleteKey() throws IOException, InterruptedException {
    // Prepare
    String serviceAccountName = generateServiceAccountName();
    Util.setUpTest_createServiceAccount(PROJECT_ID, serviceAccountName);
    ServiceAccountKey setupKey =
        Util.setUpTest_createServiceAccountKey(PROJECT_ID, serviceAccountName);
    String serviceAccountKeyId = Util.getServiceAccountKeyIdFromKey(setupKey);

    // Act
    DeleteServiceAccountKey.deleteKey(PROJECT_ID, serviceAccountName, serviceAccountKeyId);

    // Assert
    String got = bout.toString();
    assertThat(got, containsString("Deleted key:"));
    bout.reset();
    Util.test_listServiceAccountKeys(PROJECT_ID, serviceAccountName);
    got = bout.toString();
    assertThat(got, !containsString(serviceAccountKeyId).matches(got));

    // Cleanup
    Util.tearDownTest_deleteServiceAccount(PROJECT_ID, serviceAccountName);
  }
}
