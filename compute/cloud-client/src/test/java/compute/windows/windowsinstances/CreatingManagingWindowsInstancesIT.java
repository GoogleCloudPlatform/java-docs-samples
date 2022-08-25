// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package compute.windows.windowsinstances;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.compute.v1.RoutesClient;
import compute.DeleteFirewallRule;
import compute.DeleteInstance;
import compute.Util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
public class CreatingManagingWindowsInstancesIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-b";
  private static String INSTANCE_NAME_EXTERNAL;
  private static String INSTANCE_NAME_INTERNAL;
  private static String FIREWALL_RULE_NAME;
  private static String NETWORK_NAME;
  private static String SUBNETWORK_NAME;
  private static String ROUTE_NAME;

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    // Cleanup existing test instances.
    Util.cleanUpExistingInstances("windows-test-instance", PROJECT_ID, ZONE);

    String uuid = UUID.randomUUID().toString().split("-")[0];
    INSTANCE_NAME_EXTERNAL = "windows-test-instance-external-" + uuid;
    INSTANCE_NAME_INTERNAL = "windows-test-instance-internal-" + uuid;
    FIREWALL_RULE_NAME = "windows-test-firewall-" + uuid;
    NETWORK_NAME = "global/networks/default";
    SUBNETWORK_NAME = "regions/europe-central2/subnetworks/default";
    ROUTE_NAME = "windows-test-route-" + uuid;

    stdOut.close();
    System.setOut(out);
  }

  public static void deleteRoute()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (RoutesClient routesClient = RoutesClient.create()) {
      routesClient.deleteAsync(PROJECT_ID, ROUTE_NAME).get(3, TimeUnit.MINUTES);
    }
  }

  @BeforeEach
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @AfterEach
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }

  @Test
  public void testCreateWindowsServerInstanceExternalIp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Create Windows server instance with external IP.
    CreateWindowsServerInstanceExternalIp.createWindowsServerInstanceExternalIp(PROJECT_ID, ZONE,
        INSTANCE_NAME_EXTERNAL);
    assertThat(stdOut.toString()).contains("Instance created " + INSTANCE_NAME_EXTERNAL);

    // Delete instance.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_NAME_EXTERNAL);
  }

  @Test
  public void testCreateWindowsServerInstanceInternalIp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Create Windows server instance with internal IP and firewall rule.
    CreateWindowsServerInstanceInternalIp.createWindowsServerInstanceInternalIp(PROJECT_ID, ZONE,
        INSTANCE_NAME_INTERNAL, NETWORK_NAME, SUBNETWORK_NAME);
    assertThat(stdOut.toString()).contains("Instance created " + INSTANCE_NAME_INTERNAL);
    CreateFirewallRuleForWindowsActivationHost.createFirewallRuleForWindowsActivationHost(
        PROJECT_ID, FIREWALL_RULE_NAME, NETWORK_NAME);
    assertThat(stdOut.toString()).contains(
        String.format("Firewall rule created %s", FIREWALL_RULE_NAME));
    CreateRouteToWindowsActivationHost.createRouteToWindowsActivationHost(PROJECT_ID, ROUTE_NAME,
        NETWORK_NAME);
    assertThat(stdOut.toString()).contains(String.format("Route created %s", ROUTE_NAME));

    // Delete Route.
    deleteRoute();
    // Delete Firewall.
    DeleteFirewallRule.deleteFirewallRule(PROJECT_ID, FIREWALL_RULE_NAME);
    // Delete Instance.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_NAME_INTERNAL);
  }
}