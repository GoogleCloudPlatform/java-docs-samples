/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package management.api;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.securitycentermanagement.v1.SecurityCenterManagementClient.ListSecurityCenterServicesPagedResponse;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterService;
import com.google.cloud.securitycentermanagement.v1.SecurityCenterService.EnablementState;
import java.io.IOException;
import java.util.stream.StreamSupport;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SecurityCenterServiceTest {
  private static final String PROJECT_ID = System.getenv("SCC_PROJECT_ID");
  private static final String SERVICE = "EVENT_THREAT_DETECTION";

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void setUp() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("SCC_PROJECT_ID");
  }

  @Test
  public void testGetSecurityCenterService() throws IOException {
    SecurityCenterService response =
        GetSecurityCenterService.getSecurityCenterService(PROJECT_ID, SERVICE);
    assertNotNull(response);
    // check whether the response contains the specified service
    assertThat(response.getName()).contains(SERVICE);
  }

  @Test
  public void testListSecurityCenterServices() throws IOException {
    ListSecurityCenterServicesPagedResponse response =
        ListSecurityCenterServices.listSecurityCenterServices(PROJECT_ID);
    assertNotNull(response);
    // check whether the response contains the specified service
    assertTrue(
        StreamSupport.stream(response.iterateAll().spliterator(), false)
            .anyMatch(service -> service.getName().contains(SERVICE)));
  }

  @Test
  public void testUpdateSecurityCenterService() throws IOException {
    SecurityCenterService response =
        UpdateSecurityCenterService.updateSecurityCenterService(PROJECT_ID, SERVICE);
    assertNotNull(response);
    assertThat(response.getIntendedEnablementState().equals(EnablementState.ENABLED));
  }
}
