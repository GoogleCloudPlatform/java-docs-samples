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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.BulkInsertRegionDiskRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import compute.disks.consistencygroup.CloneDisksFromConsistencyGroup;
import compute.disks.consistencygroup.CreateDiskConsistencyGroup;
import compute.disks.consistencygroup.DeleteDiskConsistencyGroup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class ConsistencyGroupIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION = "us-central1";
  private static final String CONSISTENCY_GROUP_NAME =
      "test-consistency-group-" + UUID.randomUUID();


  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp() throws Exception {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @AfterAll
  public static void cleanUp()
      throws IOException, ExecutionException, InterruptedException {
    // Delete created consistency group
    DeleteDiskConsistencyGroup.deleteDiskConsistencyGroup(
        PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);
  }

  @Test
  public void testCreateDiskConsistencyGroupResourcePolicy()
      throws IOException, ExecutionException, InterruptedException {
    String consistencyGroupLink = CreateDiskConsistencyGroup.createDiskConsistencyGroup(
            PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);

    // Verify that the consistency group was created
    assertNotNull(consistencyGroupLink);
    assertThat(consistencyGroupLink.contains(CONSISTENCY_GROUP_NAME));
  }

  @Test
  public void testCloneDisksFromConsistencyGroup()
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
    try (MockedStatic<RegionDisksClient> mockedRegionDisksClient = mockStatic(RegionDisksClient.class)) {
      Operation mockOperation = mock(Operation.class);
      RegionDisksClient mockClient = mock(RegionDisksClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedRegionDisksClient.when(RegionDisksClient::create).thenReturn(mockClient);
      when(mockClient.bulkInsertAsync(any(BulkInsertRegionDiskRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(mockOperation);

      CloneDisksFromConsistencyGroup.cloneDisksFromConsistencyGroup(PROJECT_ID, REGION,
              CONSISTENCY_GROUP_NAME, REGION);


      assertThat(bout).isEqualTo(String.format("Disks cloned from consistency group: %s", CONSISTENCY_GROUP_NAME));
      verify(mockClient, times(1))
              .bulkInsertAsync(any(BulkInsertRegionDiskRequest.class));
    }
  }
}
