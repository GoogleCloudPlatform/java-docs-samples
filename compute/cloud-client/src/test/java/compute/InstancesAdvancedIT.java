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

import static com.google.common.truth.Truth.assertWithMessage;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.Instance.Status;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Snapshot;
import com.google.cloud.compute.v1.SnapshotsClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
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
public class InstancesAdvancedIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String ZONE;
  private static String MACHINE_NAME_PUBLIC_IMAGE;
  private static String MACHINE_NAME_CUSTOM_IMAGE;
  private static String MACHINE_NAME_ADDITIONAL_DISK;
  private static String MACHINE_NAME_SNAPSHOT;
  private static String MACHINE_NAME_SNAPSHOT_ADDITIONAL;
  private static String MACHINE_NAME_SUBNETWORK;
  private static String MACHINE_NAME_EXISTING_DISK;
  private static Disk TEST_DISK;
  private static Image TEST_IMAGE;
  private static Snapshot TEST_SNAPSHOT;
  private static String NETWORK_NAME;
  private static String SUBNETWORK_NAME;

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

    UUID uuid = UUID.randomUUID();
    ZONE = "us-central1-a";
    MACHINE_NAME_PUBLIC_IMAGE = "test-instance-pub-" + uuid;
    MACHINE_NAME_CUSTOM_IMAGE = "test-instance-cust-" + uuid;
    MACHINE_NAME_ADDITIONAL_DISK = "test-instance-add-" + uuid;
    MACHINE_NAME_SNAPSHOT = "test-instance-snap-" + uuid;
    MACHINE_NAME_SNAPSHOT_ADDITIONAL = "test-instance-snapa-" + uuid;
    MACHINE_NAME_SUBNETWORK = "test-instance-subnet-" + uuid;
    MACHINE_NAME_EXISTING_DISK = "test-instance-exis" + uuid;
    NETWORK_NAME = "global/networks/default";
    SUBNETWORK_NAME = "regions/us-central1/subnetworks/default";

    TEST_DISK = createSourceDisk();
    TEST_SNAPSHOT = createSnapshot(TEST_DISK);
    TEST_IMAGE = createImage(TEST_DISK);

    Util.cleanUpExistingInstances("test-instance", PROJECT_ID, ZONE);

    compute.CreateInstancesAdvanced.createFromPublicImage(PROJECT_ID, ZONE,
        MACHINE_NAME_PUBLIC_IMAGE);
    compute.CreateInstancesAdvanced.createFromCustomImage(PROJECT_ID, ZONE,
        MACHINE_NAME_CUSTOM_IMAGE, TEST_IMAGE.getSelfLink());
    compute.CreateInstancesAdvanced.createWithAdditionalDisk(PROJECT_ID, ZONE,
        MACHINE_NAME_ADDITIONAL_DISK);
    compute.CreateInstancesAdvanced.createFromSnapshot(PROJECT_ID, ZONE, MACHINE_NAME_SNAPSHOT,
        TEST_SNAPSHOT.getSelfLink());
    compute.CreateInstancesAdvanced.createWithSnapshottedDataDisk(PROJECT_ID, ZONE,
        MACHINE_NAME_SNAPSHOT_ADDITIONAL, TEST_SNAPSHOT.getSelfLink());
    compute.CreateInstancesAdvanced.createWithSubnetwork(PROJECT_ID, ZONE, MACHINE_NAME_SUBNETWORK,
        NETWORK_NAME, SUBNETWORK_NAME);
    CreateInstanceWithExistingDisks.createInstanceWithExistingDisks(PROJECT_ID, ZONE,
        MACHINE_NAME_EXISTING_DISK, List.of(TEST_DISK.getName()));

    TimeUnit.SECONDS.sleep(10);
    stdOut.close();
    System.setOut(out);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    // Delete all instances created for testing.
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_PUBLIC_IMAGE);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_CUSTOM_IMAGE);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_ADDITIONAL_DISK);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_SNAPSHOT);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_SNAPSHOT_ADDITIONAL);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_SUBNETWORK);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_EXISTING_DISK);

    deleteImage(TEST_IMAGE);
    deleteSnapshot(TEST_SNAPSHOT);
    deleteDisk(TEST_DISK);

    stdOut.close();
    System.setOut(out);
  }

  private static Image getActiveDebian()
      throws IOException {
    try (ImagesClient imagesClient = ImagesClient.create()) {
      return imagesClient.getFromFamily("debian-cloud", "debian-11");
    }
  }

  private static Disk createSourceDisk()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (DisksClient disksClient = DisksClient.create()) {

      Disk disk = Disk.newBuilder()
          .setSourceImage(getActiveDebian().getSelfLink())
          .setName("test-disk-" + UUID.randomUUID())
          .build();

      OperationFuture<Operation, Operation> operation = disksClient.insertAsync(PROJECT_ID, ZONE,
          disk);
      // Wait for the operation to complete.
      operation.get(3, TimeUnit.MINUTES);
      return disksClient.get(PROJECT_ID, ZONE, disk.getName());
    }
  }

  private static void deleteDisk(Disk disk)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (DisksClient disksClient = DisksClient.create()) {
      OperationFuture<Operation, Operation> operation = disksClient.deleteAsync(PROJECT_ID, ZONE,
          disk.getName());
      operation.get(3, TimeUnit.MINUTES);
    }
  }

  private static Snapshot createSnapshot(Disk srcDisk)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (SnapshotsClient snapshotsClient = SnapshotsClient.create();
        DisksClient disksClient = DisksClient.create()) {

      Snapshot snapshot = Snapshot.newBuilder()
          .setName("test-snap-" + UUID.randomUUID())
          .build();

      OperationFuture<Operation, Operation> operation = disksClient.createSnapshotAsync(PROJECT_ID,
          ZONE, srcDisk.getName(),
          snapshot);
      operation.get(3, TimeUnit.MINUTES);
      return snapshotsClient.get(PROJECT_ID, snapshot.getName());
    }
  }

  private static void deleteSnapshot(Snapshot snapshot)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {
      OperationFuture<Operation, Operation> operation = snapshotsClient.deleteAsync(PROJECT_ID,
          snapshot.getName());
      operation.get(3, TimeUnit.MINUTES);
    }
  }

  private static Image createImage(Disk srcDisk)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (ImagesClient imagesClient = ImagesClient.create()) {

      Image image = Image.newBuilder()
          .setName("test-img-" + UUID.randomUUID())
          .setSourceDisk(srcDisk.getSelfLink())
          .build();

      OperationFuture<Operation, Operation> operation = imagesClient.insertAsync(PROJECT_ID, image);
      operation.get(3, TimeUnit.MINUTES);
      return imagesClient.get(PROJECT_ID, image.getName());
    }
  }

  private static void deleteImage(Image image)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (ImagesClient imagesClient = ImagesClient.create()) {
      OperationFuture<Operation, Operation> operation = imagesClient.deleteAsync(PROJECT_ID,
          image.getName());
      operation.get(3, TimeUnit.MINUTES);
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
  public void testCreatePublicImage() throws IOException {
    // Check if the instance was successfully created during the setup.
    String response = Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_PUBLIC_IMAGE);
    Assert.assertEquals(response, Status.RUNNING.toString());
  }

  @Test
  public void testCreateCustomImage() throws IOException {
    // Check if the instance was successfully created during the setup.
    String response = Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_CUSTOM_IMAGE);
    Assert.assertEquals(response, Status.RUNNING.toString());
  }

  @Test
  public void testCreateAdditionalDisk() throws IOException {
    // Check if the instance was successfully created during the setup.
    String response = Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_ADDITIONAL_DISK);
    Assert.assertEquals(response, Status.RUNNING.toString());
  }

  @Test
  public void testCreateFromSnapshot() throws IOException {
    // Check if the instance was successfully created during the setup.
    String response = Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_SNAPSHOT);
    Assert.assertEquals(response, Status.RUNNING.toString());
  }

  @Test
  public void testCreateFromSnapshotAdditional() throws IOException {
    // Check if the instance was successfully created during the setup.
    String response = Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_SNAPSHOT_ADDITIONAL);
    Assert.assertEquals(response, Status.RUNNING.toString());
  }

  @Test
  public void testCreateInSubnetwork() throws IOException {
    // Check if the instance was successfully created during the setup.
    String response = Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_SUBNETWORK);
    Assert.assertEquals(response, Status.RUNNING.toString());
  }

  @Test
  public void testCreateInstanceWithExistingDisks() throws IOException {
    // Check if the instance was successfully created during the setup.
    String response = Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_EXISTING_DISK);
    Assert.assertEquals(response, Status.RUNNING.toString());
  }

}
