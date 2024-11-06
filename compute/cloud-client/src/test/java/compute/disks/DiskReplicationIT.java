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

import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.RegionDisksClient;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

@RunWith(JUnit4.class)
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class DiskReplicationIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-a";
  private static final String REGION = "us-central1";
  private static final String DISK_TYPE =
      String.format("projects/%s/zones/%s/diskTypes/pd-standard", PROJECT_ID, ZONE);
  private static String PRIMARY_DISK_NAME;
  private static String SECONDARY_DISK_NAME;

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

    PRIMARY_DISK_NAME = "test-disk-" + UUID.randomUUID().toString().substring(0, 8);
    SECONDARY_DISK_NAME = "test-disk-" + UUID.randomUUID().toString().substring(0, 8);
    // Create a primary disk to replicate from.
    CreateEmptyDisk.createEmptyDisk(PROJECT_ID, ZONE, PRIMARY_DISK_NAME, DISK_TYPE, 1L);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Delete disks created for testing.
    DeleteDisk.deleteDisk(PROJECT_ID, REGION, SECONDARY_DISK_NAME);
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, PRIMARY_DISK_NAME);
  }

  @Test
  public void testStartDiskAsyncReplication()
      throws IOException, ExecutionException, InterruptedException {
    StartDiskReplication.startDiskAsyncReplication(PROJECT_ID, REGION, PRIMARY_DISK_NAME, REGION,
        SECONDARY_DISK_NAME);

    // Assert that the secondary disk is now replicating from the primary disk.
    try (RegionDisksClient regionDisksClient = RegionDisksClient.create()) {
      Disk disk = regionDisksClient.get(PROJECT_ID, REGION, SECONDARY_DISK_NAME);
      assertThat(disk.getAsyncPrimaryDisk()).isNotNull();
      assertThat(disk.getAsyncPrimaryDisk().getDisk())
          .isEqualTo(String.format("projects/%s/regions/%s/disks/%s", PROJECT_ID, REGION,
              PRIMARY_DISK_NAME));
    }
  }
}
