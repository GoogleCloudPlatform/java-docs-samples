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

import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.tpu.v2alpha1.CreateQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.DeleteQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.GetQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.NetworkConfig;
import com.google.cloud.tpu.v2alpha1.Node;
import com.google.cloud.tpu.v2alpha1.QueuedResource;
import com.google.cloud.tpu.v2alpha1.TpuClient;
import com.google.longrunning.Operation;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class QueuedResourceIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "europe-west4-a";
  private static final String NODE_NAME = "test-tpu-queued-resource-network-" + UUID.randomUUID();
  private static final String TPU_TYPE = "v2-8";
  private static final String TPU_SOFTWARE_VERSION = "tpu-vm-tf-2.14.1";
  private static final String QUEUED_RESOURCE_NAME = "queued-resource-network-" + UUID.randomUUID();
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

  @Test
  public void testCreateQueuedResourceWithSpecifiedNetwork() throws Exception {
    TpuClient mockTpuClient = mock(TpuClient.class);
    String parent = String.format("projects/%s/locations/%s", PROJECT_ID, ZONE);
    String region = ZONE.substring(0, ZONE.length() - 2);

    NetworkConfig networkConfig =
        NetworkConfig.newBuilder()
            .setEnableExternalIps(true)
            .setNetwork(String.format("projects/%s/global/networks/%s", PROJECT_ID, NETWORK_NAME))
            .setSubnetwork(
                String.format(
                    "projects/%s/regions/%s/subnetworks/%s", PROJECT_ID, region, NETWORK_NAME))
            .build();

    Node node =
        Node.newBuilder()
            .setName(NODE_NAME)
            .setAcceleratorType(TPU_TYPE)
            .setRuntimeVersion(TPU_SOFTWARE_VERSION)
            .setNetworkConfig(networkConfig)
            .setQueuedResource(
                String.format(
                    "projects/%s/locations/%s/queuedResources/%s",
                    PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME))
            .build();

    QueuedResource queuedResource =
        QueuedResource.newBuilder()
            .setName(QUEUED_RESOURCE_NAME)
            .setTpu(
                QueuedResource.Tpu.newBuilder()
                    .addNodeSpec(
                        QueuedResource.Tpu.NodeSpec.newBuilder()
                            .setParent(parent)
                            .setNode(node)
                            .setNodeId(NODE_NAME)
                            .build())
                    .build())
            .build();

    CreateQueuedResourceRequest request =
        CreateQueuedResourceRequest.newBuilder()
            .setParent(parent)
            .setQueuedResource(queuedResource)
            .setQueuedResourceId(QUEUED_RESOURCE_NAME)
            .build();

    OperationFuture mockFuture = mock(OperationFuture.class);
    when(mockTpuClient.createQueuedResourceAsync(request))
        .thenReturn(mockFuture);
    Operation mockOperation = mock(Operation.class);
    when(mockFuture.get(3, TimeUnit.MINUTES)).thenReturn(mockOperation);
    when(mockOperation.hasError()).thenReturn(false);

    CreateQueuedResourceWithNetwork creator = new CreateQueuedResourceWithNetwork(mockTpuClient);

    creator.createQueuedResourceWithNetwork(
        PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME, NODE_NAME,
        TPU_TYPE, TPU_SOFTWARE_VERSION, NETWORK_NAME);

    verify(mockTpuClient, times(1))
        .createQueuedResourceAsync(request);
  }

  @Test
  public void testGetQueuedResource() {
    TpuClient mockTpuClient = mock(TpuClient.class);
    String name = String.format("projects/%s/locations/%s/queuedResources/%s",
        PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME);
    GetQueuedResourceRequest request =
        GetQueuedResourceRequest.newBuilder().setName(name).build();
    GetQueuedResource creator = new GetQueuedResource(mockTpuClient);
    creator.getQueuedResource(PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME);

    verify(mockTpuClient, times(1))
        .getQueuedResource(request);
  }

  @Test
  public void testDeleteForceQueuedResource()
      throws ExecutionException, InterruptedException, TimeoutException {
    TpuClient mockTpuClient = mock(TpuClient.class);
    String name = String.format("projects/%s/locations/%s/queuedResources/%s",
        PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME);
    DeleteQueuedResourceRequest request =
        DeleteQueuedResourceRequest.newBuilder().setName(name).setForce(true).build();

    OperationFuture mockFuture = mock(OperationFuture.class);
    when(mockTpuClient.deleteQueuedResourceAsync(request))
        .thenReturn(mockFuture);
    Operation mockOperation = mock(Operation.class);
    when(mockFuture.get(3, TimeUnit.MINUTES)).thenReturn(mockOperation);
    when(mockOperation.hasError()).thenReturn(false);

    DeleteForceQueuedResource creator = new DeleteForceQueuedResource(mockTpuClient);
    creator.deleteForceQueuedResource(PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME);

    verify(mockTpuClient, times(1))
        .deleteQueuedResourceAsync(request);
  }
}