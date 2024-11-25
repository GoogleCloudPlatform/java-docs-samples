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

import com.google.cloud.compute.v1.Disk;
import compute.Util;
import compute.disks.consistencygroup.AddDiskToConsistencyGroup;
import compute.disks.consistencygroup.CreateDiskConsistencyGroup;
import compute.disks.consistencygroup.DeleteDiskConsistencyGroup;
import compute.disks.consistencygroup.StopAsyncReplicationConsistencyGroup;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 3, unit = TimeUnit.MINUTES)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConsistencyGroupIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION = "us-central1";
  private static final String REGION_SECONDARY = "northamerica-northeast1";
  static String randomUUID = UUID.randomUUID().toString().split("-")[0];
  private static final String CONSISTENCY_GROUP_NAME =
      "test-consistency-group-" + UUID.randomUUID();
  private static final String DISK_NAME = "test-disk-for-consistency-" + randomUUID;
  private static final String DISK_TYPE = String.format("regions/%s/diskTypes/pd-balanced", REGION);
  private static final String SECONDARY_REGIONAL_DISK =
          "gcloud-test-disk-secondary-regional-" + randomUUID;
  private static final long DISK_SIZE = 10L;
  private static final List<String> replicaZones = Arrays.asList(
          String.format("projects/%s/zones/%s-a", PROJECT_ID, REGION),
          String.format("projects/%s/zones/%s-b", PROJECT_ID, REGION));

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp() throws Exception {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");

    RegionalCreateFromSource.createRegionalDisk(PROJECT_ID, REGION, replicaZones,
            DISK_NAME, DISK_TYPE, 10, Optional.empty(), Optional.empty());
  }

  @AfterAll
  public static void cleanUp()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    StopDiskReplication.stopDiskAsyncReplication(PROJECT_ID, REGION, DISK_NAME);
    TimeUnit.SECONDS.sleep(30);
    StopDiskReplication.stopDiskAsyncReplication(
            PROJECT_ID, REGION_SECONDARY, SECONDARY_REGIONAL_DISK);

    RegionalDelete.deleteRegionalDisk(PROJECT_ID, REGION, DISK_NAME);
    RegionalDelete.deleteRegionalDisk(PROJECT_ID, REGION_SECONDARY, SECONDARY_REGIONAL_DISK);
    DeleteDiskConsistencyGroup.deleteDiskConsistencyGroup(
            PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);

    // Clean up existing stale resources.
    Util.cleanUpExistingRegionalDisks("test-disk-for-consistency-", PROJECT_ID, REGION);
    Util.cleanUpExistingRegionalDisks(
            "gcloud-test-disk-secondary-regional-", PROJECT_ID, REGION_SECONDARY);
  }

  @Test
  @Order(1)
  public void testCreateDiskConsistencyGroupResourcePolicy()
      throws IOException, ExecutionException, InterruptedException {
    String consistencyGroupLink = CreateDiskConsistencyGroup.createDiskConsistencyGroup(
            PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);

    // Verify that the consistency group was created
    assertNotNull(consistencyGroupLink);
    assertThat(consistencyGroupLink.contains(CONSISTENCY_GROUP_NAME));
  }

  @Test
  @Order(2)
  public void testStopAsyncReplicationConsistencyGroup()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    AddDiskToConsistencyGroup.addDiskToConsistencyGroup(
            PROJECT_ID, REGION, DISK_NAME, CONSISTENCY_GROUP_NAME, REGION);
    Disk disk = CreateDiskSecondaryRegional.createDiskSecondaryRegional(
            PROJECT_ID, PROJECT_ID, DISK_NAME, SECONDARY_REGIONAL_DISK,
            REGION, REGION_SECONDARY, DISK_SIZE,  DISK_TYPE);
    assert disk != null;
    StartDiskReplication.startDiskAsyncReplication(
            PROJECT_ID, DISK_NAME, REGION, disk.getSelfLink());

    StopAsyncReplicationConsistencyGroup.stopReplicationConsistencyGroup(
            PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME, REGION);

    assertThat(stdOut.toString()).contains("Replication stopped for consistency group: ");

    stdOut.close();
  }
}
