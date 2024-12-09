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
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.tpu.v2.AcceleratorConfig;
import com.google.cloud.tpu.v2.CreateNodeRequest;
import com.google.cloud.tpu.v2.DeleteNodeRequest;
import com.google.cloud.tpu.v2.GetNodeRequest;
import com.google.cloud.tpu.v2.ListNodesRequest;
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.StartNodeRequest;
import com.google.cloud.tpu.v2.StopNodeRequest;
import com.google.cloud.tpu.v2.TpuClient;
import com.google.cloud.tpu.v2.TpuSettings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
@Timeout(value = 10)
public class TpuVmIT {
  private static final String PROJECT_ID = "project-id";
  private static final String ZONE = "asia-east1-c";
  private static final String NODE_NAME = "test-tpu";
  private static final String TPU_TYPE = "v2-8";
  private static final AcceleratorConfig.Type ACCELERATOR_TYPE = AcceleratorConfig.Type.V2;
  private static final String TPU_SOFTWARE_VERSION = "tpu-vm-tf-2.14.1";
  private static final String TOPOLOGY = "2x2";

  @Test
  public void testCreateTpuVm() throws Exception {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      Node mockNode = mock(Node.class);
      TpuClient mockTpuClient = mock(TpuClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedTpuClient.when(() -> TpuClient.create(any(TpuSettings.class)))
          .thenReturn(mockTpuClient);
      when(mockTpuClient.createNodeAsync(any(CreateNodeRequest.class)))
          .thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(mockNode);

      Node returnedNode = CreateTpuVm.createTpuVm(
          PROJECT_ID, ZONE, NODE_NAME,
          TPU_TYPE, TPU_SOFTWARE_VERSION);

      verify(mockTpuClient, times(1))
          .createNodeAsync(any(CreateNodeRequest.class));
      verify(mockFuture, times(1)).get();
      assertEquals(returnedNode, mockNode);
    }
  }

  @Test
  public void testGetTpuVm() throws IOException {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      Node mockNode = mock(Node.class);
      TpuClient mockClient = mock(TpuClient.class);

      mockedTpuClient.when(TpuClient::create).thenReturn(mockClient);
      when(mockClient.getNode(any(GetNodeRequest.class))).thenReturn(mockNode);

      Node returnedNode = GetTpuVm.getTpuVm(PROJECT_ID, ZONE, NODE_NAME);

      verify(mockClient, times(1))
          .getNode(any(GetNodeRequest.class));
      assertThat(returnedNode).isEqualTo(mockNode);
    }
  }

  @Test
  public void testDeleteTpuVm() throws IOException, ExecutionException, InterruptedException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      TpuClient mockTpuClient = mock(TpuClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedTpuClient.when(() -> TpuClient.create(any(TpuSettings.class)))
          .thenReturn(mockTpuClient);
      when(mockTpuClient.deleteNodeAsync(any(DeleteNodeRequest.class)))
          .thenReturn(mockFuture);

      DeleteTpuVm.deleteTpuVm(PROJECT_ID, ZONE, NODE_NAME);
      String output = bout.toString();

      assertThat(output).contains("TPU VM deleted");
      verify(mockTpuClient, times(1)).deleteNodeAsync(any(DeleteNodeRequest.class));

      bout.close();
    }
  }

  @Test
  public void testCreateTpuVmWithTopologyFlag()
      throws IOException, ExecutionException, InterruptedException {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      Node mockNode = mock(Node.class);
      TpuClient mockTpuClient = mock(TpuClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedTpuClient.when(TpuClient::create).thenReturn(mockTpuClient);
      when(mockTpuClient.createNodeAsync(any(CreateNodeRequest.class)))
          .thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(mockNode);
      Node returnedNode = CreateTpuWithTopologyFlag.createTpuWithTopologyFlag(
          PROJECT_ID, ZONE, NODE_NAME, ACCELERATOR_TYPE,
           TPU_SOFTWARE_VERSION, TOPOLOGY);

      verify(mockTpuClient, times(1))
          .createNodeAsync(any(CreateNodeRequest.class));
      verify(mockFuture, times(1)).get();
      assertEquals(returnedNode, mockNode);
    }
  }

  @Test
  public void testListTpuVm() throws IOException {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      Node mockNode1 = mock(Node.class);
      Node mockNode2 = mock(Node.class);
      List<Node> mockListNodes = Arrays.asList(mockNode1, mockNode2);
      TpuClient mockTpuClient = mock(TpuClient.class);
      TpuClient.ListNodesPagedResponse mockListNodesResponse =
          mock(TpuClient.ListNodesPagedResponse.class);
      TpuClient.ListNodesPage mockListNodesPage = mock(TpuClient.ListNodesPage.class);

      mockedTpuClient.when(TpuClient::create).thenReturn(mockTpuClient);
      when(mockTpuClient.listNodes(any(ListNodesRequest.class))).thenReturn(mockListNodesResponse);
      when(mockListNodesResponse.getPage()).thenReturn(mockListNodesPage);
      when(mockListNodesPage.getValues()).thenReturn(mockListNodes);

      TpuClient.ListNodesPage returnedListNodes = ListTpuVms.listTpuVms(PROJECT_ID, ZONE);

      assertThat(returnedListNodes.getValues()).isEqualTo(mockListNodes);
      verify(mockTpuClient, times(1)).listNodes(any(ListNodesRequest.class));
    }
  }

  @Test
  public void testStartTpuVm() throws IOException, ExecutionException, InterruptedException {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      TpuClient mockClient = mock(TpuClient.class);
      Node mockNode = mock(Node.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedTpuClient.when(TpuClient::create).thenReturn(mockClient);
      when(mockClient.startNodeAsync(any(StartNodeRequest.class)))
          .thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(mockNode);

      Node returnedNode = StartTpuVm.startTpuVm(PROJECT_ID, ZONE, NODE_NAME);

      verify(mockClient, times(1))
          .startNodeAsync(any(StartNodeRequest.class));
      verify(mockFuture, times(1)).get();
      assertEquals(returnedNode, mockNode);
    }
  }

  @Test
  public void testStopTpuVm() throws IOException, ExecutionException, InterruptedException {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      TpuClient mockClient = mock(TpuClient.class);
      Node mockNode = mock(Node.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedTpuClient.when(TpuClient::create).thenReturn(mockClient);
      when(mockClient.stopNodeAsync(any(StopNodeRequest.class)))
          .thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(mockNode);

      Node returnedNode = StopTpuVm.stopTpuVm(PROJECT_ID, ZONE, NODE_NAME);

      verify(mockClient, times(1))
          .stopNodeAsync(any(StopNodeRequest.class));
      verify(mockFuture, times(1)).get();
      assertEquals(returnedNode, mockNode);
    }
  }
}