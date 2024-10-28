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

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.tpu.v2alpha1.QueuedResource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 6, unit = TimeUnit.MINUTES)
public class CreateQueuedResourceWithNetworkIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "europe-west4-a";
  static String javaVersion = System.getProperty("java.version").substring(0, 2);
  private static final String NODE_NAME = "test-tpu-queued-resource-network-" + javaVersion + "-"
      + UUID.randomUUID().toString().substring(0, 8);
  private static final String TPU_TYPE = "v2-8";
  private static final String TPU_SOFTWARE_VERSION = "tpu-vm-tf-2.14.1";
  private static final String QUEUED_RESOURCE_NAME = "queued-resource-network-" + javaVersion + "-"
      + UUID.randomUUID().toString().substring(0, 8);
  private static final String NETWORK_NAME = "default";

  @BeforeAll
  public static void setUp() throws IOException {

    // Cleanup existing stale resources.
    Util.cleanUpExistingQueuedResources("queued-resource-network-", PROJECT_ID, ZONE);
  }

  @AfterAll
  public static void cleanup() {
    DeleteForceQueuedResource.deleteForceQueuedResource(PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME);

    // Test that resource is deleted
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetQueuedResource.getQueuedResource(PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME));
  }

  @Test
  public void shouldCreateQueuedResourceWithSpecifiedNetwork() throws Exception {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    QueuedResource queuedResource = CreateQueuedResourceWithNetwork.createQueuedResourceWithNetwork(
        PROJECT_ID,
        ZONE,
        QUEUED_RESOURCE_NAME,
        NODE_NAME,
        TPU_TYPE,
        TPU_SOFTWARE_VERSION,
        NETWORK_NAME);

    assertThat(stdOut.toString()).contains("Queued Resource created: " + QUEUED_RESOURCE_NAME);
    assertThat(queuedResource.getTpu().getNodeSpec(0).getNode().getName()).isEqualTo(NODE_NAME);
    assertThat(queuedResource.getTpu().getNodeSpec(0).getNode().getNetworkConfig().getNetwork()
        .contains(NETWORK_NAME));
    assertThat(queuedResource.getTpu().getNodeSpec(0).getNode().getNetworkConfig().getSubnetwork()
        .contains(NETWORK_NAME));

    stdOut.close();
    System.setOut(out);
  }
}