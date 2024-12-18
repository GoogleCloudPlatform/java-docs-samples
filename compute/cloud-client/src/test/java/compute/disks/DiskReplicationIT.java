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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.StartAsyncReplicationDiskRequest;
import com.google.cloud.compute.v1.StartAsyncReplicationRegionDiskRequest;
import com.google.cloud.compute.v1.StopAsyncReplicationDiskRequest;
import com.google.cloud.compute.v1.StopAsyncReplicationRegionDiskRequest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class DiskReplicationIT {

  private static final String PROJECT_ID = "project-id";
  private static final String PRIMARY_REGION = "us-central1";
  private static final String SECONDARY_REGION = "us-east1";
  private static final String PRIMARY_ZONE = "us-central1-a";
  private static final String SECONDARY_ZONE = "us-east1-c";
  private static final String PRIMARY_DISK_NAME = "test-disk-primary";
  private static final String SECONDARY_DISK_NAME = "test-disk-secondary";

  @Test
  public void testStartRegionalDiskAsyncReplication() throws Exception {
    try (MockedStatic<RegionDisksClient> mockedRegionDisksClient =
                 mockStatic(RegionDisksClient.class)) {
      Operation operation = mock(Operation.class);
      RegionDisksClient mockClient = mock(RegionDisksClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedRegionDisksClient.when(RegionDisksClient::create).thenReturn(mockClient);
      when(mockClient.startAsyncReplicationAsync(any(StartAsyncReplicationRegionDiskRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status status = StartRegionalDiskReplication.startRegionalDiskAsyncReplication(
          PROJECT_ID,  PRIMARY_DISK_NAME, PRIMARY_REGION, SECONDARY_DISK_NAME, SECONDARY_REGION);

      verify(mockClient, times(1))
              .startAsyncReplicationAsync(any(StartAsyncReplicationRegionDiskRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }

  @Test
  public void testStartZonalDiskAsyncReplication() throws Exception {
    try (MockedStatic<DisksClient> mockedDisksClient =
                 mockStatic(DisksClient.class)) {
      Operation operation = mock(Operation.class);
      DisksClient mockClient = mock(DisksClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedDisksClient.when(DisksClient::create).thenReturn(mockClient);
      when(mockClient.startAsyncReplicationAsync(any(StartAsyncReplicationDiskRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status status = StartZonalDiskReplication.startZonalDiskAsyncReplication(
              PROJECT_ID,  PRIMARY_DISK_NAME, PRIMARY_ZONE, SECONDARY_DISK_NAME, SECONDARY_ZONE);

      verify(mockClient, times(1))
              .startAsyncReplicationAsync(any(StartAsyncReplicationDiskRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }

  @Test
  public void testStopRegionalDiskAsyncReplication() throws Exception {
    try (MockedStatic<RegionDisksClient> mockedRegionDisksClient =
                 mockStatic(RegionDisksClient.class)) {
      Operation operation = mock(Operation.class);
      RegionDisksClient mockClient = mock(RegionDisksClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedRegionDisksClient.when(RegionDisksClient::create).thenReturn(mockClient);
      when(mockClient.stopAsyncReplicationAsync(any(StopAsyncReplicationRegionDiskRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status status = StopRegionalDiskReplication.stopRegionalDiskAsyncReplication(PROJECT_ID,
              SECONDARY_REGION, SECONDARY_DISK_NAME);

      verify(mockClient, times(1))
              .stopAsyncReplicationAsync(any(StopAsyncReplicationRegionDiskRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }

  @Test
  public void testStopZonalDiskAsyncReplication() throws Exception {
    try (MockedStatic<DisksClient> mockedDisksClient =
                 mockStatic(DisksClient.class)) {
      Operation operation = mock(Operation.class);
      DisksClient mockClient = mock(DisksClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedDisksClient.when(DisksClient::create).thenReturn(mockClient);
      when(mockClient.stopAsyncReplicationAsync(any(StopAsyncReplicationDiskRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status status = StopZonalDiskReplication.stopZonalDiskAsyncReplication(PROJECT_ID,
              SECONDARY_ZONE, SECONDARY_DISK_NAME);

      verify(mockClient, times(1))
              .stopAsyncReplicationAsync(any(StopAsyncReplicationDiskRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }
}