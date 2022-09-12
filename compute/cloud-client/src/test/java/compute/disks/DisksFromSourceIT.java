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

import com.google.cloud.compute.v1.CreateSnapshotDiskRequest;
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Snapshot;
import com.google.cloud.compute.v1.SnapshotsClient;
import com.google.cloud.kms.v1.CryptoKey;
import com.google.cloud.kms.v1.CryptoKey.CryptoKeyPurpose;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.kms.v1.KeyRing;
import compute.Util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.StreamSupport;
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
public class DisksFromSourceIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String ZONE;
  private static String REGION;
  private static CryptoKey CRYPTO_KEY;
  private static Image DEBIAN_IMAGE;
  private static String KMS_KEYRING_NAME;
  private static String KMS_KEY_NAME;
  private static String KMS_ENCRYPTED_DISK_NAME;
  private static String KMS_CLONE_ENCRYPTED_DISK_NAME;
  private static String DISK_FROM_IMAGE;
  private static String DISK_FROM_DISK;
  private static String DISK_NAME_REGIONAL;
  private static String SNAPSHOT_NAME_REGIONAL;
  private static String DISK_TYPE;

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

    ZONE = "us-central1-a";
    REGION = "us-central1";
    KMS_KEYRING_NAME = "compute-test-keyring";
    KMS_KEY_NAME = "compute-test-key";

    String uuid = UUID.randomUUID().toString().split("-")[0];
    KMS_ENCRYPTED_DISK_NAME = "test-disk-name-kms-enc" + uuid;
    KMS_CLONE_ENCRYPTED_DISK_NAME = "test-disk-name-kms-clone-enc" + uuid;
    DISK_FROM_IMAGE = "test-disk-from-image" + uuid;
    DISK_FROM_DISK = "test-disk-from-disk" + uuid;
    DISK_NAME_REGIONAL = "test-disk-name-regional" + uuid;
    SNAPSHOT_NAME_REGIONAL = "test-snapshot-name-from-source" + uuid;
    DISK_TYPE = String.format("zones/%s/diskTypes/pd-standard", ZONE);

    // Cleanup existing stale instances.
    Util.cleanUpExistingInstances("test-disk", PROJECT_ID, ZONE);

    // Create disk from image.
    DEBIAN_IMAGE = null;
    try (ImagesClient imagesClient = ImagesClient.create()) {
      DEBIAN_IMAGE = imagesClient.getFromFamily("debian-cloud", "debian-11");
    }

    // Create KMS Encrypted disk.
    // The service account service-{PROJECT_ID}@compute-system.iam.gserviceaccount.com needs to
    // have the cloudkms.cryptoKeyVersions.useToEncrypt permission to execute this test.
    CRYPTO_KEY = createKmsKey();
    CreateKmsEncryptedDisk.createKmsEncryptedDisk(PROJECT_ID, ZONE, KMS_ENCRYPTED_DISK_NAME,
        DISK_TYPE, 25, CRYPTO_KEY.getName(), "", DEBIAN_IMAGE.getSelfLink());
    assertThat(stdOut.toString()).contains(
        "Disk created with KMS encryption key. Operation Status: ");

    // Create Regional disk.
    CreateDiskFromImage.createDiskFromImage(PROJECT_ID, ZONE, DISK_FROM_IMAGE, DISK_TYPE, 20,
        DEBIAN_IMAGE.getSelfLink());
    List<String> replicaZones = new ArrayList<>(
        Arrays.asList(String.format("projects/%s/zones/%s-a", PROJECT_ID, REGION),
            String.format("projects/%s/zones/%s-b", PROJECT_ID, REGION)));
    createDiskSnapshot(PROJECT_ID, ZONE, DISK_FROM_IMAGE, SNAPSHOT_NAME_REGIONAL);
    TimeUnit.SECONDS.sleep(10);
    RegionalCreateFromSource.createRegionalDisk(PROJECT_ID, REGION, replicaZones,
        DISK_NAME_REGIONAL, String.format("regions/%s/diskTypes/pd-balanced", REGION), 25, "",
        getSnapshot(SNAPSHOT_NAME_REGIONAL).getSelfLink());
    assertThat(stdOut.toString()).contains("Regional disk created.");

    stdOut.close();
    System.setOut(out);
  }

  @AfterAll
  public static void cleanUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Delete snapshot.
    try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {
      Operation operationSnapFromSource = snapshotsClient.deleteAsync(PROJECT_ID,
              SNAPSHOT_NAME_REGIONAL)
          .get(3, TimeUnit.MINUTES);
      if (operationSnapFromSource.hasError()) {
        throw new Error("Error in deleting the snapshot.");
      }
    }
    // Delete disks.
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, KMS_CLONE_ENCRYPTED_DISK_NAME);
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, KMS_ENCRYPTED_DISK_NAME);
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, DISK_FROM_DISK);
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, DISK_FROM_IMAGE);
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, DISK_NAME_REGIONAL);

    stdOut.close();
    System.setOut(out);
  }

  public static CryptoKey createKmsKey() throws IOException, InterruptedException {
    String location = String.format("projects/%s/locations/global", PROJECT_ID);
    String keyringLink = String.format("projects/%s/locations/global/keyRings/%s", PROJECT_ID,
        KMS_KEYRING_NAME);
    String keyName = String.format("%s/cryptoKeys/%s", keyringLink, KMS_KEY_NAME);

    try (KeyManagementServiceClient kmsClient = KeyManagementServiceClient.create()) {
      // Check if the Key ring is already present.
      boolean isKeyRingPresent = StreamSupport.stream(
              kmsClient.listKeyRings(location).iterateAll().spliterator(), false)
          .anyMatch(keyRing -> keyRing.getName().equalsIgnoreCase(keyringLink));

      // If not, create a new key ring.
      if (!isKeyRingPresent) {
        kmsClient.createKeyRing(location, KMS_KEYRING_NAME,
            KeyRing.newBuilder().build());
      }

      TimeUnit.SECONDS.sleep(10);
      // Check if the key is already present.
      boolean isKeyPresent = StreamSupport.stream(
              kmsClient.listCryptoKeys(keyringLink).iterateAll().spliterator(), false)
          .anyMatch(key -> key.getName().equalsIgnoreCase(keyName));

      // If not, create a new key.
      if (!isKeyPresent) {
        kmsClient.createCryptoKey(keyringLink, KMS_KEY_NAME,
            CryptoKey.newBuilder()
                .setPurpose(CryptoKeyPurpose.ENCRYPT_DECRYPT)
                .build());
      }
      return kmsClient.getCryptoKey(keyName);
    }
  }

  private static Disk getDisk(String diskName) throws IOException {
    try (DisksClient disksClient = DisksClient.create()) {
      return disksClient.get(PROJECT_ID, ZONE, diskName);
    }
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

  public static Snapshot getSnapshot(String snapshotName) throws IOException {
    try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {
      return snapshotsClient.get(PROJECT_ID, snapshotName);
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
  public void testCloneEncryptedDiskManagedKey()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CloneEncryptedDiskManagedKey.createDiskFromKmsEncryptedDisk(PROJECT_ID, ZONE,
        KMS_CLONE_ENCRYPTED_DISK_NAME, DISK_TYPE, 25,
        getDisk(KMS_ENCRYPTED_DISK_NAME).getSelfLink(), CRYPTO_KEY.getName());
    assertThat(stdOut.toString()).contains(
        "Disk cloned with KMS encryption key. Operation Status: ");
  }

  @Test
  public void testCreateFromSource()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateFromSource.createDiskFromDisk(PROJECT_ID, ZONE, DISK_FROM_DISK, DISK_TYPE, 24,
        getDisk(DISK_FROM_IMAGE).getSelfLink());
    assertThat(stdOut.toString()).contains("Disk created from source. Operation Status: ");
  }

}
