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

import com.google.cloud.tpu.v2.ListNodesRequest;
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.TpuClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
@Timeout(value = 3)
public class ListTpuVmsIT {
  private static final String PROJECT_ID = "project-id";
  private static final String ZONE = "asia-east1-c";

  @Test
  public void testListTpuVm() throws IOException {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      Node mockNode1 = mock(Node.class);
      Node mockNode2 = mock(Node.class);
      List<Node> mockListNodes = Arrays.asList(mockNode1, mockNode2);

      TpuClient mockTpuClient = mock(TpuClient.class);
      mockedTpuClient.when(TpuClient::create).thenReturn(mockTpuClient);
      TpuClient.ListNodesPagedResponse mockListNodesResponse =
          mock(TpuClient.ListNodesPagedResponse.class);
      when(mockTpuClient.listNodes(any(ListNodesRequest.class))).thenReturn(mockListNodesResponse);
      TpuClient.ListNodesPage mockListNodesPage = mock(TpuClient.ListNodesPage.class);
      when(mockListNodesResponse.getPage()).thenReturn(mockListNodesPage);
      when(mockListNodesPage.getValues()).thenReturn(mockListNodes);

      TpuClient.ListNodesPage returnedListNodes = ListTpuVms.listTpuVms(PROJECT_ID, ZONE);

      assertThat(returnedListNodes.getValues()).isEqualTo(mockListNodes);
      verify(mockTpuClient, times(1)).listNodes(any(ListNodesRequest.class));
    }
  }
}

