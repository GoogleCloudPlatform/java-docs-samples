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

import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.StoragePool;
import compute.disks.storagepool.CreateDiskInStoragePool;
import compute.disks.storagepool.CreateHyperdiskStoragePool;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@Timeout(value = 40, unit = TimeUnit.MINUTES)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HyperdisksIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  // Zone in which the hyperdisk will be created.
  private static final String ZONE_1 = "europe-west1-b";
  // Zone in which the storage pool will be created.
  private static final String ZONE_2 = "us-central1-a";
  private static String HYPERDISK_NAME;
  private static String HYPERDISK_IN_POOL_NAME;
  private static String STORAGE_POOL_NAME;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
         .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    HYPERDISK_NAME = "test-hyperdisk-enc-" + UUID.randomUUID();
    HYPERDISK_IN_POOL_NAME = "test-hyperdisk-enc-" + UUID.randomUUID();
    STORAGE_POOL_NAME = "test-storage-pool-enc-" + UUID.randomUUID();
  }

  @AfterAll
  public static void cleanup()
       throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Delete all disks created for testing.
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE_1, HYPERDISK_NAME);
    //    DeleteDisk.deleteDisk(PROJECT_ID, ZONE_2, HYPERDISK_IN_POOL_NAME);
    //
    //    try (StoragePoolsClient client = StoragePoolsClient.create()) {
    //      client.deleteAsync(PROJECT_ID, ZONE_2, STORAGE_POOL_NAME);
    //    }
  }

  @Test
  public void stage1_CreateHyperdiskTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String diskType = String.format("zones/%s/diskTypes/hyperdisk-balanced", ZONE_1);

    Disk hyperdisk = CreateHyperdisk
        .createHyperdisk(PROJECT_ID, ZONE_1, HYPERDISK_NAME, diskType,
            10, 3000, 140);

    Assert.assertNotNull(hyperdisk);
    Assert.assertEquals(HYPERDISK_NAME, hyperdisk.getName());
    Assert.assertEquals(3000, hyperdisk.getProvisionedIops());
    Assert.assertEquals(140, hyperdisk.getProvisionedThroughput());
    Assert.assertEquals(10, hyperdisk.getSizeGb());
    Assert.assertTrue(hyperdisk.getType().contains("hyperdisk-balanced"));
    Assert.assertTrue(hyperdisk.getZone().contains(ZONE_1));
  }

  @Disabled
  @Test
  public void stage1_CreateHyperdiskStoragePoolTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String poolType = String.format("projects/%s/zones/%s/storagePoolTypes/hyperdisk-balanced",
        PROJECT_ID, ZONE_2);
    StoragePool storagePool = CreateHyperdiskStoragePool
         .createHyperdiskStoragePool(PROJECT_ID, ZONE_2, STORAGE_POOL_NAME, poolType,
         "advanced", 10240, 10000, 10240);

    Assert.assertNotNull(storagePool);
    Assert.assertEquals(STORAGE_POOL_NAME, storagePool.getName());
    Assert.assertEquals(10000, storagePool.getPoolProvisionedIops());
    Assert.assertEquals(10240, storagePool.getPoolProvisionedThroughput());
    Assert.assertEquals(10240, storagePool.getPoolProvisionedCapacityGb());
    Assert.assertTrue(storagePool.getStoragePoolType().contains("hyperdisk-balanced"));
    Assert.assertTrue(storagePool.getCapacityProvisioningType().equalsIgnoreCase("advanced"));
    Assert.assertTrue(storagePool.getZone().contains(ZONE_2));
  }

  @Disabled
  @Test
  public void stage2_CreateHyperdiskStoragePoolTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String diskType = String.format("zones/%s/diskTypes/hyperdisk-balanced", ZONE_2);
    String storagePoolLink = String
        .format("https://www.googleapis.com/compute/v1/projects/%s/zones/%s/storagePools/%s",
           PROJECT_ID, ZONE_2, STORAGE_POOL_NAME);
    Disk disk = CreateDiskInStoragePool
        .createDiskInStoragePool(PROJECT_ID, ZONE_2, HYPERDISK_IN_POOL_NAME, storagePoolLink,
           diskType, 10, 3000, 140);

    Assert.assertNotNull(disk);
    Assert.assertEquals(HYPERDISK_IN_POOL_NAME, disk.getName());
    Assert.assertTrue(disk.getStoragePool().contains(STORAGE_POOL_NAME));
    Assert.assertEquals(3000, disk.getProvisionedIops());
    Assert.assertEquals(140, disk.getProvisionedThroughput());
    Assert.assertEquals(10, disk.getSizeGb());
    Assert.assertTrue(disk.getType().contains("hyperdisk-balanced"));
    Assert.assertTrue(disk.getZone().contains(ZONE_2));
  }
}