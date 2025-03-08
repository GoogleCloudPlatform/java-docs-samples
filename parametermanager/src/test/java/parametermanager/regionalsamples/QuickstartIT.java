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

package parametermanager.regionalsamples;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.client.util.Strings;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerSettings;
import com.google.cloud.parametermanager.v1.ParameterName;
import com.google.cloud.parametermanager.v1.ParameterVersionName;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class QuickstartIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION_ID =
          System.getenv().getOrDefault("GOOGLE_CLOUD_PROJECT_LOCATION", "us-central1");
  private static final String PARAMETER_ID = "java-quickstart-" + UUID.randomUUID();
  private static final String VERSION_ID = "java-quickstart-" + UUID.randomUUID();

  @BeforeClass
  public static void beforeAll() {
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT_LOCATION", Strings.isNullOrEmpty(LOCATION_ID));
  }

  @AfterClass
  public static void afterAll() throws Exception {
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT_LOCATION", Strings.isNullOrEmpty(LOCATION_ID));

    // Endpoint to call the regional parameter manager server
    String apiEndpoint = String.format("parametermanager.%s.rep.googleapis.com:443", LOCATION_ID);
    ParameterManagerSettings parameterManagerSettings =
        ParameterManagerSettings.newBuilder().setEndpoint(apiEndpoint).build();

    try (ParameterManagerClient client = ParameterManagerClient.create(parameterManagerSettings)) {
      ParameterVersionName parameterVersionName =
          ParameterVersionName.of(PROJECT_ID, LOCATION_ID, PARAMETER_ID, VERSION_ID);
      ParameterName parameterName = ParameterName.of(PROJECT_ID, LOCATION_ID, PARAMETER_ID);
      client.deleteParameterVersion(parameterVersionName.toString());
      client.deleteParameter(parameterName.toString());
    } catch (com.google.api.gax.rpc.NotFoundException e) {
      // Ignore not found error - parameter was already deleted
    } catch (io.grpc.StatusRuntimeException e) {
      if (e.getStatus().getCode() != io.grpc.Status.Code.NOT_FOUND) {
        throw e;
      }
    }
  }

  @Test
  public void quickstart_test() throws Exception {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream redirected = new ByteArrayOutputStream();

    System.setOut(new PrintStream(redirected));

    try {
      RegionalQuickstart.regionalQuickstart(
          PROJECT_ID, LOCATION_ID, PARAMETER_ID, VERSION_ID);
      assertThat(redirected.toString()).contains(
              "{\"username\": \"test-user\", \"host\": \"localhost\"}");
    } finally {
      System.setOut(originalOut);
    }
  }
}
