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
import static org.junit.Assert.assertNotNull;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.tpu.v2alpha1.QueuedResource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 6, unit = TimeUnit.MINUTES)
public class QueuedResourcesIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-f";
  private static final String NODE_NAME = "test-tpu-queued-resource-" + UUID.randomUUID();
  private static final String NODE_WITH_NETWORK_NAME =
      "test-tpu-queued-resource-network-" + UUID.randomUUID();
  private static final String TPU_TYPE = "v2-8";
  private static final String TPU_SOFTWARE_VERSION = "tpu-vm-tf-2.17.0-pjrt";
  private static final String QUEUED_RESOURCE_NAME = "queued-resource-" + UUID.randomUUID();
  private static final String QUEUED_RESOURCE_PATH_NAME =
      String.format("projects/%s/locations/%s/queuedResources/%s",
          PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME);
  private static final String QUEUED_RESOURCE_WITH_NETWORK =
      "queued-resource-network-" + UUID.randomUUID();
  private static final String NETWORK_NAME = "default";

  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp() throws IOException, ExecutionException, InterruptedException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    QueuedResource queuedResource = CreateQueuedResource.createQueuedResource(PROJECT_ID, ZONE,
        QUEUED_RESOURCE_NAME, NODE_NAME, TPU_TYPE, TPU_SOFTWARE_VERSION);

    assertThat(queuedResource.getName()).isEqualTo(QUEUED_RESOURCE_PATH_NAME);
    assertThat(queuedResource.getTpu().getNodeSpec(0).getNode().getName()).isEqualTo(NODE_NAME);
  }

  @AfterAll
  public static void cleanup() throws IOException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    DeleteQueuedResource.deleteQueuedResource(PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME);
    DeleteForceQueuedResource.deleteForceQueuedResource(
        PROJECT_ID, ZONE, QUEUED_RESOURCE_WITH_NETWORK);

    // Test that resources are deleted
    assertThat(stdOut.toString()).contains("Deleted Queued Resource:");
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetTpuVm.getTpuVm(PROJECT_ID, ZONE, NODE_NAME));

    stdOut.close();
    System.setOut(out);
  }

  @Test
  public void testGetQueuedResource() throws IOException {
    QueuedResource queuedResource = GetQueuedResource.getQueuedResource(
        PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME);

    assertNotNull(queuedResource);
    assertThat(queuedResource.getName()).isEqualTo(QUEUED_RESOURCE_PATH_NAME);
  }

  @Test
  public void testCreateQueuedResourceWithSpecifiedNetwork() throws Exception {
    QueuedResource queuedResource = CreateQueuedResourceWithNetwork.createQueuedResourceWithNetwork(
        PROJECT_ID, ZONE, QUEUED_RESOURCE_WITH_NETWORK, NODE_WITH_NETWORK_NAME,
        TPU_TYPE, TPU_SOFTWARE_VERSION, NETWORK_NAME);

    assertThat(queuedResource.getTpu().getNodeSpec(0).getNode().getName())
        .isEqualTo(NODE_WITH_NETWORK_NAME);
    assertThat(queuedResource.getTpu().getNodeSpec(0).getNode().getNetworkConfig().getNetwork()
        .contains(NETWORK_NAME));
    assertThat(queuedResource.getTpu().getNodeSpec(0).getNode().getNetworkConfig().getSubnetwork()
        .contains(NETWORK_NAME));
  }
}