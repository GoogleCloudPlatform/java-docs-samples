/*
 * Copyright 2025 Google LLC
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

package parametermanager;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.parametermanager.v1.LocationName;
import com.google.cloud.parametermanager.v1.Parameter;
import com.google.cloud.parametermanager.v1.ParameterFormat;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterName;
import com.google.cloud.parametermanager.v1.ParameterVersion;
import com.google.cloud.parametermanager.v1.ParameterVersionName;
import com.google.cloud.parametermanager.v1.ParameterVersionPayload;
import com.google.cloud.secretmanager.v1.AddSecretVersionRequest;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.common.base.Strings;
import com.google.iam.v1.Binding;
import com.google.iam.v1.GetIamPolicyRequest;
import com.google.iam.v1.Policy;
import com.google.iam.v1.SetIamPolicyRequest;
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

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class SnippetsIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String PAYLOAD = "test123";
  private static final String JSON_PAYLOAD =
      "{\"username\": \"test-user\", \"host\": \"localhost\"}";
  private static final String SECRET_ID = "projects/project-id/secrets/secret-id/versions/latest";
  private static ParameterName TEST_PARAMETER_NAME;
  private static ParameterName TEST_PARAMETER_NAME_WITH_FORMAT;
  private static ParameterName TEST_PARAMETER_NAME_FOR_VERSION;
  private static ParameterVersionName TEST_PARAMETER_VERSION_NAME;
  private static ParameterName TEST_PARAMETER_NAME_FOR_VERSION_WITH_FORMAT;
  private static ParameterVersionName TEST_PARAMETER_VERSION_NAME_WITH_FORMAT;
  private static ParameterVersionName TEST_PARAMETER_VERSION_NAME_WITH_SECRET_REFERENCE;
  private static ParameterName TEST_PARAMETER_NAME_TO_DELETE;
  private static ParameterName TEST_PARAMETER_NAME_TO_DELETE_VERSION;
  private static ParameterVersionName TEST_PARAMETER_VERSION_NAME_TO_DELETE;
  private static ParameterName TEST_PARAMETER_NAME_TO_GET;
  private static ParameterVersionName TEST_PARAMETER_VERSION_NAME_TO_GET;
  private static ParameterVersionName TEST_PARAMETER_VERSION_NAME_TO_GET_1;
  private static ParameterName TEST_PARAMETER_NAME_TO_RENDER;
  private static ParameterVersionName TEST_PARAMETER_VERSION_NAME_TO_RENDER;
  private static SecretName SECRET_NAME;
  private ByteArrayOutputStream stdOut;

  @BeforeClass
  public static void beforeAll() throws IOException {
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));

    // test create parameter
    TEST_PARAMETER_NAME = ParameterName.of(PROJECT_ID, "global", randomId());
    TEST_PARAMETER_NAME_WITH_FORMAT = ParameterName.of(PROJECT_ID, "global", randomId());

    // test create parameter version with unformatted format
    TEST_PARAMETER_NAME_FOR_VERSION = ParameterName.of(PROJECT_ID, "global", randomId());
    createParameter(TEST_PARAMETER_NAME_FOR_VERSION.getParameter(), ParameterFormat.UNFORMATTED);
    TEST_PARAMETER_VERSION_NAME =
        ParameterVersionName.of(
            PROJECT_ID, "global", TEST_PARAMETER_NAME_FOR_VERSION.getParameter(), randomId());

    // test create parameter version with json format
    TEST_PARAMETER_NAME_FOR_VERSION_WITH_FORMAT =
        ParameterName.of(PROJECT_ID, "global", randomId());
    createParameter(
        TEST_PARAMETER_NAME_FOR_VERSION_WITH_FORMAT.getParameter(), ParameterFormat.JSON);
    TEST_PARAMETER_VERSION_NAME_WITH_FORMAT =
        ParameterVersionName.of(
            PROJECT_ID,
            "global",
            TEST_PARAMETER_NAME_FOR_VERSION_WITH_FORMAT.getParameter(),
            randomId());
    TEST_PARAMETER_VERSION_NAME_WITH_SECRET_REFERENCE =
        ParameterVersionName.of(
            PROJECT_ID,
            "global",
            TEST_PARAMETER_NAME_FOR_VERSION_WITH_FORMAT.getParameter(),
            randomId());

    // test delete parameter
    TEST_PARAMETER_NAME_TO_DELETE = ParameterName.of(PROJECT_ID, "global", randomId());
    createParameter(TEST_PARAMETER_NAME_TO_DELETE.getParameter(), ParameterFormat.JSON);

    // test delete parameter version
    TEST_PARAMETER_NAME_TO_DELETE_VERSION = ParameterName.of(PROJECT_ID, "global", randomId());
    createParameter(TEST_PARAMETER_NAME_TO_DELETE_VERSION.getParameter(), ParameterFormat.JSON);
    TEST_PARAMETER_VERSION_NAME_TO_DELETE =
        ParameterVersionName.of(
            PROJECT_ID, "global", TEST_PARAMETER_NAME_TO_DELETE_VERSION.getParameter(), randomId());
    createParameterVersion(
        TEST_PARAMETER_VERSION_NAME_TO_DELETE.getParameter(),
        TEST_PARAMETER_VERSION_NAME_TO_DELETE.getParameterVersion(),
        JSON_PAYLOAD);

    // test get, list parameter and parameter version, enable/disable parameter version
    TEST_PARAMETER_NAME_TO_GET = ParameterName.of(PROJECT_ID, "global", randomId());
    createParameter(TEST_PARAMETER_NAME_TO_GET.getParameter(), ParameterFormat.JSON);
    TEST_PARAMETER_VERSION_NAME_TO_GET =
        ParameterVersionName.of(
            PROJECT_ID, "global", TEST_PARAMETER_NAME_TO_GET.getParameter(), randomId());
    createParameterVersion(
        TEST_PARAMETER_VERSION_NAME_TO_GET.getParameter(),
        TEST_PARAMETER_VERSION_NAME_TO_GET.getParameterVersion(),
        JSON_PAYLOAD);
    TEST_PARAMETER_VERSION_NAME_TO_GET_1 =
        ParameterVersionName.of(
            PROJECT_ID, "global", TEST_PARAMETER_NAME_TO_GET.getParameter(), randomId());
    createParameterVersion(
        TEST_PARAMETER_VERSION_NAME_TO_GET_1.getParameter(),
        TEST_PARAMETER_VERSION_NAME_TO_GET_1.getParameterVersion(),
        JSON_PAYLOAD);

    // test render parameter version
    TEST_PARAMETER_NAME_TO_RENDER = ParameterName.of(PROJECT_ID, "global", randomId());
    SECRET_NAME = SecretName.of(PROJECT_ID, randomId());
    Secret secret = createSecret(SECRET_NAME.getSecret());
    addSecretVersion(secret);
    Parameter testParameter =
        createParameter(TEST_PARAMETER_NAME_TO_RENDER.getParameter(), ParameterFormat.JSON);
    iamGrantAccess(SECRET_NAME, testParameter.getPolicyMember().getIamPolicyUidPrincipal());
    TEST_PARAMETER_VERSION_NAME_TO_RENDER =
        ParameterVersionName.of(
            PROJECT_ID, "global", TEST_PARAMETER_NAME_TO_RENDER.getParameter(), randomId());
    String payload =
        String.format(
            "{\"username\": \"test-user\","
                + "\"password\": \"__REF__(//secretmanager.googleapis.com/%s/versions/latest)\"}",
            SECRET_NAME.toString());
    createParameterVersion(
        TEST_PARAMETER_VERSION_NAME_TO_RENDER.getParameter(),
        TEST_PARAMETER_VERSION_NAME_TO_RENDER.getParameterVersion(),
        payload);
  }

  @AfterClass
  public static void afterAll() throws IOException {
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));

    deleteParameter(TEST_PARAMETER_NAME.toString());
    deleteParameter(TEST_PARAMETER_NAME_WITH_FORMAT.toString());

    deleteParameterVersion(TEST_PARAMETER_VERSION_NAME_WITH_FORMAT.toString());
    deleteParameterVersion(TEST_PARAMETER_VERSION_NAME_WITH_SECRET_REFERENCE.toString());
    deleteParameter(TEST_PARAMETER_NAME_FOR_VERSION_WITH_FORMAT.toString());

    deleteParameterVersion(TEST_PARAMETER_VERSION_NAME.toString());
    deleteParameter(TEST_PARAMETER_NAME_FOR_VERSION.toString());

    deleteParameterVersion(TEST_PARAMETER_VERSION_NAME_TO_DELETE.toString());
    deleteParameter(TEST_PARAMETER_NAME_TO_DELETE_VERSION.toString());
    deleteParameter(TEST_PARAMETER_NAME_TO_DELETE.toString());

    deleteParameterVersion(TEST_PARAMETER_VERSION_NAME_TO_RENDER.toString());
    deleteParameter(TEST_PARAMETER_NAME_TO_RENDER.toString());
    deleteSecret(SECRET_NAME.toString());

    deleteParameterVersion(TEST_PARAMETER_VERSION_NAME_TO_GET.toString());
    deleteParameterVersion(TEST_PARAMETER_VERSION_NAME_TO_GET_1.toString());
    deleteParameter(TEST_PARAMETER_NAME_TO_GET.toString());
  }

  private static String randomId() {
    Random random = new Random();
    return "java-" + random.nextLong();
  }

  private static Parameter createParameter(String parameterId, ParameterFormat format)
      throws IOException {
    LocationName parent = LocationName.of(PROJECT_ID, "global");
    Parameter parameter = Parameter.newBuilder().setFormat(format).build();

    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      return client.createParameter(parent.toString(), parameter, parameterId);
    }
  }

  private static void createParameterVersion(String parameterId, String versionId, String payload)
      throws IOException {
    ParameterName parameterName = ParameterName.of(PROJECT_ID, "global", parameterId);
    // Convert the payload string to ByteString.
    ByteString byteStringPayload = ByteString.copyFromUtf8(payload);

    // Create the parameter version payload.
    ParameterVersionPayload parameterVersionPayload =
        ParameterVersionPayload.newBuilder().setData(byteStringPayload).build();

    // Create the parameter version with the unformatted payload.
    ParameterVersion parameterVersion =
        ParameterVersion.newBuilder().setPayload(parameterVersionPayload).build();

    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      client.createParameterVersion(parameterName.toString(), parameterVersion, versionId);
    }
  }

  private static void deleteParameter(String name) throws IOException {
    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      client.deleteParameter(name);
    } catch (com.google.api.gax.rpc.NotFoundException e) {
      // Ignore not found error - parameter was already deleted
    } catch (io.grpc.StatusRuntimeException e) {
      if (e.getStatus().getCode() != io.grpc.Status.Code.NOT_FOUND) {
        throw e;
      }
    }
  }

  private static void deleteParameterVersion(String name) throws IOException {
    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      client.deleteParameterVersion(name);
    } catch (com.google.api.gax.rpc.NotFoundException e) {
      // Ignore not found error - parameter version was already deleted
    } catch (io.grpc.StatusRuntimeException e) {
      if (e.getStatus().getCode() != io.grpc.Status.Code.NOT_FOUND) {
        throw e;
      }
    }
  }

  private static Secret createSecret(String secretId) throws IOException {
    ProjectName projectName = ProjectName.of(PROJECT_ID);
    Secret secret =
        Secret.newBuilder()
            .setReplication(
                Replication.newBuilder()
                    .setAutomatic(Replication.Automatic.newBuilder().build())
                    .build())
            .build();

    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      return client.createSecret(projectName.toString(), secretId, secret);
    }
  }

  private static void addSecretVersion(Secret secret) throws IOException {
    SecretName parent = SecretName.parse(secret.getName());
    AddSecretVersionRequest request =
        AddSecretVersionRequest.newBuilder()
            .setParent(parent.toString())
            .setPayload(
                SecretPayload.newBuilder().setData(ByteString.copyFromUtf8(PAYLOAD)).build())
            .build();
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      client.addSecretVersion(request);
    }
  }

  private static void deleteSecret(String name) throws IOException {
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      client.deleteSecret(name);
    } catch (com.google.api.gax.rpc.NotFoundException e) {
      // Ignore not found error - parameter was already deleted
    } catch (io.grpc.StatusRuntimeException e) {
      if (e.getStatus().getCode() != io.grpc.Status.Code.NOT_FOUND) {
        throw e;
      }
    }
  }

  private static void iamGrantAccess(SecretName secretName, String member) throws IOException {
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      Policy currentPolicy =
          client.getIamPolicy(
              GetIamPolicyRequest.newBuilder().setResource(secretName.toString()).build());

      Binding binding =
          Binding.newBuilder()
              .setRole("roles/secretmanager.secretAccessor")
              .addMembers(member)
              .build();

      Policy newPolicy = Policy.newBuilder().mergeFrom(currentPolicy).addBindings(binding).build();

      client.setIamPolicy(
          SetIamPolicyRequest.newBuilder()
              .setResource(secretName.toString())
              .setPolicy(newPolicy)
              .build());
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
  public void testGetParam() throws IOException {
    ParameterName parameterName = TEST_PARAMETER_NAME_TO_GET;
    GetParam.getParam(parameterName.getProject(), parameterName.getParameter());

    assertThat(stdOut.toString()).contains("Found the parameter");
  }

  @Test
  public void testGetParamVersion() throws IOException {
    ParameterVersionName parameterVersionName = TEST_PARAMETER_VERSION_NAME_TO_GET;
    GetParamVersion.getParamVersion(
        parameterVersionName.getProject(),
        parameterVersionName.getParameter(),
        parameterVersionName.getParameterVersion());

    assertThat(stdOut.toString()).contains("Found parameter version");
    assertThat(stdOut.toString()).contains("Payload: " + JSON_PAYLOAD);
  }

  @Test
  public void testListParams() throws IOException {
    ParameterName parameterName = TEST_PARAMETER_NAME_TO_GET;
    ListParams.listParams(parameterName.getProject());

    assertThat(stdOut.toString()).contains("Found parameter");
  }

  @Test
  public void testListParamVersions() throws IOException {
    ParameterVersionName parameterVersionName = TEST_PARAMETER_VERSION_NAME_TO_GET;
    ListParamVersions.listParamVersions(
        parameterVersionName.getProject(), parameterVersionName.getParameter());

    assertThat(stdOut.toString()).contains("Found parameter version");
  }

  @Test
  public void testRenderParamVersion() throws IOException {
    ParameterVersionName parameterVersionName = TEST_PARAMETER_VERSION_NAME_TO_RENDER;
    RenderParamVersion.renderParamVersion(
        parameterVersionName.getProject(),
        parameterVersionName.getParameter(),
        parameterVersionName.getParameterVersion());

    assertThat(stdOut.toString()).contains("Rendered parameter version payload");
  }

  @Test
  public void testCreateParam() throws IOException {
    ParameterName parameterName = TEST_PARAMETER_NAME;
    CreateParam.createParam(parameterName.getProject(), parameterName.getParameter());

    assertThat(stdOut.toString()).contains("Created parameter:");
  }

  @Test
  public void testStructuredCreateParam() throws IOException {
    ParameterName parameterName = TEST_PARAMETER_NAME_WITH_FORMAT;
    CreateStructuredParam.createStructuredParameter(
        parameterName.getProject(), parameterName.getParameter(), ParameterFormat.JSON);

    assertThat(stdOut.toString()).contains("Created parameter");
  }

  @Test
  public void testCreateParamVersion() throws IOException {
    ParameterVersionName parameterVersionName = TEST_PARAMETER_VERSION_NAME;
    CreateParamVersion.createParamVersion(
        parameterVersionName.getProject(),
        parameterVersionName.getParameter(),
        parameterVersionName.getParameterVersion(),
        PAYLOAD);

    assertThat(stdOut.toString()).contains("Created parameter version");
  }

  @Test
  public void testStructuredCreateParamVersion() throws IOException {
    ParameterVersionName parameterVersionName = TEST_PARAMETER_VERSION_NAME_WITH_FORMAT;
    CreateStructuredParamVersion.createStructuredParamVersion(
        parameterVersionName.getProject(),
        parameterVersionName.getParameter(),
        parameterVersionName.getParameterVersion(),
        JSON_PAYLOAD);

    assertThat(stdOut.toString()).contains("Created parameter version");
  }

  @Test
  public void testStructuredCreateParamVersionWithSecret() throws IOException {
    ParameterVersionName parameterVersionName = TEST_PARAMETER_VERSION_NAME_WITH_SECRET_REFERENCE;
    CreateParamVersionWithSecret.createParamVersionWithSecret(
        parameterVersionName.getProject(),
        parameterVersionName.getParameter(),
        parameterVersionName.getParameterVersion(),
        SECRET_ID);

    assertThat(stdOut.toString()).contains("Created parameter version");
  }
}
