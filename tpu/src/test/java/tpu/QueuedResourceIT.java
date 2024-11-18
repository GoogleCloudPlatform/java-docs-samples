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
import com.google.cloud.tpu.v2alpha1.CreateQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.DeleteQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.GetQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.QueuedResource;
import com.google.cloud.tpu.v2alpha1.TpuClient;
import com.google.cloud.tpu.v2alpha1.TpuSettings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
@Timeout(value = 3)
public class QueuedResourceIT {
  private static final String PROJECT_ID = "project-id";
  private static final String ZONE = "europe-west4-a";
  private static final String NODE_NAME = "test-tpu";
  private static final String TPU_TYPE = "v2-8";
  private static final String TPU_SOFTWARE_VERSION = "tpu-vm-tf-2.14.1";
  private static final String QUEUED_RESOURCE_NAME = "queued-resource";
  private static final String NETWORK_NAME = "default";
  private static ByteArrayOutputStream bout;

  @BeforeAll
  public static void setUp() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @Test
  public void testCreateQueuedResourceWithSpecifiedNetwork() throws Exception {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      QueuedResource mockQueuedResource = mock(QueuedResource.class);
      TpuClient mockTpuClient = mock(TpuClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedTpuClient.when(() -> TpuClient.create(any(TpuSettings.class)))
          .thenReturn(mockTpuClient);
      when(mockTpuClient.createQueuedResourceAsync(any(CreateQueuedResourceRequest.class)))
          .thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(mockQueuedResource);

      QueuedResource returnedQueuedResource =
          CreateQueuedResourceWithNetwork.createQueuedResourceWithNetwork(
              PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME, NODE_NAME,
              TPU_TYPE, TPU_SOFTWARE_VERSION, NETWORK_NAME);

      verify(mockTpuClient, times(1))
          .createQueuedResourceAsync(any(CreateQueuedResourceRequest.class));
      verify(mockFuture, times(1)).get();
      assertEquals(returnedQueuedResource, mockQueuedResource);
    }
  }

  @Test
  public void testGetQueuedResource() throws IOException {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      TpuClient mockClient = mock(TpuClient.class);
      GetQueuedResource mockGetQueuedResource = mock(GetQueuedResource.class);
      QueuedResource mockQueuedResource = mock(QueuedResource.class);

      mockedTpuClient.when(TpuClient::create).thenReturn(mockClient);
      when(mockClient.getQueuedResource(any(GetQueuedResourceRequest.class)))
          .thenReturn(mockQueuedResource);

      QueuedResource returnedQueuedResource =
          GetQueuedResource.getQueuedResource(PROJECT_ID, ZONE, NODE_NAME);

      verify(mockGetQueuedResource, times(1))
          .getQueuedResource(PROJECT_ID, ZONE, NODE_NAME);
      assertEquals(returnedQueuedResource, mockQueuedResource);
    }
  }

  @Test
  public void testDeleteForceQueuedResource() {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      TpuClient mockTpuClient = mock(TpuClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedTpuClient.when(() -> TpuClient.create(any(TpuSettings.class)))
          .thenReturn(mockTpuClient);
      when(mockTpuClient.deleteQueuedResourceAsync(any(DeleteQueuedResourceRequest.class)))
          .thenReturn(mockFuture);

      DeleteForceQueuedResource.deleteForceQueuedResource(PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME);
      String output = bout.toString();

      assertThat(output).contains("Deleted Queued Resource:");
      verify(mockTpuClient, times(1))
          .deleteQueuedResourceAsync(any(DeleteQueuedResourceRequest.class));
    }
  }
}