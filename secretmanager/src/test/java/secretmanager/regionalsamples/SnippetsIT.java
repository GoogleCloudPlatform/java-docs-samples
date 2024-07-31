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

package secretmanager.regionalsamples;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.secretmanager.v1.AddSecretVersionRequest;
import com.google.cloud.secretmanager.v1.CreateSecretRequest;
import com.google.cloud.secretmanager.v1.DeleteSecretRequest;
import com.google.cloud.secretmanager.v1.DisableSecretVersionRequest;
import com.google.cloud.secretmanager.v1.LocationName;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient.ListSecretVersionsPage;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient.ListSecretVersionsPagedResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient.ListSecretsPage;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient.ListSecretsPagedResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.SecretVersion.State;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.common.base.Strings;
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Integration (system) tests for {@link Snippets}.
*/
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class SnippetsIT {

  private static final String IAM_USER =
      "serviceAccount:iam-samples@java-docs-samples-testing.iam.gserviceaccount.com";
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION_ID = "us-central1";
  private static final String REGIONAL_ENDPOINT = 
      String.format("secretmanager.%s.rep.googleapis.com:443", LOCATION_ID);

  private static Secret TEST_REGIONAL_SECRET;
  private static Secret TEST_REGIONAL_SECRET_TO_DELETE;
  private static Secret TEST_REGIONAL_SECRET_TO_DELETE_WITH_ETAG;
  private static Secret TEST_REGIONAL_SECRET_WITH_VERSIONS;
  private static SecretName TEST_REGIONAL_SECRET_TO_CREATE_NAME;
  private static SecretVersion TEST_REGIONAL_SECRET_VERSION;
  private static SecretVersion TEST_REGIONAL_SECRET_VERSION_TO_DESTROY;
  private static SecretVersion TEST_REGIONAL_SECRET_VERSION_TO_DESTROY_WITH_ETAG;
  private static SecretVersion TEST_REGIONAL_SECRET_VERSION_TO_DISABLE;
  private static SecretVersion TEST_REGIONAL_SECRET_VERSION_TO_DISABLE_WITH_ETAG;
  private static SecretVersion TEST_REGIONAL_SECRET_VERSION_TO_ENABLE;
  private static SecretVersion TEST_REGIONAL_SECRET_VERSION_TO_ENABLE_WITH_ETAG;

  private ByteArrayOutputStream stdOut;

  @BeforeClass
  public static void beforeAll() throws IOException {
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT_LOCATION",
        Strings.isNullOrEmpty(LOCATION_ID));

    TEST_REGIONAL_SECRET = createRegionalSecret();
    TEST_REGIONAL_SECRET_TO_DELETE = createRegionalSecret();
    TEST_REGIONAL_SECRET_TO_DELETE_WITH_ETAG = createRegionalSecret();
    TEST_REGIONAL_SECRET_WITH_VERSIONS = createRegionalSecret();
    TEST_REGIONAL_SECRET_TO_CREATE_NAME = 
        SecretName.ofProjectLocationSecretName(PROJECT_ID, LOCATION_ID, randomSecretId());

    TEST_REGIONAL_SECRET_VERSION = addRegionalSecretVersion(TEST_REGIONAL_SECRET_WITH_VERSIONS);
    TEST_REGIONAL_SECRET_VERSION_TO_DESTROY = 
        addRegionalSecretVersion(TEST_REGIONAL_SECRET_WITH_VERSIONS);
    TEST_REGIONAL_SECRET_VERSION_TO_DESTROY_WITH_ETAG = 
        addRegionalSecretVersion(TEST_REGIONAL_SECRET_WITH_VERSIONS);
    TEST_REGIONAL_SECRET_VERSION_TO_DISABLE = 
        addRegionalSecretVersion(TEST_REGIONAL_SECRET_WITH_VERSIONS);
    TEST_REGIONAL_SECRET_VERSION_TO_DISABLE_WITH_ETAG = 
        addRegionalSecretVersion(TEST_REGIONAL_SECRET_WITH_VERSIONS);
    TEST_REGIONAL_SECRET_VERSION_TO_ENABLE = 
        addRegionalSecretVersion(TEST_REGIONAL_SECRET_WITH_VERSIONS);
    TEST_REGIONAL_SECRET_VERSION_TO_ENABLE_WITH_ETAG = 
        addRegionalSecretVersion(TEST_REGIONAL_SECRET_WITH_VERSIONS);
    disableRegionalSecretVersion(TEST_REGIONAL_SECRET_VERSION_TO_ENABLE);
    TEST_REGIONAL_SECRET_VERSION_TO_ENABLE_WITH_ETAG = disableRegionalSecretVersion(
    TEST_REGIONAL_SECRET_VERSION_TO_ENABLE_WITH_ETAG);
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
  public static void afterAll() throws IOException {
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));

    deleteRegionalSecret(TEST_REGIONAL_SECRET.getName());
    deleteRegionalSecret(TEST_REGIONAL_SECRET_TO_CREATE_NAME.toString());
    deleteRegionalSecret(TEST_REGIONAL_SECRET_TO_DELETE.getName());
    deleteRegionalSecret(TEST_REGIONAL_SECRET_TO_DELETE_WITH_ETAG.getName());
    deleteRegionalSecret(TEST_REGIONAL_SECRET_WITH_VERSIONS.getName());
  }

  private static String randomSecretId() {
    Random random = new Random();
    return "test-drz-" + random.nextLong();
  }

  private static Secret createRegionalSecret() throws IOException {
    LocationName parent = LocationName.of(PROJECT_ID, LOCATION_ID);

    CreateSecretRequest request =
        CreateSecretRequest.newBuilder()
            .setParent(parent.toString())
            .setSecretId(randomSecretId())
            .build();

    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(REGIONAL_ENDPOINT).build();
    try (SecretManagerServiceClient client = 
          SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      return client.createSecret(request);
    }
  }

  private static SecretVersion addRegionalSecretVersion(Secret secret) throws IOException {
    SecretName parent = SecretName.parse(secret.getName());

    AddSecretVersionRequest request =
        AddSecretVersionRequest.newBuilder()
        .setParent(parent.toString())
        .setPayload(
          SecretPayload.newBuilder()
            .setData(ByteString.copyFromUtf8("my super secret data"))
            .build())
        .build();

    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(REGIONAL_ENDPOINT).build();
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      return client.addSecretVersion(request);
    }
  }

  private static void deleteRegionalSecret(String secretId) throws IOException {
    DeleteSecretRequest request = DeleteSecretRequest.newBuilder().setName(secretId).build();
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(REGIONAL_ENDPOINT).build();
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      try {
        client.deleteSecret(request);
      } catch (NotFoundException e) {
        // Ignore not found error - secret was already deleted
      } catch (io.grpc.StatusRuntimeException e) {
        if (e.getStatus().getCode() != io.grpc.Status.Code.NOT_FOUND) {
          throw e;
        }
      }
    }
  }

  private static SecretVersion disableRegionalSecretVersion(
      SecretVersion version) throws IOException {
    DisableSecretVersionRequest request =
        DisableSecretVersionRequest.newBuilder().setName(version.getName()).build();
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(REGIONAL_ENDPOINT).build();
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      return client.disableSecretVersion(request);
    }
  }

  @Test
  public void testAccessRegionalSecretVersion() throws Exception {
    SecretVersionName name = SecretVersionName.parse(TEST_REGIONAL_SECRET_VERSION.getName());
    SecretPayload secretPayload = AccessRegionalSecretVersion.accessRegionalSecretVersion(
        name.getProject(), name.getLocation(), name.getSecret(), name.getSecretVersion());

    assertEquals("my super secret data", secretPayload.getData().toStringUtf8());
  }

  @Test
  public void testAddRegionalSecretVersion() throws IOException {
    SecretName name = SecretName.parse(TEST_REGIONAL_SECRET_WITH_VERSIONS.getName());
    SecretVersion secretVersion = AddRegionalSecretVersion.addRegionalSecretVersion(
        name.getProject(), name.getLocation(), name.getSecret());
    SecretVersionName secretVersionName = SecretVersionName.parse(secretVersion.getName());
    
    assertEquals(TEST_REGIONAL_SECRET_WITH_VERSIONS.getName(), 
        SecretName.ofProjectLocationSecretName(
          secretVersionName.getProject(), 
          secretVersionName.getLocation(), 
          secretVersionName.getSecret()).toString());
  }

  @Test
  public void testCreateRegionalSecret() throws IOException {
    SecretName name = TEST_REGIONAL_SECRET_TO_CREATE_NAME;
    Secret secret = CreateRegionalSecret.createRegionalSecret(
        name.getProject(), name.getLocation(), name.getSecret());
    SecretName createdSecretName = SecretName.parse(secret.getName());
    assertEquals(name.getSecret(), createdSecretName.getSecret());
  }

  @Test
  public void testDeleteRegionalSecret() throws IOException {
    SecretName name = SecretName.parse(TEST_REGIONAL_SECRET_TO_DELETE.getName());
    DeleteRegionalSecret.deleteRegionalSecret(
        name.getProject(), name.getLocation(), name.getSecret());

    DeleteSecretRequest request = 
        DeleteSecretRequest.newBuilder()
        .setName(TEST_REGIONAL_SECRET_TO_DELETE.getName()).build();

    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(REGIONAL_ENDPOINT).build();
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      assertThrows(
          NotFoundException.class,
          () -> client.deleteSecret(request));
    }
  }

  @Test
  public void testDeleteRegionalSecretWithEtag() throws IOException {
    SecretName name = SecretName.parse(TEST_REGIONAL_SECRET_TO_DELETE_WITH_ETAG.getName());
    String etag = TEST_REGIONAL_SECRET_TO_DELETE_WITH_ETAG.getEtag();
    DeleteRegionalSecretWithEtag.deleteRegionalSecretWithEtag(
        name.getProject(), name.getLocation(), name.getSecret(), etag);
        
    DeleteSecretRequest request = 
        DeleteSecretRequest.newBuilder()
        .setName(TEST_REGIONAL_SECRET_TO_DELETE_WITH_ETAG.getName()).build();

    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(REGIONAL_ENDPOINT).build();
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      assertThrows(
          NotFoundException.class,
          () -> client.deleteSecret(request));
    }
  }

  @Test
  public void testDestroyRegionalSecretVersion() throws IOException {
    SecretVersionName name = SecretVersionName.parse(
        TEST_REGIONAL_SECRET_VERSION_TO_DESTROY.getName());
    SecretVersion version = DestroyRegionalSecretVersion.destroyRegionalSecretVersion(
        name.getProject(), name.getLocation(), name.getSecret(), name.getSecretVersion());

    assertEquals(State.DESTROYED, version.getState());
  }

  @Test
  public void testDestroyRegionalSecretVersionWithEtag() throws IOException {
    SecretVersionName name = SecretVersionName.parse(
        TEST_REGIONAL_SECRET_VERSION_TO_DESTROY_WITH_ETAG.getName());
    String etag = TEST_REGIONAL_SECRET_VERSION_TO_DESTROY_WITH_ETAG.getEtag();
    SecretVersion version = 
        DestroyRegionalSecretVersionWithEtag.destroyRegionalSecretVersionWithEtag(
        name.getProject(), name.getLocation(), name.getSecret(), name.getSecretVersion(), etag);

    assertEquals(State.DESTROYED, version.getState());
  }

  @Test
  public void testDisableRegionalSecretVersion() throws IOException {
    SecretVersionName name = SecretVersionName.parse(
        TEST_REGIONAL_SECRET_VERSION_TO_DISABLE.getName());
    SecretVersion version = DisableRegionalSecretVersion.disableRegionalSecretVersion(
        name.getProject(), name.getLocation(), name.getSecret(), name.getSecretVersion());

    assertEquals(State.DISABLED, version.getState());
  }

  @Test
  public void testDisableRegionalSecretVersionWithEtag() throws IOException {
    SecretVersionName name = SecretVersionName.parse(
        TEST_REGIONAL_SECRET_VERSION_TO_DISABLE_WITH_ETAG.getName());
    String etag = TEST_REGIONAL_SECRET_VERSION_TO_DISABLE_WITH_ETAG.getEtag();
    SecretVersion version = 
        DisableRegionalSecretVersionWithEtag.disableRegionalSecretVersionWithEtag(
        name.getProject(), name.getLocation(), name.getSecret(), name.getSecretVersion(), etag);

    assertEquals(State.DISABLED, version.getState());
  }

  @Test
  public void testEnableRegionalSecretVersion() throws IOException {
    SecretVersionName name = 
        SecretVersionName.parse(TEST_REGIONAL_SECRET_VERSION_TO_ENABLE.getName());
    SecretVersion version = EnableRegionalSecretVersion.enableRegionalSecretVersion(
        name.getProject(), name.getLocation(), name.getSecret(), name.getSecretVersion());

    assertEquals(State.ENABLED, version.getState());
  }

  @Test
  public void testEnableRegionalSecretVersionWithEtag() throws IOException {
    SecretVersionName name = SecretVersionName.parse(
        TEST_REGIONAL_SECRET_VERSION_TO_ENABLE_WITH_ETAG.getName());
    String etag = TEST_REGIONAL_SECRET_VERSION_TO_ENABLE_WITH_ETAG.getEtag();
    SecretVersion version = 
        EnableRegionalSecretVersionWithEtag.enableRegionalSecretVersionWithEtag(
        name.getProject(), name.getLocation(), name.getSecret(), name.getSecretVersion(), etag);

    assertEquals(State.ENABLED, version.getState());
  }

  @Test
  public void testGetRegionalSecretVersion() throws IOException {
    SecretVersionName name = SecretVersionName.parse(TEST_REGIONAL_SECRET_VERSION.getName());
    SecretVersion version = GetRegionalSecretVersion.getRegionalSecretVersion(
        name.getProject(), name.getLocation(), name.getSecret(), name.getSecretVersion());

    assertEquals(TEST_REGIONAL_SECRET_VERSION.getName(), version.getName());
  }

  @Test
  public void testGetRegionalSecret() throws IOException {
    SecretName name = SecretName.parse(TEST_REGIONAL_SECRET.getName());
    Secret secret = GetRegionalSecret.getRegionalSecret(
        name.getProject(), name.getLocation(), name.getSecret());

    assertEquals(TEST_REGIONAL_SECRET.getName(), secret.getName());
  }

  @Test
  public void testIamGrantAccessWithRegionalSecret() throws IOException {
    SecretName name = SecretName.parse(TEST_REGIONAL_SECRET.getName());
    Policy updatedPolicy = IamGrantAccessWithRegionalSecret.iamGrantAccessWithRegionalSecret(
        name.getProject(), name.getLocation(), name.getSecret(), IAM_USER);

    Binding bindingForSecretAccesorRole = null;
    String roleToFind = "roles/secretmanager.secretAccessor";
    for (Binding binding : updatedPolicy.getBindingsList()) {
      if (binding.getRole().equals(roleToFind)) {
        bindingForSecretAccesorRole = binding;
      }
    }
    assertThat(bindingForSecretAccesorRole.getMembersList()).contains(IAM_USER);
  }

  @Test
  public void testIamRevokeAccessWithRegionalSecret() throws IOException {
    SecretName name = SecretName.parse(TEST_REGIONAL_SECRET.getName());
    Policy updatedPolicy = IamRevokeAccessWithRegionalSecret.iamRevokeAccessWithRegionalSecret(
        name.getProject(), name.getLocation(), name.getSecret(), IAM_USER);

    assertEquals(updatedPolicy.getBindingsList().size(), 0);
  }

  @Test
  public void testListRegionalSecretVersions() throws IOException {
    SecretName name = SecretName.parse(TEST_REGIONAL_SECRET_WITH_VERSIONS.getName());
    ListSecretVersionsPagedResponse listSecreVersionsPage = 
        ListRegionalSecretVersions.listRegionalSecretVersions(
        name.getProject(), name.getLocation(), name.getSecret());

    boolean secretPresentInList = false;
    for (SecretVersion secretVersion : listSecreVersionsPage.iterateAll()) {
      SecretVersionName secretVersionName = SecretVersionName.parse(
          TEST_REGIONAL_SECRET_WITH_VERSIONS.getName() + "/versions/1");
      if (secretVersionName.toString().equals(secretVersion.getName().toString())) {
        secretPresentInList = true;
      }
    }
    assertTrue(secretPresentInList);
  }

  @Test
  public void testListRegionalSecretVersionsWithFilter() throws IOException {
    SecretName name = SecretName.parse(TEST_REGIONAL_SECRET_WITH_VERSIONS.getName());
    ListSecretVersionsPage listSecreVersionsPage = 
        ListRegionalSecretVersionsWithFilter.listRegionalSecretVersionsWithFilter(
        name.getProject(), name.getLocation(), name.getSecret(), "name:1");

    boolean secretPresentInList = false;
    for (SecretVersion secretVersion : listSecreVersionsPage.iterateAll()) {
      SecretVersionName secretVersionName = SecretVersionName.parse(
          TEST_REGIONAL_SECRET_WITH_VERSIONS.getName() + "/versions/1");
      if (secretVersionName.toString().equals(secretVersion.getName().toString())) {
        secretPresentInList = true;
      }
    }
    assertTrue(secretPresentInList);
  }

  @Test
  public void testListRegionalSecrets() throws IOException {
    SecretName name = SecretName.parse(TEST_REGIONAL_SECRET.getName());
    ListSecretsPagedResponse listSecretsPage = 
        ListRegionalSecrets.listRegionalSecrets(name.getProject(), name.getLocation());

    boolean secretPresentInList = false;
    for (Secret secret : listSecretsPage.iterateAll()) {
      if (TEST_REGIONAL_SECRET_WITH_VERSIONS.getName().equals(secret.getName())) {
        secretPresentInList = true;
      }
    }
    assertTrue(secretPresentInList);
  }

  @Test
  public void testListRegionalSecretsWithFilter() throws IOException {
    SecretName name = SecretName.parse(TEST_REGIONAL_SECRET.getName());
    ListSecretsPage listSecretsPage = ListRegionalSecretsWithFilter.listRegionalSecretsWithFilter(
        name.getProject(), name.getLocation(), String.format("name:%s", name.getSecret()));

    boolean secretPresentInList = false;
    for (Secret secret : listSecretsPage.getValues()) {
      if (TEST_REGIONAL_SECRET.getName().equals(secret.getName())) {
        secretPresentInList = true;
      }
    }
    assertTrue(secretPresentInList);
  }

  @Test
  public void testUpdateRegionalSecret() throws IOException {
    SecretName name = SecretName.parse(TEST_REGIONAL_SECRET.getName());
    Secret updatedSecret = UpdateRegionalSecret.updateRegionalSecret(
        name.getProject(), name.getLocation(), name.getSecret());

    assertEquals("rocks", updatedSecret.getLabelsMap().get("secretmanager"));
  }

  @Test
  public void testUpdateRegionalSecretWithAlias() throws IOException {
    SecretName name = SecretName.parse(TEST_REGIONAL_SECRET_WITH_VERSIONS.getName());
    Secret updatedSecret = UpdateRegionalSecretWithAlias.updateRegionalSecretWithAlias(
        name.getProject(), name.getLocation(), name.getSecret());

    assertEquals(1L, (long) updatedSecret.getVersionAliasesMap().get("test"));
  }
}
 