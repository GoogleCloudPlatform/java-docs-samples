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
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.StartAsyncReplicationRegionDiskRequest;
import com.google.cloud.compute.v1.StopAsyncReplicationRegionDiskRequest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
@Timeout(value = 10)
public class DiskReplicationIT {

  private static final String PROJECT_ID = "project-id";
  private static final String PRIMARY_REGION = "us-central1";
  private static final String SECONDARY_REGION = "us-east1";
  private static final String PRIMARY_DISK_NAME = "test-disk-primary";
  private static final String SECONDARY_DISK_NAME = "test-disk-secondary";

  @Test
  public void testStartDiskAsyncReplication() throws Exception {
    try (MockedStatic<RegionDisksClient> mockedRegionDisksClient =
                 mockStatic(RegionDisksClient.class)) {
      Operation operation = mock(Operation.class);
      RegionDisksClient mockClient = mock(RegionDisksClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedRegionDisksClient.when(RegionDisksClient::create).thenReturn(mockClient);
      when(mockClient.startAsyncReplicationAsync(any(StartAsyncReplicationRegionDiskRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Operation.Status.DONE);

      Operation.Status status = StartDiskReplication.startDiskAsyncReplication(
            PROJECT_ID,  PRIMARY_DISK_NAME, PRIMARY_REGION, SECONDARY_DISK_NAME, SECONDARY_REGION);

      verify(mockClient, times(1))
              .startAsyncReplicationAsync(any(StartAsyncReplicationRegionDiskRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Operation.Status.DONE, status);
    }
  }

  @Test
  public void testStopDiskAsyncReplication() throws Exception {
    try (MockedStatic<RegionDisksClient> mockedRegionDisksClient =
                 mockStatic(RegionDisksClient.class)) {
      Operation operation = mock(Operation.class);
      RegionDisksClient mockClient = mock(RegionDisksClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedRegionDisksClient.when(RegionDisksClient::create).thenReturn(mockClient);
      when(mockClient.stopAsyncReplicationAsync(any(StopAsyncReplicationRegionDiskRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Operation.Status.DONE);

      Operation.Status status = StopDiskReplication.stopDiskAsyncReplication(PROJECT_ID,
              SECONDARY_REGION, SECONDARY_DISK_NAME);

      verify(mockClient, times(1))
              .stopAsyncReplicationAsync(any(StopAsyncReplicationRegionDiskRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Operation.Status.DONE, status);
    }
  }
}
