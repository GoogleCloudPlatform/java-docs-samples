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

package tpu;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.tpu.v2alpha1.QueuedResource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 6, unit = TimeUnit.MINUTES)
public class QueuedResourceIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "europe-west4-a";
  private static final String NODE_FOR_NETWORK =
      "test-tpu-queued-resource-network-" + UUID.randomUUID();
  private static final String NODE_FOR_SCRIPT =
      "test-tpu-queued-resource-script-" + UUID.randomUUID();
  private static final String TPU_TYPE = "v2-8";
  private static final String TPU_SOFTWARE_VERSION = "tpu-vm-tf-2.14.1";
  private static final String QUEUED_RESOURCE_FOR_NETWORK =
      "queued-resource-network-" + UUID.randomUUID();
  private static final String QUEUED_RESOURCE_FOR_SCRIPT =
      "queued-resource-script-" + UUID.randomUUID();
  private static final String NETWORK_NAME = "default";

  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @AfterAll
  public static void cleanup() {
    DeleteForceQueuedResource.deleteForceQueuedResource(
        PROJECT_ID, ZONE, QUEUED_RESOURCE_FOR_NETWORK);
    DeleteForceQueuedResource.deleteForceQueuedResource(
        PROJECT_ID, ZONE, QUEUED_RESOURCE_FOR_SCRIPT);
  }

  @Test
  public void testCreateQueuedResourceWithSpecifiedNetwork() throws Exception {

    QueuedResource queuedResource = CreateQueuedResourceWithNetwork.createQueuedResourceWithNetwork(
        PROJECT_ID, ZONE, QUEUED_RESOURCE_FOR_NETWORK, NODE_FOR_NETWORK,
        TPU_TYPE, TPU_SOFTWARE_VERSION, NETWORK_NAME);

    assertThat(queuedResource.getTpu().getNodeSpec(0).getNode().getName())
        .isEqualTo(NODE_FOR_NETWORK);
    assertThat(queuedResource.getTpu().getNodeSpec(0).getNode().getNetworkConfig().getNetwork()
        .contains(NETWORK_NAME));
    assertThat(queuedResource.getTpu().getNodeSpec(0).getNode().getNetworkConfig().getSubnetwork()
        .contains(NETWORK_NAME));
  }

  @Test
  public void testCreateQueuedResourceWithStartupScript() throws Exception {
    QueuedResource queuedResource = CreateQueuedResourceWithStartupScript.createQueuedResource(
        PROJECT_ID, ZONE, QUEUED_RESOURCE_FOR_SCRIPT, NODE_FOR_SCRIPT,
        TPU_TYPE, TPU_SOFTWARE_VERSION);

    assertThat(queuedResource.getTpu().getNodeSpec(0).getNode().containsLabels("startup-script"));
    assertThat(queuedResource.getTpu().getNodeSpec(0).getNode().getLabelsMap()
        .containsValue("Hello from the startup script!"));
  }
}