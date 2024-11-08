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
import com.google.cloud.tpu.v2.CreateNodeRequest;
import com.google.cloud.tpu.v2.DeleteNodeRequest;
import com.google.cloud.tpu.v2.GetNodeRequest;
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.NodeName;
import com.google.cloud.tpu.v2.TpuClient;
import com.google.longrunning.Operation;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class TpuVmIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "asia-east1-c";
  private static final String NODE_NAME = "test-tpu-" + UUID.randomUUID();
  private static final String TPU_TYPE = "v2-8";
  private static final String TPU_SOFTWARE_VERSION = "tpu-vm-tf-2.12.1";

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
  public void testCreateTpuVm() throws  ExecutionException, InterruptedException, TimeoutException {
    TpuClient mockTpuClient = mock(TpuClient.class);
    String parent = String.format("projects/%s/locations/%s", PROJECT_ID, ZONE);

    Node tpuVm = Node.newBuilder()
        .setName(NODE_NAME)
        .setAcceleratorType(TPU_TYPE)
        .setRuntimeVersion(TPU_SOFTWARE_VERSION)
        .build();

    CreateNodeRequest request = CreateNodeRequest.newBuilder()
        .setParent(parent)
        .setNodeId(NODE_NAME)
        .setNode(tpuVm)
        .build();

    OperationFuture mockFuture = mock(OperationFuture.class);
    when(mockTpuClient.createNodeAsync(request))
        .thenReturn(mockFuture);
    Operation mockOperation = mock(Operation.class);
    when(mockFuture.get(3, TimeUnit.MINUTES)).thenReturn(mockOperation);
    when(mockOperation.hasError()).thenReturn(false);

    CreateTpuVm creator = new CreateTpuVm(mockTpuClient);

    creator.createTpuVm(
        PROJECT_ID, ZONE, NODE_NAME, TPU_TYPE, TPU_SOFTWARE_VERSION);
    verify(mockTpuClient, times(1))
        .createNodeAsync(request);
  }

  @Test
  public void testGetTpuVm() {
    TpuClient mockTpuClient = mock(TpuClient.class);
    String name = NodeName.of(PROJECT_ID, ZONE, NODE_NAME).toString();
    GetNodeRequest request = GetNodeRequest.newBuilder().setName(name).build();
    GetTpuVm creator = new GetTpuVm(mockTpuClient);
    creator.getTpuVm(PROJECT_ID, ZONE, NODE_NAME);

    verify(mockTpuClient, times(1))
        .getNode(request);
  }

  @Test
  public void testDeleteTpuVm() throws ExecutionException, InterruptedException, TimeoutException {
    TpuClient mockTpuClient = mock(TpuClient.class);
    String name = NodeName.of(PROJECT_ID, ZONE, NODE_NAME).toString();
    DeleteNodeRequest deleteRequest = DeleteNodeRequest.newBuilder().setName(name).build();

    OperationFuture mockFuture = mock(OperationFuture.class);
    when(mockTpuClient.deleteNodeAsync(deleteRequest))
        .thenReturn(mockFuture);
    Operation mockOperation = mock(Operation.class);
    when(mockFuture.get(3, TimeUnit.MINUTES)).thenReturn(mockOperation);
    when(mockOperation.hasError()).thenReturn(false);

    DeleteTpuVm creator = new DeleteTpuVm(mockTpuClient);
    creator.deleteTpuVm(PROJECT_ID, ZONE, NODE_NAME);

    verify(mockTpuClient, times(1))
        .deleteNodeAsync(deleteRequest);
  }
}