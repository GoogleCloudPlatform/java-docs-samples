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
  public void testDisableParamVersion() throws IOException {
    ParameterVersionName parameterVersionName = TEST_PARAMETER_VERSION_NAME_TO_GET_1;
    DisableParamVersion.disableParamVersion(
        parameterVersionName.getProject(),
        parameterVersionName.getParameter(),
        parameterVersionName.getParameterVersion());

    assertThat(stdOut.toString()).contains("Disabled parameter version");
  }

  @Test
  public void testEnableParamVersion() throws IOException {
    ParameterVersionName parameterVersionName = TEST_PARAMETER_VERSION_NAME_TO_GET_1;
    EnableParamVersion.enableParamVersion(
        parameterVersionName.getProject(),
        parameterVersionName.getParameter(),
        parameterVersionName.getParameterVersion());

    assertThat(stdOut.toString()).contains("Enabled parameter version");
  }

  @Test
  public void testDeleteParamVersion() throws IOException {
    ParameterVersionName parameterVersionName = TEST_PARAMETER_VERSION_NAME_TO_DELETE;
    DeleteParamVersion.deleteParamVersion(
        parameterVersionName.getProject(),
        parameterVersionName.getParameter(),
        parameterVersionName.getParameterVersion());

    assertThat(stdOut.toString()).contains("Deleted parameter version:");
  }

  @Test
  public void testDeleteParam() throws IOException {
    ParameterName parameterName = TEST_PARAMETER_NAME_TO_DELETE;
    DeleteParam.deleteParam(parameterName.getProject(), parameterName.getParameter());

    assertThat(stdOut.toString()).contains("Deleted parameter:");
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
