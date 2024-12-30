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

package compute.disks;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.AddResourcePoliciesRegionDiskRequest;
import com.google.cloud.compute.v1.BulkInsertDiskRequest;
import com.google.cloud.compute.v1.BulkInsertRegionDiskRequest;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.InsertResourcePolicyRequest;
import com.google.cloud.compute.v1.ListDisksRequest;
import com.google.cloud.compute.v1.ListRegionDisksRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.RemoveResourcePoliciesRegionDiskRequest;
import com.google.cloud.compute.v1.ResourcePoliciesClient;
import com.google.cloud.compute.v1.StopGroupAsyncReplicationDiskRequest;
import com.google.cloud.compute.v1.StopGroupAsyncReplicationRegionDiskRequest;
import compute.disks.consistencygroup.AddDiskToConsistencyGroup;
import compute.disks.consistencygroup.CloneRegionalDisksFromConsistencyGroup;
import compute.disks.consistencygroup.CloneZonalDisksFromConsistencyGroup;
import compute.disks.consistencygroup.CreateConsistencyGroup;
import compute.disks.consistencygroup.DeleteConsistencyGroup;
import compute.disks.consistencygroup.ListRegionalDisksInConsistencyGroup;
import compute.disks.consistencygroup.ListZonalDisksInConsistencyGroup;
import compute.disks.consistencygroup.RemoveDiskFromConsistencyGroup;
import compute.disks.consistencygroup.StopRegionalDiskReplicationConsistencyGroup;
import compute.disks.consistencygroup.StopZonalDiskReplicationConsistencyGroup;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
@Timeout(value = 2, unit = TimeUnit.MINUTES)
public class ConsistencyGroupIT {
  private static final String PROJECT_ID = "project-id";
  private static final String REGION = "asia-east1";
  private static final String ZONE = "asia-east1-c";
  private static final String CONSISTENCY_GROUP_NAME = "consistency-group";
  private static final String DISK_NAME = "disk-for-consistency";

  @Test
  public void testCreateConsistencyGroupResourcePolicy() throws Exception {
    try (MockedStatic<ResourcePoliciesClient> mockedResourcePoliciesClient =
                 mockStatic(ResourcePoliciesClient.class)) {
      Operation operation = mock(Operation.class);
      ResourcePoliciesClient mockClient = mock(ResourcePoliciesClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedResourcePoliciesClient.when(ResourcePoliciesClient::create).thenReturn(mockClient);
      when(mockClient.insertAsync(any(InsertResourcePolicyRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status status = CreateConsistencyGroup.createConsistencyGroup(
              PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);

      verify(mockClient, times(1)).insertAsync(any(InsertResourcePolicyRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }

  @Test
  public void testAddRegionalDiskToConsistencyGroup() throws Exception {
    try (MockedStatic<RegionDisksClient> mockedRegionDisksClient =
                 mockStatic(RegionDisksClient.class)) {
      Operation operation = mock(Operation.class);
      RegionDisksClient mockClient = mock(RegionDisksClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedRegionDisksClient.when(RegionDisksClient::create).thenReturn(mockClient);
      when(mockClient.addResourcePoliciesAsync(any(AddResourcePoliciesRegionDiskRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status status = AddDiskToConsistencyGroup.addDiskToConsistencyGroup(
              PROJECT_ID, REGION, DISK_NAME, CONSISTENCY_GROUP_NAME, REGION);

      verify(mockClient, times(1))
              .addResourcePoliciesAsync(any(AddResourcePoliciesRegionDiskRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }

  @Test
  public void testRemoveDiskFromConsistencyGroup() throws Exception {
    try (MockedStatic<RegionDisksClient> mockedRegionDisksClient =
                 mockStatic(RegionDisksClient.class)) {
      Operation operation = mock(Operation.class);
      RegionDisksClient mockClient = mock(RegionDisksClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedRegionDisksClient.when(RegionDisksClient::create).thenReturn(mockClient);
      when(mockClient.removeResourcePoliciesAsync(
              any(RemoveResourcePoliciesRegionDiskRequest.class))).thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status status = RemoveDiskFromConsistencyGroup.removeDiskFromConsistencyGroup(
              PROJECT_ID, REGION, DISK_NAME, CONSISTENCY_GROUP_NAME, REGION);

      verify(mockClient, times(1))
              .removeResourcePoliciesAsync(any(RemoveResourcePoliciesRegionDiskRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }

  @Test
  public void testDeleteConsistencyGroup() throws Exception {
    try (MockedStatic<ResourcePoliciesClient> mockedResourcePoliciesClient =
                 mockStatic(ResourcePoliciesClient.class)) {
      Operation operation = mock(Operation.class);
      ResourcePoliciesClient mockClient = mock(ResourcePoliciesClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedResourcePoliciesClient.when(ResourcePoliciesClient::create).thenReturn(mockClient);
      when(mockClient.deleteAsync(PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status status = DeleteConsistencyGroup.deleteConsistencyGroup(
              PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);

      verify(mockClient, times(1))
              .deleteAsync(PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }

  @Test
  public void testListRegionalDisksInConsistencyGroup() throws Exception {
    try (MockedStatic<RegionDisksClient> mockedRegionDisksClient =
                 mockStatic(RegionDisksClient.class)) {
      RegionDisksClient mockClient = mock(RegionDisksClient.class);
      RegionDisksClient.ListPagedResponse mockResponse =
              mock(RegionDisksClient.ListPagedResponse.class);

      mockedRegionDisksClient.when(RegionDisksClient::create).thenReturn(mockClient);
      when(mockClient.list(any(ListRegionDisksRequest.class)))
              .thenReturn(mockResponse);

      ListRegionalDisksInConsistencyGroup.listRegionalDisksInConsistencyGroup(
              PROJECT_ID, CONSISTENCY_GROUP_NAME, REGION, REGION);

      verify(mockClient, times(1))
              .list(any(ListRegionDisksRequest.class));
      verify(mockResponse, times(1)).iterateAll();
    }
  }

  @Test
  public void testListZonalDisksInConsistencyGroup() throws Exception {
    try (MockedStatic<DisksClient> mockedRegionDisksClient =
                 mockStatic(DisksClient.class)) {
      DisksClient mockClient = mock(DisksClient.class);
      DisksClient.ListPagedResponse mockResponse =
              mock(DisksClient.ListPagedResponse.class);

      mockedRegionDisksClient.when(DisksClient::create).thenReturn(mockClient);
      when(mockClient.list(any(ListDisksRequest.class)))
              .thenReturn(mockResponse);

      ListZonalDisksInConsistencyGroup.listZonalDisksInConsistencyGroup(
              PROJECT_ID, CONSISTENCY_GROUP_NAME, REGION, REGION);

      verify(mockClient, times(1))
              .list(any(ListDisksRequest.class));
      verify(mockResponse, times(1)).iterateAll();
    }
  }

  @Test
  public void testStopRegionalDiskReplicationConsistencyGroup() throws Exception {
    try (MockedStatic<RegionDisksClient> mockedRegionDisksClient =
                 mockStatic(RegionDisksClient.class)) {
      Operation operation = mock(Operation.class);
      RegionDisksClient mockClient = mock(RegionDisksClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedRegionDisksClient.when(RegionDisksClient::create).thenReturn(mockClient);
      when(mockClient.stopGroupAsyncReplicationAsync(
              any(StopGroupAsyncReplicationRegionDiskRequest.class))).thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status status = StopRegionalDiskReplicationConsistencyGroup
              .stopRegionalDiskReplicationConsistencyGroup(
                      PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);

      verify(mockClient, times(1)).stopGroupAsyncReplicationAsync(
              any(StopGroupAsyncReplicationRegionDiskRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }

  @Test
  public void testStopZonalDiskReplicationConsistencyGroup() throws Exception {
    try (MockedStatic<DisksClient> mockedDisksClient =
                 mockStatic(DisksClient.class)) {
      Operation operation = mock(Operation.class);
      DisksClient mockClient = mock(DisksClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedDisksClient.when(DisksClient::create).thenReturn(mockClient);
      when(mockClient.stopGroupAsyncReplicationAsync(
              any(StopGroupAsyncReplicationDiskRequest.class))).thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status status = StopZonalDiskReplicationConsistencyGroup
              .stopZonalDiskReplicationConsistencyGroup(
                      PROJECT_ID, ZONE, CONSISTENCY_GROUP_NAME);

      verify(mockClient, times(1)).stopGroupAsyncReplicationAsync(
              any(StopGroupAsyncReplicationDiskRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }

  @Test
  public void testCloneRegionalDisksFromConsistencyGroup() throws Exception {
    try (MockedStatic<RegionDisksClient> mockedRegionDisksClient =
                 mockStatic(RegionDisksClient.class)) {
      Operation operation = mock(Operation.class);
      RegionDisksClient mockClient = mock(RegionDisksClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedRegionDisksClient.when(RegionDisksClient::create).thenReturn(mockClient);
      when(mockClient.bulkInsertAsync(any(BulkInsertRegionDiskRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status status = CloneRegionalDisksFromConsistencyGroup
              .cloneRegionalDisksFromConsistencyGroup(
                      PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);

      verify(mockClient, times(1))
              .bulkInsertAsync(any(BulkInsertRegionDiskRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }

  @Test
  public void testCloneZonalDisksFromConsistencyGroup() throws Exception {
    try (MockedStatic<DisksClient> mockedRegionDisksClient =
                 mockStatic(DisksClient.class)) {
      Operation operation = mock(Operation.class);
      DisksClient mockClient = mock(DisksClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedRegionDisksClient.when(DisksClient::create).thenReturn(mockClient);
      when(mockClient.bulkInsertAsync(any(BulkInsertDiskRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status status = CloneZonalDisksFromConsistencyGroup
              .cloneZonalDisksFromConsistencyGroup(PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);

      verify(mockClient, times(1))
              .bulkInsertAsync(any(BulkInsertDiskRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }
}