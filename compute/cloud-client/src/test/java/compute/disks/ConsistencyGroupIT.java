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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.AddResourcePoliciesRegionDiskRequest;
import com.google.cloud.compute.v1.InsertResourcePolicyRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.RemoveResourcePoliciesRegionDiskRequest;
import com.google.cloud.compute.v1.ResourcePoliciesClient;
import compute.disks.consistencygroup.AddDiskToConsistencyGroup;
import compute.disks.consistencygroup.CreateDiskConsistencyGroup;
import compute.disks.consistencygroup.DeleteDiskConsistencyGroup;
import compute.disks.consistencygroup.RemoveDiskFromConsistencyGroup;
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
  private static final String CONSISTENCY_GROUP_NAME = "consistency-group";
  private static final String DISK_NAME = "disk-for-consistency";

  @Test
  public void testCreateDiskConsistencyGroupResourcePolicy() throws Exception {
    try (MockedStatic<ResourcePoliciesClient> mockedResourcePoliciesClient =
                 mockStatic(ResourcePoliciesClient.class)) {
      Operation operation = mock(Operation.class);
      ResourcePoliciesClient mockClient = mock(ResourcePoliciesClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedResourcePoliciesClient.when(ResourcePoliciesClient::create).thenReturn(mockClient);
      when(mockClient.insertAsync(any(InsertResourcePolicyRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Operation.Status.DONE);

      Operation.Status status = CreateDiskConsistencyGroup.createDiskConsistencyGroup(
              PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);

      verify(mockClient, times(1)).insertAsync(any(InsertResourcePolicyRequest.class));
      verify(mockFuture, times(1)).get();
      assertEquals(Operation.Status.DONE, status);
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
      when(mockFuture.get()).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Operation.Status.DONE);

      Operation.Status status = AddDiskToConsistencyGroup.addDiskToConsistencyGroup(
              PROJECT_ID, REGION, DISK_NAME, CONSISTENCY_GROUP_NAME, REGION);

      verify(mockClient, times(1))
              .addResourcePoliciesAsync(any(AddResourcePoliciesRegionDiskRequest.class));
      verify(mockFuture, times(1)).get();
      assertEquals(Operation.Status.DONE, status);
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
      when(mockFuture.get()).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Operation.Status.DONE);

      Operation.Status status = RemoveDiskFromConsistencyGroup.removeDiskFromConsistencyGroup(
              PROJECT_ID, REGION, DISK_NAME, CONSISTENCY_GROUP_NAME, REGION);

      verify(mockClient, times(1))
              .removeResourcePoliciesAsync(any(RemoveResourcePoliciesRegionDiskRequest.class));
      verify(mockFuture, times(1)).get();
      assertEquals(Operation.Status.DONE, status);
    }
  }

  @Test
  public void testDeleteDiskConsistencyGroup() throws Exception {
    try (MockedStatic<ResourcePoliciesClient> mockedResourcePoliciesClient =
                 mockStatic(ResourcePoliciesClient.class)) {
      Operation operation = mock(Operation.class);
      ResourcePoliciesClient mockClient = mock(ResourcePoliciesClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedResourcePoliciesClient.when(ResourcePoliciesClient::create).thenReturn(mockClient);
      when(mockClient.deleteAsync(PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME))
              .thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Operation.Status.DONE);

      Operation.Status status = DeleteDiskConsistencyGroup.deleteDiskConsistencyGroup(
              PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);

      verify(mockClient, times(1))
              .deleteAsync(PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);
      verify(mockFuture, times(1)).get();
      assertEquals(Operation.Status.DONE, status);
    }
  }
}
