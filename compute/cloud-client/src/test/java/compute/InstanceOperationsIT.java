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

package compute;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static compute.Util.getZone;

import com.google.cloud.compute.v1.CreateSnapshotRegionDiskRequest;
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.Instance.Status;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.Snapshot;
import compute.disks.CloneEncryptedDisk;
import compute.disks.CreateEncryptedDisk;
import compute.disks.DeleteDisk;
import compute.disks.DeleteSnapshot;
import compute.disks.RegionalCreateFromSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
public class InstanceOperationsIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = getZone();
  private static final String REGION = ZONE.substring(0, ZONE.length() - 2);
  private static String MACHINE_NAME;
  private static String MACHINE_NAME_ENCRYPTED;
  private static String DISK_NAME;
  private static String ENCRYPTED_DISK_NAME;
  private static String RAW_KEY;
  private static String INSTANCE_NAME;
  private static final String DISK_TYPE = String.format("regions/%s/diskTypes/pd-standard", REGION);
  private static String REPLICATED_DISK_NAME;
  private static String SNAPSHOT_NAME;
  private static final String DISK_SNAPSHOT_LINK =
          String.format("projects/%s/global/snapshots/%s", PROJECT_ID, SNAPSHOT_NAME);
  private static final List<String> REPLICA_ZONES = Arrays.asList(
          String.format("projects/%s/zones/%s-a", PROJECT_ID, REGION),
          String.format("projects/%s/zones/%s-b", PROJECT_ID, REGION));

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    MACHINE_NAME = "test-instance-operation-" + UUID.randomUUID();
    MACHINE_NAME_ENCRYPTED = "test-instance-encrypted-" + UUID.randomUUID();
    DISK_NAME = "test-clone-disk-enc-" + UUID.randomUUID();
    ENCRYPTED_DISK_NAME = "test-disk-enc-" + UUID.randomUUID();
    RAW_KEY = Util.getBase64EncodedKey();
    INSTANCE_NAME = "test-instance-" + UUID.randomUUID();
    REPLICATED_DISK_NAME = "test-disk-replicated-" + UUID.randomUUID();
    SNAPSHOT_NAME = "test-snapshot-" + UUID.randomUUID().toString().split("-")[0];

    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    compute.CreateEncryptedInstance
        .createEncryptedInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED, RAW_KEY);
    RegionalCreateFromSource.createRegionalDisk(PROJECT_ID, REGION, REPLICA_ZONES,
            REPLICATED_DISK_NAME, DISK_TYPE, 200, Optional.empty(), Optional.empty());
    createDiskSnapshot(PROJECT_ID, REGION, REPLICATED_DISK_NAME, SNAPSHOT_NAME);

    TimeUnit.SECONDS.sleep(30);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Cleanup existing stale resources.
    Util.cleanUpExistingInstances("test-instance-", PROJECT_ID, ZONE);
    Util.cleanUpExistingDisks("test-clone-disk-enc-", PROJECT_ID, ZONE);
    Util.cleanUpExistingDisks("test-disk-enc-", PROJECT_ID, ZONE);
    Util.cleanUpExistingRegionalDisks("test-disk-replicated-", PROJECT_ID, REGION);
    Util.cleanUpExistingSnapshots("test-snapshot-", PROJECT_ID);

    // Delete all instances created for testing.
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_NAME);
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, DISK_NAME);
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, ENCRYPTED_DISK_NAME);
    DeleteSnapshot.deleteSnapshot(PROJECT_ID, SNAPSHOT_NAME);
  }

  private static Instance getInstance(String machineName) throws IOException {
    try (InstancesClient instancesClient = InstancesClient.create()) {
      return instancesClient.get(PROJECT_ID, ZONE, machineName);
    }
  }

  public static void createDiskSnapshot(String project, String region, String diskName,
                                        String snapshotName)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (RegionDisksClient disksClient = RegionDisksClient.create()) {

      CreateSnapshotRegionDiskRequest createSnapshotDiskRequest =
              CreateSnapshotRegionDiskRequest.newBuilder()
              .setProject(project)
              .setRegion(region)
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

  @Test
  public void testInstanceOperations()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Assert.assertEquals(Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME),
        Status.RUNNING.toString());

    // Stopping the instance.
    StopInstance.stopInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    LocalDateTime endTime = LocalDateTime.now().plusMinutes(3);
    while (!Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME)
        .equalsIgnoreCase(Status.STOPPED.toString())
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertEquals(Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME),
        Status.TERMINATED.toString());

    // Change machine type.
    Assert.assertFalse(getInstance(MACHINE_NAME).getMachineType().endsWith("e2-standard-2"));
    ChangeInstanceMachineType.changeMachineType(PROJECT_ID, ZONE, MACHINE_NAME, "e2-standard-2");
    Assert.assertTrue(getInstance(MACHINE_NAME).getMachineType().endsWith("e2-standard-2"));

    // Starting the instance.
    StartInstance.startInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    endTime = LocalDateTime.now().plusMinutes(3);
    while (!Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME)
        .equalsIgnoreCase(Status.RUNNING.toString())
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertEquals(Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME),
        Status.RUNNING.toString());
  }

  @Test
  public void testEncryptedInstanceOperations()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Assert.assertEquals(Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED),
        Status.RUNNING.toString());

    // Stopping the encrypted instance.
    StopInstance.stopInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    LocalDateTime endTime = LocalDateTime.now().plusMinutes(3);
    while (!Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED)
        .equalsIgnoreCase(Status.STOPPED.toString())
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertEquals(Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED),
        Status.TERMINATED.toString());

    // Starting the encrypted instance.
    StartEncryptedInstance
        .startEncryptedInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED, RAW_KEY);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    endTime = LocalDateTime.now().plusMinutes(3);
    while (!Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED)
        .equalsIgnoreCase(Status.RUNNING.toString())
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertEquals(Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED),
        Status.RUNNING.toString());
  }

  @Test
  public void testCloneEncryptedDisk()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    Instance instance = getInstance(MACHINE_NAME_ENCRYPTED);
    String diskType = String.format("zones/%s/diskTypes/pd-standard", ZONE);
    CloneEncryptedDisk.createDiskFromCustomerEncryptedKey(PROJECT_ID, ZONE, DISK_NAME, diskType, 10,
        instance.getDisks(0).getSource(), RAW_KEY.getBytes(
            StandardCharsets.UTF_8));
    assertThat(stdOut.toString()).contains("Disk cloned with customer encryption key.");

    stdOut.close();
  }

  @Test
  public void testCreateEncryptedDisk()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String diskType = String.format("zones/%s/diskTypes/pd-standard", ZONE);
    byte[] rawKeyBytes = RAW_KEY.getBytes(StandardCharsets.UTF_8);

    Disk encryptedDisk = CreateEncryptedDisk
            .createEncryptedDisk(PROJECT_ID, ZONE, ENCRYPTED_DISK_NAME, diskType, 10, rawKeyBytes);

    Assert.assertNotNull(encryptedDisk);
    Assert.assertEquals(ENCRYPTED_DISK_NAME, encryptedDisk.getName());
    Assert.assertNotNull(encryptedDisk.getDiskEncryptionKey());
    Assert.assertNotNull(encryptedDisk.getDiskEncryptionKey().getSha256());
  }

  @Test
  public void testCreateInstanceWithRegionalDiskFromSnapshot()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Operation.Status status = CreateInstanceWithRegionalDiskFromSnapshot
                  .createInstanceWithRegionalDiskFromSnapshot(
          PROJECT_ID, ZONE, INSTANCE_NAME, REPLICATED_DISK_NAME,
                  DISK_TYPE, DISK_SNAPSHOT_LINK, REPLICA_ZONES);

    assertThat(status).isEqualTo(Operation.Status.DONE);
  }
}
