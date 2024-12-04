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

import com.google.cloud.compute.v1.Operation;
import compute.disks.CreateEmptyDisk;
import compute.disks.DeleteDisk;
import java.io.IOException;
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
@Timeout(value = 6, unit = TimeUnit.MINUTES)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SnapshotScheduleIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "asia-south1-a";
  private static final String REGION = ZONE.substring(0, ZONE.lastIndexOf('-'));
  static String templateUUID = UUID.randomUUID().toString();
  private static final String SCHEDULE_NAME = "test-schedule-" + templateUUID;
  private static final String SCHEDULE_FOR_DISK = "test-schedule-for-disk-" + templateUUID;
  private static final  String SCHEDULE_DESCRIPTION = "Test hourly snapshot schedule";
  private static final int MAX_RETENTION_DAYS = 2;
  private static final String STORAGE_LOCATION = "US";
  private static final  String ON_SOURCE_DISK_DELETE = "KEEP_AUTO_SNAPSHOTS";
  private static final String DISK_NAME = "gcloud-test-disk-" + templateUUID;

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

    String diskType = String.format("zones/%s/diskTypes/pd-standard", ZONE);
    CreateEmptyDisk.createEmptyDisk(PROJECT_ID, ZONE, DISK_NAME, diskType, 12);
    CreateSnapshotSchedule.createSnapshotSchedule(
            PROJECT_ID, REGION, SCHEDULE_FOR_DISK, SCHEDULE_DESCRIPTION,
            MAX_RETENTION_DAYS, STORAGE_LOCATION, ON_SOURCE_DISK_DELETE);
  }

  @AfterAll
  public static void cleanUp()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, DISK_NAME);
    DeleteSnapshotSchedule.deleteSnapshotSchedule(PROJECT_ID, REGION, SCHEDULE_FOR_DISK);
  }

  @Test
  @Order(1)
  public void testCreateSnapshotScheduleHourly()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Operation.Status status = CreateSnapshotSchedule.createSnapshotSchedule(
            PROJECT_ID, REGION, SCHEDULE_NAME, SCHEDULE_DESCRIPTION,
            MAX_RETENTION_DAYS, STORAGE_LOCATION, ON_SOURCE_DISK_DELETE);

    assertThat(status).isEqualTo(Operation.Status.DONE);
  }

  @Test
  @Order(2)
  public void testAttachSnapshotScheduleToDisk()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Operation.Status status = AttachSnapshotScheduleToDisk.attachSnapshotScheduleToDisk(
            PROJECT_ID, ZONE, DISK_NAME, SCHEDULE_FOR_DISK, REGION);

    assertThat(status).isEqualTo(Operation.Status.DONE);
  }

  @Test
  @Order(3)
  public void testRemoveSnapshotScheduleFromDisk()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Operation.Status status = RemoveSnapshotScheduleFromDisk.removeSnapshotScheduleFromDisk(
            PROJECT_ID, ZONE, DISK_NAME, REGION, SCHEDULE_FOR_DISK);

    assertThat(status).isEqualTo(Operation.Status.DONE);
  }

  @Test
  @Order(4)
  public void testDeleteSnapshotSchedule()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Operation.Status status = DeleteSnapshotSchedule
            .deleteSnapshotSchedule(PROJECT_ID, REGION, SCHEDULE_NAME);

    assertThat(status).isEqualTo(Operation.Status.DONE);
  }
}
