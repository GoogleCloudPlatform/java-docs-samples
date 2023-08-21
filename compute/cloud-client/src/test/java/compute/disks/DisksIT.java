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
import static compute.Util.getZone;

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.CreateSnapshotDiskRequest;
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Snapshot;
import com.google.cloud.compute.v1.SnapshotsClient;
import compute.DeleteInstance;
import compute.Util;
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
import org.junit.Assert;
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
public class DisksIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String ZONE;

  private static String REGION;
  private static String INSTANCE_NAME;
  private static String DISK_NAME;
  private static String DISK_NAME_2;
  private static String DISK_NAME_DUMMY;
  private static String EMPTY_DISK_NAME;
  private static String SNAPSHOT_NAME;
  private static String DISK_TYPE;

  private static String ZONAL_BLANK_DISK;

  private static String REGIONAL_BLANK_DISK;

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

    ZONE = getZone();
    REGION = ZONE.substring(0, ZONE.length() - 2);
    String uuid = UUID.randomUUID().toString().split("-")[0];
    INSTANCE_NAME = "test-disks-" + uuid;
    DISK_NAME = "gcloud-test-disk-" + uuid;
    DISK_NAME_2 = "gcloud-test-disk2-" + uuid;
    DISK_NAME_DUMMY = "gcloud-test-disk-dummy--" + uuid;
    EMPTY_DISK_NAME = "gcloud-test-disk-empty" + uuid;
    SNAPSHOT_NAME = "gcloud-test-snapshot-" + uuid;
    DISK_TYPE = String.format("zones/%s/diskTypes/pd-ssd", ZONE);
    ZONAL_BLANK_DISK = "gcloud-test-disk-zattach-" + uuid;
    REGIONAL_BLANK_DISK = "gcloud-test-disk-rattach-" + uuid;

    // Cleanup existing stale instances.
    Util.cleanUpExistingInstances("test-disks", PROJECT_ID, ZONE);

    // Create disk from image.
    Image debianImage = null;
    try (ImagesClient imagesClient = ImagesClient.create()) {
      debianImage = imagesClient.getFromFamily("debian-cloud", "debian-11");
    }
    CreateDiskFromImage.createDiskFromImage(PROJECT_ID, ZONE, DISK_NAME, DISK_TYPE, 10,
        debianImage.getSelfLink());
    assertThat(stdOut.toString()).contains("Disk created from image.");

    // Create disk from snapshot.
    CreateDiskFromImage.createDiskFromImage(PROJECT_ID, ZONE, DISK_NAME_DUMMY, DISK_TYPE, 10,
        debianImage.getSelfLink());
    TimeUnit.SECONDS.sleep(10);
    createDiskSnapshot(PROJECT_ID, ZONE, DISK_NAME_DUMMY, SNAPSHOT_NAME);
    String diskSnapshotLink = String.format("projects/%s/global/snapshots/%s", PROJECT_ID,
        SNAPSHOT_NAME);
    TimeUnit.SECONDS.sleep(5);
    CreateDiskFromSnapshot.createDiskFromSnapshot(PROJECT_ID, ZONE, DISK_NAME_2, DISK_TYPE, 10,
        diskSnapshotLink);
    assertThat(stdOut.toString()).contains("Disk created.");

    // Create empty disk.
    CreateEmptyDisk.createEmptyDisk(PROJECT_ID, ZONE, EMPTY_DISK_NAME, DISK_TYPE, 10);
    assertThat(stdOut.toString()).contains("Empty disk created.");

    // Set Disk autodelete.
    createInstance(PROJECT_ID, ZONE, INSTANCE_NAME, DISK_NAME, debianImage.getSelfLink());
    TimeUnit.SECONDS.sleep(10);
    SetDiskAutodelete.setDiskAutodelete(PROJECT_ID, ZONE, INSTANCE_NAME, DISK_NAME, true);
    assertThat(stdOut.toString()).contains("Disk autodelete field updated.");

    // Create zonal and regional blank disks for testing attach and resize.
    createZonalDisk();
    createRegionalDisk();
    TimeUnit.SECONDS.sleep(30);

    stdOut.close();
    System.setOut(out);
  }

  @AfterAll
  public static void cleanUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Delete instance.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_NAME);
    // Delete snapshot.
    try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {
      Operation operation = snapshotsClient.deleteAsync(PROJECT_ID, SNAPSHOT_NAME)
          .get(3, TimeUnit.MINUTES);
      if (operation.hasError()) {
        throw new Error("Error in deleting the snapshot.");
      }
    }
    // Delete disks.
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, DISK_NAME_DUMMY);
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, DISK_NAME_2);
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, EMPTY_DISK_NAME);
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, ZONAL_BLANK_DISK);
    RegionalDelete.deleteRegionalDisk(PROJECT_ID, REGION, REGIONAL_BLANK_DISK);

    stdOut.close();
    System.setOut(out);
  }

  public static void createDiskSnapshot(String project, String zone, String diskName,
      String snapshotName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (DisksClient disksClient = DisksClient.create()) {

      CreateSnapshotDiskRequest createSnapshotDiskRequest = CreateSnapshotDiskRequest.newBuilder()
          .setProject(project)
          .setZone(zone)
          .setDisk(diskName)
          .setSnapshotResource(Snapshot.newBuilder()
              .setName(snapshotName)
              .build())
          .build();

      Operation operation = disksClient.createSnapshotAsync(createSnapshotDiskRequest)
          .get(3, TimeUnit.MINUTES);

      if (operation.hasError()) {
        throw new Error("Failed to create the snapshot");
      }
    }
  }

  public static void createInstance(String projectId, String zone, String instanceName,
      String diskName, String sourceImage)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (InstancesClient instancesClient = InstancesClient.create()) {

      Instance instance = Instance.newBuilder()
          .setName(instanceName)
          .addDisks(AttachedDisk.newBuilder()
              .setDeviceName(diskName)
              .setAutoDelete(false)
              .setBoot(true)
              .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
                  .setDiskSizeGb(10)
                  .setSourceImage(sourceImage)
                  .setDiskName(diskName)
                  .build())
              .build())
          .setMachineType(String.format("zones/%s/machineTypes/n1-standard-1", ZONE))
          .addNetworkInterfaces(NetworkInterface.newBuilder()
              .setName("global/networks/default")
              .build())
          .build();

      InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setInstanceResource(instance)
          .build();

      Operation operation = instancesClient.insertAsync(insertInstanceRequest)
          .get(3, TimeUnit.MINUTES);

      if (operation.hasError()) {
        System.out.println("Failed to create the instance");
      }
    }
  }

  public static void createZonalDisk()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String diskType = String.format("zones/%s/diskTypes/pd-standard", ZONE);
    CreateEmptyDisk.createEmptyDisk(PROJECT_ID, ZONE, ZONAL_BLANK_DISK, diskType, 12);
  }

  public static void createRegionalDisk()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String diskType = String.format("regions/%s/diskTypes/pd-balanced", REGION);
    List<String> replicaZones = Arrays.asList(
        String.format("projects/%s/zones/%s-a", PROJECT_ID, REGION),
        String.format("projects/%s/zones/%s-b", PROJECT_ID, REGION));
    RegionalCreateFromSource.createRegionalDisk(PROJECT_ID, REGION, replicaZones,
        REGIONAL_BLANK_DISK, diskType, 11, Optional.empty(), Optional.empty());
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
  public void testListDisks() throws IOException {
    ListDisks.listDisks(PROJECT_ID, ZONE, "");
    assertThat(stdOut.toString()).contains(DISK_NAME);
    assertThat(stdOut.toString()).contains(DISK_NAME_2);
  }

  @Test
  public void testDiskAttachResize()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Test disk attach.
    Instance instance = Util.getInstance(PROJECT_ID, ZONE, INSTANCE_NAME);
    Assert.assertEquals(1, instance.getDisksCount());

    Disk zonalDisk = Util.getDisk(PROJECT_ID, ZONE, ZONAL_BLANK_DISK);
    Disk regionalDisk = Util.getRegionalDisk(PROJECT_ID, REGION, REGIONAL_BLANK_DISK);

    AttachDisk.attachDisk(PROJECT_ID, ZONE, instance.getName(), zonalDisk.getSelfLink(),
        "READ_ONLY");
    AttachDisk.attachDisk(PROJECT_ID, ZONE, instance.getName(), regionalDisk.getSelfLink(),
        "READ_WRITE");
    TimeUnit.SECONDS.sleep(5);

    instance = Util.getInstance(PROJECT_ID, ZONE, INSTANCE_NAME);
    assertThat(instance.getDisksCount() == 3);

    // Test Disk resize.
    ResizeDisk.resizeDisk(PROJECT_ID, zonalDisk.getZone().split("zones/")[1], zonalDisk.getName(),
        22);
    ResizeRegionalDisk.resizeRegionalDisk(PROJECT_ID, regionalDisk.getRegion().split("regions/")[1],
        regionalDisk.getName(), 23);

    Assert.assertEquals(22, Util.getDisk(PROJECT_ID, ZONE, ZONAL_BLANK_DISK).getSizeGb());
    Assert.assertEquals(23,
        Util.getRegionalDisk(PROJECT_ID, REGION, REGIONAL_BLANK_DISK).getSizeGb());
  }

}
