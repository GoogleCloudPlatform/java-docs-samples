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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.tpu.v2.DeleteNodeRequest;
import com.google.cloud.tpu.v2.GetNodeRequest;
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.TpuClient;
import com.google.cloud.tpu.v2.TpuSettings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
@Timeout(value = 3)
public class TpuVmIT {
  private static final String PROJECT_ID = "project-id";
  private static final String ZONE = "asia-east1-c";
  private static final String NODE_NAME = "test-tpu";
  private static ByteArrayOutputStream bout;

  @BeforeAll
  public static void setUp() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @Test
  public void testGetTpuVm() throws IOException {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      Node mockNode = mock(Node.class);
      TpuClient mockClient = mock(TpuClient.class);
      GetTpuVm mockGetTpuVm = mock(GetTpuVm.class);

      mockedTpuClient.when(TpuClient::create).thenReturn(mockClient);
      when(mockClient.getNode(any(GetNodeRequest.class))).thenReturn(mockNode);

      Node returnedNode = GetTpuVm.getTpuVm(PROJECT_ID, ZONE, NODE_NAME);

      verify(mockGetTpuVm, times(1))
          .getTpuVm(PROJECT_ID, ZONE, NODE_NAME);
      assertThat(returnedNode).isEqualTo(mockNode);
    }
  }

  @Test
  public void testDeleteTpuVm() throws IOException, ExecutionException, InterruptedException {
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
    }
  }
}