/*
 * Copyright 2020 Google LLC
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

package secretmanager;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;

import com.google.cloud.secretmanager.v1.AddSecretVersionRequest;
import com.google.cloud.secretmanager.v1.CreateSecretRequest;
import com.google.cloud.secretmanager.v1.DeleteSecretRequest;
import com.google.cloud.secretmanager.v1.DisableSecretVersionRequest;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import secretmanager.ConsumeEventNotification.PubSubMessage;

/**
 * Integration (system) tests for {@link Snippets}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class SnippetsIT {

  private static final String IAM_USER =
      "serviceAccount:iam-samples@java-docs-samples-testing.iam.gserviceaccount.com";
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LABEL_KEY = "examplelabelkey";
  private static final String LABEL_VALUE = "examplelabelvalue";
  private static final String UPDATED_LABEL_KEY = "updatedlabelkey";
  private static final String UPDATED_LABEL_VALUE = "updatedlabelvalue";
  private static final String ANNOTATION_KEY = "exampleannotationkey";
  private static final String ANNOTATION_VALUE = "exampleannotationvalue";
  private static final String UPDATED_ANNOTATION_KEY = "updatedannotationkey";
  private static final String UPDATED_ANNOTATION_VALUE = "updatedannotationvalue";

  private static Secret TEST_SECRET;
  private static Secret TEST_SECRET_TO_DELETE;
  private static Secret TEST_SECRET_TO_DELETE_WITH_ETAG;
  private static Secret TEST_SECRET_WITH_VERSIONS;
  private static SecretName TEST_SECRET_TO_CREATE_NAME;
  private static SecretName TEST_SECRET_WITH_LABEL_TO_CREATE_NAME;
  private static SecretName TEST_SECRET_WITH_ANNOTATION_TO_CREATE_NAME;
  private static SecretName TEST_UMMR_SECRET_TO_CREATE_NAME;
  private static SecretVersion TEST_SECRET_VERSION;
  private static SecretVersion TEST_SECRET_VERSION_TO_DESTROY;
  private static SecretVersion TEST_SECRET_VERSION_TO_DESTROY_WITH_ETAG;
  private static SecretVersion TEST_SECRET_VERSION_TO_DISABLE;
  private static SecretVersion TEST_SECRET_VERSION_TO_DISABLE_WITH_ETAG;
  private static SecretVersion TEST_SECRET_VERSION_TO_ENABLE;
  private static SecretVersion TEST_SECRET_VERSION_TO_ENABLE_WITH_ETAG;

  private ByteArrayOutputStream stdOut;

  @BeforeClass
  public static void beforeAll() throws IOException {
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));

    TEST_SECRET = createSecret(true);
    TEST_SECRET_TO_DELETE = createSecret(false);
    TEST_SECRET_TO_DELETE_WITH_ETAG = createSecret(false);
    TEST_SECRET_WITH_VERSIONS = createSecret(false);
    TEST_SECRET_TO_CREATE_NAME = SecretName.of(PROJECT_ID, randomSecretId());
    TEST_UMMR_SECRET_TO_CREATE_NAME = SecretName.of(PROJECT_ID, randomSecretId());
    TEST_SECRET_WITH_LABEL_TO_CREATE_NAME = SecretName.of(PROJECT_ID, randomSecretId());
    TEST_SECRET_WITH_ANNOTATION_TO_CREATE_NAME = SecretName.of(PROJECT_ID, randomSecretId());

    TEST_SECRET_VERSION = addSecretVersion(TEST_SECRET_WITH_VERSIONS);
    TEST_SECRET_VERSION_TO_DESTROY = addSecretVersion(TEST_SECRET_WITH_VERSIONS);
    TEST_SECRET_VERSION_TO_DESTROY_WITH_ETAG = addSecretVersion(TEST_SECRET_WITH_VERSIONS);
    TEST_SECRET_VERSION_TO_DISABLE = addSecretVersion(TEST_SECRET_WITH_VERSIONS);
    TEST_SECRET_VERSION_TO_DISABLE_WITH_ETAG = addSecretVersion(TEST_SECRET_WITH_VERSIONS);
    TEST_SECRET_VERSION_TO_ENABLE = addSecretVersion(TEST_SECRET_WITH_VERSIONS);
    TEST_SECRET_VERSION_TO_ENABLE_WITH_ETAG = addSecretVersion(TEST_SECRET_WITH_VERSIONS);
    disableSecretVersion(TEST_SECRET_VERSION_TO_ENABLE);
    TEST_SECRET_VERSION_TO_ENABLE_WITH_ETAG = disableSecretVersion(
        TEST_SECRET_VERSION_TO_ENABLE_WITH_ETAG);
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

    deleteSecret(TEST_SECRET.getName());
    deleteSecret(TEST_SECRET_TO_CREATE_NAME.toString());
    deleteSecret(TEST_SECRET_WITH_LABEL_TO_CREATE_NAME.toString());
    deleteSecret(TEST_SECRET_WITH_ANNOTATION_TO_CREATE_NAME.toString());
    deleteSecret(TEST_UMMR_SECRET_TO_CREATE_NAME.toString());
    deleteSecret(TEST_SECRET_TO_DELETE.getName());
    deleteSecret(TEST_SECRET_TO_DELETE_WITH_ETAG.getName());
    deleteSecret(TEST_SECRET_WITH_VERSIONS.getName());
  }

  private static String randomSecretId() {
    Random random = new Random();
    return "java-" + random.nextLong();
  }

  private static Secret createSecret(boolean addAnnotation) throws IOException {
    ProjectName parent = ProjectName.of(PROJECT_ID);

    Secret secret;
    if (addAnnotation) {
      secret = Secret.newBuilder()
      .setReplication(
        Replication.newBuilder()
            .setAutomatic(Replication.Automatic.newBuilder().build())
            .build())
      .putLabels(LABEL_KEY, LABEL_VALUE)
      .putAnnotations(ANNOTATION_KEY, ANNOTATION_VALUE)
      .build();
    } else {
      secret = Secret.newBuilder()
      .setReplication(
        Replication.newBuilder()
            .setAutomatic(Replication.Automatic.newBuilder().build())
            .build())
      .putLabels(LABEL_KEY, LABEL_VALUE)
      .build();
    }

    CreateSecretRequest request =
        CreateSecretRequest.newBuilder()
            .setParent(parent.toString())
            .setSecretId(randomSecretId())
            .setSecret(secret)
            .build();

    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      return client.createSecret(request);
    }
  }


  private static SecretVersion addSecretVersion(Secret secret) throws IOException {
    SecretName parent = SecretName.parse(secret.getName());

    AddSecretVersionRequest request =
        AddSecretVersionRequest.newBuilder()
            .setParent(parent.toString())
            .setPayload(
                SecretPayload.newBuilder()
                    .setData(ByteString.copyFromUtf8("my super secret data"))
                    .build())
            .build();

    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      return client.addSecretVersion(request);
    }
  }

  private static void deleteSecret(String secretId) throws IOException {
    DeleteSecretRequest request = DeleteSecretRequest.newBuilder().setName(secretId).build();
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      try {
        client.deleteSecret(request);
      } catch (com.google.api.gax.rpc.NotFoundException e) {
        // Ignore not found error - secret was already deleted
      } catch (io.grpc.StatusRuntimeException e) {
        if (e.getStatus().getCode() != io.grpc.Status.Code.NOT_FOUND) {
          throw e;
        }
      }
    }
  }

  private static SecretVersion disableSecretVersion(SecretVersion version) throws IOException {
    DisableSecretVersionRequest request =
        DisableSecretVersionRequest.newBuilder().setName(version.getName()).build();
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      return client.disableSecretVersion(request);
    }
  }
 
  @Test
  public void testAccessSecretVersion() throws IOException {
    SecretVersionName name = SecretVersionName.parse(TEST_SECRET_VERSION.getName());
    AccessSecretVersion.accessSecretVersion(
        name.getProject(), name.getSecret(), name.getSecretVersion());

    assertThat(stdOut.toString()).contains("my super secret data");
  }
  

  @Test
  public void testAddSecretVersion() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET_WITH_VERSIONS.getName());
    AddSecretVersion.addSecretVersion(name.getProject(), name.getSecret());

    assertThat(stdOut.toString()).contains("Added secret version");
  }

  @Test
  public void testCreateSecret() throws IOException {
    SecretName name = TEST_SECRET_TO_CREATE_NAME;
    CreateSecret.createSecret(name.getProject(), name.getSecret());

    assertThat(stdOut.toString()).contains("Created secret");
  }

  @Test
  public void testCreateSecretWithLabel() throws IOException {
    SecretName name = TEST_SECRET_WITH_LABEL_TO_CREATE_NAME;
    Secret secret = CreateSecretWithLabels.createSecretWithLabels(
        name.getProject(), name.getSecret(), LABEL_KEY, LABEL_VALUE);

    assertThat(secret.getLabelsMap()).containsEntry(LABEL_KEY, LABEL_VALUE);
  }

  @Test
  public void testCreateSecretWithAnnotations() throws IOException {
    SecretName name = TEST_SECRET_WITH_ANNOTATION_TO_CREATE_NAME;
    Secret secret = CreateSecretWithAnnotations.createSecretWithAnnotations(
        name.getProject(), name.getSecret(), ANNOTATION_KEY, ANNOTATION_VALUE);

    assertThat(secret.getAnnotationsMap()).containsEntry(ANNOTATION_KEY, ANNOTATION_VALUE);
  }

  @Test
  public void testCreateSecretWithUserManagedReplication() throws IOException {
    SecretName name = TEST_UMMR_SECRET_TO_CREATE_NAME;
    List<String> locations = Arrays.asList("us-east1", "us-east4", "us-west1");
    CreateSecretWithUserManagedReplication.createSecret(
        name.getProject(), name.getSecret(), locations);

    assertThat(stdOut.toString()).contains("Created secret");
  }

  @Test
  public void testDeleteSecret() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET_TO_DELETE.getName());
    DeleteSecret.deleteSecret(name.getProject(), name.getSecret());

    assertThat(stdOut.toString()).contains("Deleted secret");
  }

  @Test
  public void testDeleteSecretLabel() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET.getName());
    Secret secret = DeleteSecretLabel.deleteSecretLabel(
        name.getProject(), name.getSecret(), LABEL_KEY);

    assertFalse(secret.getLabelsMap().containsKey(LABEL_KEY));
  }

  @Test
  public void testDeleteSecretWithEtag() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET_TO_DELETE_WITH_ETAG.getName());
    String etag = TEST_SECRET_TO_DELETE_WITH_ETAG.getEtag();
    DeleteSecretWithEtag.deleteSecret(name.getProject(), name.getSecret(), etag);

    assertThat(stdOut.toString()).contains("Deleted secret");
  }

  @Test
  public void testDestroySecretVersion() throws IOException {
    SecretVersionName name = SecretVersionName.parse(TEST_SECRET_VERSION_TO_DESTROY.getName());
    DestroySecretVersion.destroySecretVersion(
        name.getProject(), name.getSecret(), name.getSecretVersion());

    assertThat(stdOut.toString()).contains("Destroyed secret version");
  }

  @Test
  public void testDestroySecretVersionWithEtag() throws IOException {
    SecretVersionName name = SecretVersionName.parse(
        TEST_SECRET_VERSION_TO_DESTROY_WITH_ETAG.getName());
    String etag = TEST_SECRET_VERSION_TO_DESTROY_WITH_ETAG.getEtag();
    DestroySecretVersionWithEtag.destroySecretVersion(
        name.getProject(), name.getSecret(), name.getSecretVersion(), etag);

    assertThat(stdOut.toString()).contains("Destroyed secret version");
  }

  @Test
  public void testDisableSecretVersion() throws IOException {
    SecretVersionName name = SecretVersionName.parse(TEST_SECRET_VERSION_TO_DISABLE.getName());
    DisableSecretVersion.disableSecretVersion(
        name.getProject(), name.getSecret(), name.getSecretVersion());

    assertThat(stdOut.toString()).contains("Disabled secret version");
  }

  @Test
  public void testDisableSecretVersionWithEtag() throws IOException {
    SecretVersionName name = SecretVersionName.parse(
        TEST_SECRET_VERSION_TO_DISABLE_WITH_ETAG.getName());
    String etag = TEST_SECRET_VERSION_TO_DISABLE_WITH_ETAG.getEtag();
    DisableSecretVersionWithEtag.disableSecretVersion(
        name.getProject(), name.getSecret(), name.getSecretVersion(), etag);

    assertThat(stdOut.toString()).contains("Disabled secret version");
  }

  @Test
  public void testEnableSecretVersion() throws IOException {
    SecretVersionName name = SecretVersionName.parse(TEST_SECRET_VERSION_TO_ENABLE.getName());
    EnableSecretVersion.enableSecretVersion(
        name.getProject(), name.getSecret(), name.getSecretVersion());

    assertThat(stdOut.toString()).contains("Enabled secret version");
  }

  @Test
  public void testEnableSecretVersionWithEtag() throws IOException {
    SecretVersionName name = SecretVersionName.parse(
        TEST_SECRET_VERSION_TO_ENABLE_WITH_ETAG.getName());
    String etag = TEST_SECRET_VERSION_TO_ENABLE_WITH_ETAG.getEtag();
    EnableSecretVersionWithEtag.enableSecretVersion(
        name.getProject(), name.getSecret(), name.getSecretVersion(), etag);

    assertThat(stdOut.toString()).contains("Enabled secret version");
  }

  @Test
  public void testGetSecretVersion() throws IOException {
    SecretVersionName name = SecretVersionName.parse(TEST_SECRET_VERSION.getName());
    GetSecretVersion.getSecretVersion(
        name.getProject(), name.getSecret(), name.getSecretVersion());

    assertThat(stdOut.toString()).contains("Secret version");
    assertThat(stdOut.toString()).contains("state ENABLED");
  }

  @Test
  public void testGetSecret() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET.getName());
    GetSecret.getSecret(name.getProject(), name.getSecret());

    assertThat(stdOut.toString()).contains("Secret");
    assertThat(stdOut.toString()).contains("replication AUTOMATIC");
  }

  @Test
  public void testViewSecretLabels() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET.getName());
    Map<String, String> labels = 
        ViewSecretLabels.viewSecretLabels(name.getProject(), name.getSecret());

    assertThat(labels).containsEntry(LABEL_KEY, LABEL_VALUE);
  }

  @Test
  public void testViewSecretAnnotations() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET.getName());
    Map<String, String> annotations = 
        ViewSecretAnnotations.viewSecretAnnotations(name.getProject(), name.getSecret());

    assertThat(annotations).containsEntry(ANNOTATION_KEY, ANNOTATION_VALUE);
  }


  @Test
  public void testIamGrantAccess() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET.getName());
    IamGrantAccess.iamGrantAccess(name.getProject(), name.getSecret(), IAM_USER);

    assertThat(stdOut.toString()).contains("Updated IAM policy");
  }

  @Test
  public void testIamRevokeAccess() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET.getName());
    IamRevokeAccess.iamRevokeAccess(name.getProject(), name.getSecret(), IAM_USER);

    assertThat(stdOut.toString()).contains("Updated IAM policy");
  }

  @Test
  public void testListSecretVersions() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET_WITH_VERSIONS.getName());
    ListSecretVersions.listSecretVersions(name.getProject(), name.getSecret());

    assertThat(stdOut.toString()).contains("Secret version");
  }

  @Test
  public void testListSecretVersionsWithFilter() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET_WITH_VERSIONS.getName());
    ListSecretVersionsWithFilter.listSecretVersions(
        name.getProject(), name.getSecret(), "name:1");

    assertThat(stdOut.toString()).contains("Secret version");
  }
 
  @Test
  public void testListSecrets() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET.getName());
    ListSecrets.listSecrets(name.getProject());

    assertThat(stdOut.toString()).contains("Secret projects/");
    assertThat(stdOut.toString()).contains(name.getSecret());
  }

  @Test
  public void testListSecretsWithFilter() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET.getName());
    ListSecretsWithFilter.listSecrets(
        name.getProject(), String.format("name:%s", name.getSecret()));

    assertThat(stdOut.toString()).contains("Secret projects/");
    assertThat(stdOut.toString()).contains(name.getSecret());
  }

  @Test
  public void testUpdateSecret() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET.getName());
    UpdateSecret.updateSecret(name.getProject(), name.getSecret());

    assertThat(stdOut.toString()).contains("Updated secret");
  }

  @Test
  public void testCreateUpdateSecretLabel() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET.getName());
    Secret updatedSecret = CreateUpdateSecretLabel.createUpdateSecretLabel(
        name.getProject(), name.getSecret(), UPDATED_LABEL_KEY, UPDATED_LABEL_VALUE);

    assertThat(updatedSecret.getLabelsMap()).containsEntry(
        UPDATED_LABEL_KEY, UPDATED_LABEL_VALUE);
  }

  @Test
  public void testEditSecretAnnotations() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET.getName());
    Secret updatedSecret = EditSecretAnnotations.editSecretAnnotations(
        name.getProject(), name.getSecret(), UPDATED_ANNOTATION_KEY, UPDATED_ANNOTATION_VALUE);

    assertThat(updatedSecret.getAnnotationsMap()).containsEntry(
        UPDATED_ANNOTATION_KEY, UPDATED_ANNOTATION_VALUE);
  }

  @Test
  public void testUpdateSecretWithAlias() throws IOException {
    SecretName name = SecretName.parse(TEST_SECRET_WITH_VERSIONS.getName());
    UpdateSecretWithAlias.updateSecret(name.getProject(), name.getSecret());

    assertThat(stdOut.toString()).contains("test");
  }

  @Test
  public void testConsumeEventNotification() {
    String message = "hello!";
    byte[] base64Bytes = Base64.getEncoder().encode(message.getBytes(StandardCharsets.UTF_8));
    Map<String, String> attributes = new HashMap<>();
    attributes.put("eventType", "SECRET_UPDATE");
    attributes.put("secretId", "projects/p/secrets/s");

    PubSubMessage pubSubMessage = new PubSubMessage();
    pubSubMessage.setData(base64Bytes);
    pubSubMessage.setAttributes(attributes);

    String log = ConsumeEventNotification.accept(pubSubMessage);
    assertThat(log).isEqualTo(
        "Received SECRET_UPDATE for projects/p/secrets/s. New metadata: hello!");
  }
        
}
