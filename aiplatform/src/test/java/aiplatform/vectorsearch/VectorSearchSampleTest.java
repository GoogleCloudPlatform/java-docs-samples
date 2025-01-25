/*
 * Copyright 2025 Google LLC
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

package aiplatform.vectorsearch;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.aiplatform.v1.CreateIndexRequest;
import com.google.cloud.aiplatform.v1.Index;
import com.google.cloud.aiplatform.v1.IndexServiceClient;
import com.google.cloud.aiplatform.v1.IndexServiceSettings;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
public class VectorSearchSampleTest {

  private static final String PROJECT = "test-project";
  private static final String LOCATION = "test-location";
  private static final String DISPLAY_NAME = "test-display-name";

  private static final String INDEX_ID = "test-index-id";
  private static final String METADATA_JSON = "{'some': {'key' : 2}}";

  @Test
  public void testCreateIndexSample() throws Exception {
    try (MockedStatic<IndexServiceClient> mockedStaticIndexServiceClient =
        mockStatic(IndexServiceClient.class)) {
      IndexServiceClient mockIndexServiceClient = mock(IndexServiceClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);
      Index mockIndex = mock(Index.class);
      mockedStaticIndexServiceClient
          .when(() -> IndexServiceClient.create(any(IndexServiceSettings.class)))
          .thenReturn(mockIndexServiceClient);
      when(mockIndexServiceClient.createIndexAsync(any(CreateIndexRequest.class)))
          .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(mockIndex);

      Index result =
          CreateIndexSample.createIndexSample(PROJECT, LOCATION, DISPLAY_NAME, METADATA_JSON);

      verify(mockIndexServiceClient, times(1)).createIndexAsync(any(CreateIndexRequest.class));
      assertThat(result).isEqualTo(mockIndex);
    }
  }

  @Test
  public void testCreateStreamingIndexSample() throws Exception {
    try (MockedStatic<IndexServiceClient> mockedStaticIndexServiceClient =
        mockStatic(IndexServiceClient.class)) {
      IndexServiceClient mockIndexServiceClient = mock(IndexServiceClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);
      Index mockIndex = mock(Index.class);
      mockedStaticIndexServiceClient
          .when(() -> IndexServiceClient.create(any(IndexServiceSettings.class)))
          .thenReturn(mockIndexServiceClient);
      when(mockIndexServiceClient.createIndexAsync(any(CreateIndexRequest.class)))
          .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(mockIndex);

      Index result =
          CreateStreamingIndexSample.createStreamingIndexSample(
              PROJECT, LOCATION, DISPLAY_NAME, METADATA_JSON);

      verify(mockIndexServiceClient, times(1)).createIndexAsync(any(CreateIndexRequest.class));
      assertThat(result).isEqualTo(mockIndex);
    }
  }

  @Test
  public void testListIndexesSample() throws Exception {
    try (MockedStatic<IndexServiceClient> mockedStaticIndexServiceClient =
        mockStatic(IndexServiceClient.class)) {
      IndexServiceClient mockIndexServiceClient = mock(IndexServiceClient.class);
      IndexServiceClient.ListIndexesPagedResponse mockPagedResponse =
          mock(IndexServiceClient.ListIndexesPagedResponse.class);
      mockedStaticIndexServiceClient
          .when(() -> IndexServiceClient.create(any(IndexServiceSettings.class)))
          .thenReturn(mockIndexServiceClient);
      when(mockIndexServiceClient.listIndexes(anyString())).thenReturn(mockPagedResponse);

      IndexServiceClient.ListIndexesPagedResponse response =
          ListIndexesSample.listIndexesSample(PROJECT, LOCATION);

      verify(mockIndexServiceClient, times(1)).listIndexes(anyString());
      assertThat(response).isEqualTo(mockPagedResponse);
    }
  }

  @Test
  public void testDeleteIndexSample() throws Exception {
    try (MockedStatic<IndexServiceClient> mockedStaticIndexServiceClient =
        mockStatic(IndexServiceClient.class)) {
      IndexServiceClient mockIndexServiceClient = mock(IndexServiceClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);
      mockedStaticIndexServiceClient
          .when(() -> IndexServiceClient.create(any(IndexServiceSettings.class)))
          .thenReturn(mockIndexServiceClient);
      when(mockIndexServiceClient.deleteIndexAsync(anyString())).thenReturn(mockFuture);

      DeleteIndexSample.deleteIndexSample(PROJECT, LOCATION, INDEX_ID);

      verify(mockIndexServiceClient, times(1)).deleteIndexAsync(anyString());
    }
  }
}
