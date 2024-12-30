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

package compute.snapshotschedule;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.AddResourcePoliciesDiskRequest;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.RemoveResourcePoliciesDiskRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
@Timeout(value = 6, unit = TimeUnit.MINUTES)
public class SnapshotScheduleIT {
  private static final String PROJECT_ID = "GOOGLE_CLOUD_PROJECT";
  private static final String ZONE = "asia-south1-a";
  private static final String REGION = ZONE.substring(0, ZONE.lastIndexOf('-'));
  private static final String SCHEDULE_FOR_DISK = "test-schedule-for-disk";
  private static final String DISK_NAME = "gcloud-test-disk";

  @Test
  public void testAttachSnapshotScheduleToDisk()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (MockedStatic<DisksClient> mockedDisksClient = mockStatic(DisksClient.class)) {
      DisksClient mockClient = mock(DisksClient.class);
      Operation operation = mock(Operation.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedDisksClient.when(DisksClient::create).thenReturn(mockClient);
      when(mockClient.addResourcePoliciesAsync(any(AddResourcePoliciesDiskRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status actualStatus = AttachSnapshotScheduleToDisk.attachSnapshotScheduleToDisk(
              PROJECT_ID, ZONE, DISK_NAME, SCHEDULE_FOR_DISK, REGION);

      verify(mockClient, times(1)).addResourcePoliciesAsync(any());
      assertEquals(Status.DONE, actualStatus);
    }
  }

  @Test
  public void testRemoveSnapshotScheduleFromDisk()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (MockedStatic<DisksClient> mockedDisksClient = mockStatic(DisksClient.class)) {
      DisksClient mockClient = mock(DisksClient.class);
      Operation operation = mock(Operation.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedDisksClient.when(DisksClient::create).thenReturn(mockClient);
      when(mockClient.removeResourcePoliciesAsync(any(RemoveResourcePoliciesDiskRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status actualStatus = RemoveSnapshotScheduleFromDisk.removeSnapshotScheduleFromDisk(
            PROJECT_ID, ZONE, DISK_NAME, REGION, SCHEDULE_FOR_DISK);

      verify(mockClient, times(1)).removeResourcePoliciesAsync(any());
      assertEquals(Status.DONE, actualStatus);
    }
  }
}
