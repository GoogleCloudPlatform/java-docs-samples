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
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.google.cloud.tpu.v2alpha1.ListQueuedResourcesRequest;
import com.google.cloud.tpu.v2alpha1.QueuedResource;
import com.google.cloud.tpu.v2alpha1.TpuClient;
import com.google.cloud.tpu.v2alpha1.TpuClient.ListQueuedResourcesPage;
import com.google.cloud.tpu.v2alpha1.TpuClient.ListQueuedResourcesPagedResponse;
import com.google.cloud.tpu.v2alpha1.TpuSettings;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
@Timeout(value = 2, unit = TimeUnit.MINUTES)
public class QueuedResourceIT {
  private static final String PROJECT_ID = "project-id";
  private static final String ZONE = "europe-west4-a";
  private static final String NODE_NAME = "test-tpu";
  private static final String TPU_TYPE = "v5litepod-4";
  private static final String TPU_SOFTWARE_VERSION = "v2-tpuv5-litepod";
  private static final String QUEUED_RESOURCE_NAME = "queued-resource";
  private static final String NETWORK_NAME = "default";

  @Test
  public void testCreateQueuedResource() throws Exception {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      QueuedResource mockQueuedResource = mock(QueuedResource.class);
      TpuClient mockTpuClient = mock(TpuClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedTpuClient.when(TpuClient::create).thenReturn(mockTpuClient);
      when(mockTpuClient.createQueuedResourceAsync(any(CreateQueuedResourceRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(mockQueuedResource);

      QueuedResource returnedQueuedResource =
              CreateQueuedResource.createQueuedResource(
                      PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME, NODE_NAME,
                      TPU_TYPE, TPU_SOFTWARE_VERSION);

      verify(mockTpuClient, times(1))
              .createQueuedResourceAsync(any(CreateQueuedResourceRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(returnedQueuedResource, mockQueuedResource);
    }
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
      QueuedResource mockQueuedResource = mock(QueuedResource.class);

      mockedTpuClient.when(TpuClient::create).thenReturn(mockClient);
      when(mockClient.getQueuedResource(any(GetQueuedResourceRequest.class)))
          .thenReturn(mockQueuedResource);

      QueuedResource returnedQueuedResource =
          GetQueuedResource.getQueuedResource(PROJECT_ID, ZONE, NODE_NAME);

      verify(mockClient, times(1))
          .getQueuedResource(any(GetQueuedResourceRequest.class));
      assertEquals(returnedQueuedResource, mockQueuedResource);
    }
  }

  @Test
  public void testListTpuVm() throws IOException {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      QueuedResource queuedResource1 = mock(QueuedResource.class);
      QueuedResource queuedResource2 = mock(QueuedResource.class);
      List<QueuedResource> mockListQueuedResources =
          Arrays.asList(queuedResource1, queuedResource2);

      TpuClient mockClient = mock(TpuClient.class);
      mockedTpuClient.when(TpuClient::create).thenReturn(mockClient);
      ListQueuedResourcesPagedResponse mockListQueuedResourcesResponse =
          mock(ListQueuedResourcesPagedResponse.class);
      when(mockClient.listQueuedResources(any(ListQueuedResourcesRequest.class)))
          .thenReturn(mockListQueuedResourcesResponse);
      ListQueuedResourcesPage mockQueuedResourcesPage =
          mock(ListQueuedResourcesPage.class);
      when(mockListQueuedResourcesResponse.getPage()).thenReturn(mockQueuedResourcesPage);
      when(mockQueuedResourcesPage.getValues()).thenReturn(mockListQueuedResources);

      ListQueuedResourcesPage returnedList =
          ListQueuedResources.listQueuedResources(PROJECT_ID, ZONE);

      assertThat(returnedList.getValues()).isEqualTo(mockListQueuedResources);
      verify(mockClient, times(1)).listQueuedResources(any(ListQueuedResourcesRequest.class));
    }
  }

  @Test
  public void testDeleteForceQueuedResource()
          throws IOException, InterruptedException, ExecutionException {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      TpuClient mockTpuClient = mock(TpuClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedTpuClient.when(() -> TpuClient.create(any(TpuSettings.class)))
          .thenReturn(mockTpuClient);
      when(mockTpuClient.deleteQueuedResourceAsync(any(DeleteQueuedResourceRequest.class)))
          .thenReturn(mockFuture);

      DeleteForceQueuedResource.deleteForceQueuedResource(PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME);

      verify(mockTpuClient, times(1))
          .deleteQueuedResourceAsync(any(DeleteQueuedResourceRequest.class));
    }
  }

  @Test
  public void testDeleteQueuedResource()
          throws IOException, ExecutionException, InterruptedException {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      TpuClient mockTpuClient = mock(TpuClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedTpuClient.when(TpuClient::create).thenReturn(mockTpuClient);
      when(mockTpuClient.deleteQueuedResourceAsync(any(DeleteQueuedResourceRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(null);

      DeleteQueuedResource.deleteQueuedResource(PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME);

      verify(mockTpuClient, times(1))
              .deleteQueuedResourceAsync(any(DeleteQueuedResourceRequest.class));
    }
  }

  @Test
  public void testCreateQueuedResourceWithStartupScript() throws Exception {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      QueuedResource mockQueuedResource = mock(QueuedResource.class);
      TpuClient mockTpuClient = mock(TpuClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedTpuClient.when(TpuClient::create).thenReturn(mockTpuClient);
      when(mockTpuClient.createQueuedResourceAsync(any(CreateQueuedResourceRequest.class)))
          .thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(mockQueuedResource);

      QueuedResource returnedQueuedResource =
          CreateQueuedResourceWithStartupScript.createQueuedResource(
              PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME, NODE_NAME,
              TPU_TYPE, TPU_SOFTWARE_VERSION);

      verify(mockTpuClient, times(1))
          .createQueuedResourceAsync(any(CreateQueuedResourceRequest.class));
      verify(mockFuture, times(1)).get();
      assertEquals(returnedQueuedResource, mockQueuedResource);
    }
  }

  @Test
  public void testCreateSpotQueuedResource() throws Exception {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      QueuedResource mockQueuedResource =  QueuedResource.newBuilder()
              .setName("QueuedResourceName")
              .build();
      TpuClient mockedClientInstance = mock(TpuClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedTpuClient.when(TpuClient::create).thenReturn(mockedClientInstance);
      when(mockedClientInstance.createQueuedResourceAsync(any(CreateQueuedResourceRequest.class)))
          .thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(mockQueuedResource);

      QueuedResource returnedQueuedResource =
          CreateSpotQueuedResource.createQueuedResource(
              PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME, NODE_NAME,
              TPU_TYPE, TPU_SOFTWARE_VERSION);

      verify(mockedClientInstance, times(1))
          .createQueuedResourceAsync(any(CreateQueuedResourceRequest.class));
      verify(mockFuture, times(1)).get();
      assertEquals(returnedQueuedResource.getName(), mockQueuedResource.getName());
    }
  }

  @Test
  public void testCreateTimeBoundQueuedResource() throws Exception {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      QueuedResource mockQueuedResource =  QueuedResource.newBuilder()
              .setName("QueuedResourceName")
              .build();
      TpuClient mockTpuClient = mock(TpuClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedTpuClient.when(TpuClient::create).thenReturn(mockTpuClient);
      when(mockTpuClient.createQueuedResourceAsync(any(CreateQueuedResourceRequest.class)))
          .thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(mockQueuedResource);

      QueuedResource returnedQueuedResource =
          CreateTimeBoundQueuedResource.createTimeBoundQueuedResource(
              PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME, NODE_NAME,
              TPU_TYPE, TPU_SOFTWARE_VERSION);

      verify(mockTpuClient, times(1))
          .createQueuedResourceAsync(any(CreateQueuedResourceRequest.class));
      verify(mockFuture, times(1)).get();
      assertEquals(returnedQueuedResource.getName(), mockQueuedResource.getName());
    }
  }
}