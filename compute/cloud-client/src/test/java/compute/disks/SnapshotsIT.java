/*
 * Copyright 2022 Google LLC
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

import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.InsertDiskRequest;
import com.google.cloud.compute.v1.InsertRegionDiskRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
public class SnapshotsIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String ZONE;
  private static String LOCATION;
  private static String DISK_NAME;
  private static String REGIONAL_DISK_NAME;
  private static String SNAPSHOT_NAME;
  private static String SNAPSHOT_NAME_DELETE_BY_FILTER;
  private static String SNAPSHOT_NAME_REGIONAL;

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    ZONE = "europe-central2-b";
    LOCATION = "europe-central2";
    String uuid = UUID.randomUUID().toString().split("-")[0];
    DISK_NAME = "gcloud-test-disk-" + uuid;
    REGIONAL_DISK_NAME = "gcloud-regional-test-disk-" + uuid;
    SNAPSHOT_NAME = "gcloud-test-snapshot-" + uuid;
    SNAPSHOT_NAME_DELETE_BY_FILTER = "gcloud-test-snapshot-dbf-" + uuid;
    SNAPSHOT_NAME_REGIONAL = "gcloud-test-regional-snap-" + uuid;

    Image debianImage = null;
    try (ImagesClient imagesClient = ImagesClient.create()) {
      debianImage = imagesClient.getFromFamily("debian-cloud", "debian-11");
    }

    // Create zonal snapshot.
    createDisk(PROJECT_ID, ZONE, DISK_NAME, debianImage.getSelfLink());
    CreateSnapshot.createSnapshot(PROJECT_ID, DISK_NAME, SNAPSHOT_NAME, ZONE, "", LOCATION, "");
    CreateSnapshot.createSnapshot(PROJECT_ID, DISK_NAME, SNAPSHOT_NAME_DELETE_BY_FILTER, ZONE, "",
        LOCATION, "");
    assertThat(stdOut.toString()).contains("Snapshot created: " + SNAPSHOT_NAME);
    assertThat(stdOut.toString()).contains("Snapshot created: " + SNAPSHOT_NAME_DELETE_BY_FILTER);

    // Create regional snapshot.
    createRegionalDisk(PROJECT_ID, LOCATION, REGIONAL_DISK_NAME);
    CreateSnapshot.createSnapshot(PROJECT_ID, REGIONAL_DISK_NAME, SNAPSHOT_NAME_REGIONAL, "",
        LOCATION, LOCATION, "");
    assertThat(stdOut.toString()).contains("Snapshot created: " + SNAPSHOT_NAME_REGIONAL);

    stdOut.close();
    System.setOut(out);
  }

  @AfterAll
  public static void cleanUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    deleteDisk(PROJECT_ID, ZONE, DISK_NAME);
    deleteRegionalDisk(PROJECT_ID, LOCATION, REGIONAL_DISK_NAME);
    DeleteSnapshot.deleteSnapshot(PROJECT_ID, SNAPSHOT_NAME);
    DeleteSnapshot.deleteSnapshot(PROJECT_ID, SNAPSHOT_NAME_REGIONAL);

    stdOut.close();
    System.setOut(out);
  }

  public static void createDisk(String projectId, String zone, String diskName, String sourceImage)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (DisksClient disksClient = DisksClient.create()) {

      InsertDiskRequest insertDiskRequest = InsertDiskRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setDiskResource(Disk.newBuilder()
              .setSourceImage(sourceImage)
              .setName(diskName)
              .build())
          .build();

      Operation operation = disksClient.insertAsync(insertDiskRequest).get(3, TimeUnit.MINUTES);

      if (operation.hasError()) {
        throw new Error("Failed to create disk.");
      }
    }
  }

  public static void createRegionalDisk(String projectId, String region, String diskName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    try (RegionDisksClient regionDisksClient = RegionDisksClient.create()) {

      Disk disk = Disk.newBuilder()
          .setSizeGb(200)
          .setName(diskName)
          .addAllReplicaZones(
              List.of(
                  String.format("projects/%s/zones/europe-central2-a", projectId),
                  String.format("projects/%s/zones/europe-central2-b", projectId))
          )
          .build();

      InsertRegionDiskRequest insertRegionDiskRequest = InsertRegionDiskRequest.newBuilder()
          .setProject(projectId)
          .setRegion(region)
          .setDiskResource(disk)
          .build();

      Operation operation = regionDisksClient.insertAsync(insertRegionDiskRequest)
          .get(3, TimeUnit.MINUTES);

      if (operation.hasError()) {
        throw new Error("Failed to create regional disk.");
      }
    }
  }

  public static void deleteDisk(String projectId, String zone, String diskName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (DisksClient disksClient = DisksClient.create()) {

      Operation operation = disksClient.deleteAsync(projectId, zone, diskName)
          .get(3, TimeUnit.MINUTES);

      if (operation.hasError()) {
        throw new Error("Failed to delete disk.");
      }
    }
  }

  public static void deleteRegionalDisk(String projectId, String region, String diskName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    try (RegionDisksClient regionDisksClient = RegionDisksClient.create()) {

      Operation operation = regionDisksClient.deleteAsync(projectId, region, diskName)
          .get(3, TimeUnit.MINUTES);

      if (operation.hasError()) {
        throw new Error("Failed to delete regional disk.");
      }
    }
  }

  @BeforeEach
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @AfterEach
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }

  @Test
  public void testListSnapshots() throws IOException {
    ListSnapshots.listSnapshots(PROJECT_ID, "");
    assertThat(stdOut.toString()).contains(SNAPSHOT_NAME);
    assertThat(stdOut.toString()).contains(SNAPSHOT_NAME_REGIONAL);
  }

  @Test
  public void testGetSnapshot() throws IOException {
    GetSnapshot.getSnapshot(PROJECT_ID, SNAPSHOT_NAME);
    assertThat(stdOut.toString()).contains("Retrieved the snapshot: ");
  }

  @Test
  public void testDeleteSnapshotsByFilter()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    DeleteSnapshotsByFilter.deleteSnapshotsByFilter(PROJECT_ID,
        "name = " + SNAPSHOT_NAME_DELETE_BY_FILTER);
    assertThat(stdOut.toString()).contains("Snapshot deleted: " + SNAPSHOT_NAME_DELETE_BY_FILTER);
  }

}
