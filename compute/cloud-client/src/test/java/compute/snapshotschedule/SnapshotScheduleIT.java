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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.DeleteResourcePolicyRequest;
import com.google.cloud.compute.v1.GetResourcePolicyRequest;
import com.google.cloud.compute.v1.InsertResourcePolicyRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.ResourcePoliciesClient;
import com.google.cloud.compute.v1.ResourcePolicy;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
@Timeout(value = 2, unit = TimeUnit.MINUTES)
public class SnapshotScheduleIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION = "us-central1";
  private static final String SCHEDULE_NAME = "test-schedule-" + UUID.randomUUID();
  private static final String SCHEDULE_DESCRIPTION = "Test hourly snapshot schedule";
  private static final int MAX_RETENTION_DAYS = 2;
  private static final String STORAGE_LOCATION = "US";
  private static final String ON_SOURCE_DISK_DELETE = "KEEP_AUTO_SNAPSHOTS";

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
            .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
    CreateSnapshotSchedule.createSnapshotSchedule(
            PROJECT_ID, REGION, SCHEDULE_NAME, SCHEDULE_DESCRIPTION,
            MAX_RETENTION_DAYS, STORAGE_LOCATION, ON_SOURCE_DISK_DELETE);
  }

  @AfterAll
  public static void cleanup()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    DeleteSnapshotSchedule.deleteSnapshotSchedule(PROJECT_ID, REGION, SCHEDULE_NAME);
  }

  @Test
  public void testEditSnapshotSchedule()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Status status = EditSnapshotSchedule.editSnapshotSchedule(
            PROJECT_ID, REGION, SCHEDULE_NAME);

    assertThat(status).isEqualTo(Status.DONE);
  }

  @Test
  public void testListSnapshotSchedules() throws IOException {
    List<ResourcePolicy> list = ListSnapshotSchedules.listSnapshotSchedules(
            PROJECT_ID, REGION, SCHEDULE_NAME);

    assertThat(list.size()).isEqualTo(1);
    assertThat(list.get(0).getName()).isEqualTo(SCHEDULE_NAME);
  }

  @Test
  public void testCreateSnapshotScheduleHourly()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
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

      Status status = CreateSnapshotSchedule.createSnapshotSchedule(
              PROJECT_ID, REGION, SCHEDULE_NAME, SCHEDULE_DESCRIPTION,
              MAX_RETENTION_DAYS, STORAGE_LOCATION, ON_SOURCE_DISK_DELETE);

      verify(mockClient, times(1))
              .insertAsync(any(InsertResourcePolicyRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }

  @Test
  public void testGetSnapshotSchedule() throws IOException {
    try (MockedStatic<ResourcePoliciesClient> mockedResourcePoliciesClient =
                 mockStatic(ResourcePoliciesClient.class)) {
      Operation operation = mock(Operation.class);
      ResourcePoliciesClient mockClient = mock(ResourcePoliciesClient.class);
      ResourcePolicy mockResourcePolicy = mock(ResourcePolicy.class);

      mockedResourcePoliciesClient.when(ResourcePoliciesClient::create).thenReturn(mockClient);
      when(mockClient.get(any(GetResourcePolicyRequest.class)))
              .thenReturn(mockResourcePolicy);

      ResourcePolicy resourcePolicy = GetSnapshotSchedule.getSnapshotSchedule(
              PROJECT_ID, REGION, SCHEDULE_NAME);

      verify(mockClient, times(1))
              .get(any(GetResourcePolicyRequest.class));
      assertEquals(mockResourcePolicy, resourcePolicy);
    }
  }

  @Test
  public void testDeleteSnapshotSchedule()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (MockedStatic<ResourcePoliciesClient> mockedResourcePoliciesClient =
                 mockStatic(ResourcePoliciesClient.class)) {
      Operation operation = mock(Operation.class);
      ResourcePoliciesClient mockClient = mock(ResourcePoliciesClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedResourcePoliciesClient.when(ResourcePoliciesClient::create).thenReturn(mockClient);
      when(mockClient.deleteAsync(any(DeleteResourcePolicyRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status status = DeleteSnapshotSchedule
              .deleteSnapshotSchedule(PROJECT_ID, REGION, SCHEDULE_NAME);

      verify(mockClient, times(1))
              .deleteAsync(any(DeleteResourcePolicyRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }
}