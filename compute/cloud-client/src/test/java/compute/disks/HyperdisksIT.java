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
import compute.Util;
import compute.disks.storagepool.CreateDiskInStoragePool;
import compute.disks.storagepool.CreateHyperdiskStoragePool;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
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
public class HyperdisksIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-a";
  private static final String  HYPERDISK_NAME = "test-hyperdisk-enc-" + UUID.randomUUID();
  private static final String  HYPERDISK_IN_POOL_NAME = "test-hyperdisk-enc-" + UUID.randomUUID();
  private static final String STORAGE_POOL_NAME = "test-storage-pool-enc-" + UUID.randomUUID();
  private static final String PERFORMANCE_PROVISIONING_TYPE = "advanced";
  private static final String CAPACITY_PROVISIONING_TYPE = "advanced";

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
    Util.cleanUpExistingStoragePool("ttest-storage-pool-enc-", PROJECT_ID, ZONE);
    Util.cleanUpExistingDisks("test-hyperdisk-enc-", PROJECT_ID, ZONE);
    Util.cleanUpExistingStoragePool("test-storage-pool-enc-", PROJECT_ID, "us-west1-a");
    Util.cleanUpExistingDisks("test-hyperdisk-enc-", PROJECT_ID, "us-west1-a");
  }

  @AfterAll
  public static void cleanup()
       throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Delete all disks created for testing.
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, HYPERDISK_NAME);
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, HYPERDISK_IN_POOL_NAME);

    Util.deleteStoragePool(PROJECT_ID, ZONE, STORAGE_POOL_NAME);
  }

  @Test
  @Order(1)
  public void testCreateHyperdisk()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String diskType = String.format("zones/%s/diskTypes/hyperdisk-balanced", ZONE);

    Disk hyperdisk = CreateHyperdisk
        .createHyperdisk(PROJECT_ID, ZONE, HYPERDISK_NAME, diskType,
            10, 3000, 140);

    Assert.assertNotNull(hyperdisk);
    Assert.assertEquals(HYPERDISK_NAME, hyperdisk.getName());
    Assert.assertEquals(3000, hyperdisk.getProvisionedIops());
    Assert.assertEquals(140, hyperdisk.getProvisionedThroughput());
    Assert.assertEquals(10, hyperdisk.getSizeGb());
    Assert.assertTrue(hyperdisk.getType().contains("hyperdisk-balanced"));
    Assert.assertTrue(hyperdisk.getZone().contains(ZONE));
  }

  @Test
  @Order(1)
  public void testCreateHyperdiskStoragePool()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String poolType = String.format("projects/%s/zones/%s/storagePoolTypes/hyperdisk-balanced",
        PROJECT_ID, ZONE);
    StoragePool storagePool = CreateHyperdiskStoragePool
         .createHyperdiskStoragePool(PROJECT_ID, ZONE, STORAGE_POOL_NAME, poolType,
             CAPACITY_PROVISIONING_TYPE, 10240, 10000, 1024,
             PERFORMANCE_PROVISIONING_TYPE);

    Assert.assertNotNull(storagePool);
    Assert.assertEquals(STORAGE_POOL_NAME, storagePool.getName());
    Assert.assertEquals(10000, storagePool.getPoolProvisionedIops());
    Assert.assertEquals(1024, storagePool.getPoolProvisionedThroughput());
    Assert.assertEquals(10240, storagePool.getPoolProvisionedCapacityGb());
    Assert.assertTrue(storagePool.getStoragePoolType().contains("hyperdisk-balanced"));
    Assert.assertTrue(storagePool.getCapacityProvisioningType()
        .equalsIgnoreCase(CAPACITY_PROVISIONING_TYPE));
    Assert.assertTrue(storagePool.getPerformanceProvisioningType()
        .equalsIgnoreCase(PERFORMANCE_PROVISIONING_TYPE));
    Assert.assertTrue(storagePool.getZone().contains(ZONE));
  }

  @Test
  @Order(2)
  public void testCreateDiskInStoragePool()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String diskType = String.format("zones/%s/diskTypes/hyperdisk-balanced", ZONE);
    String storagePoolLink = String
        .format("https://www.googleapis.com/compute/v1/projects/%s/zones/%s/storagePools/%s",
           PROJECT_ID, ZONE, STORAGE_POOL_NAME);
    Disk disk = CreateDiskInStoragePool
        .createDiskInStoragePool(PROJECT_ID, ZONE, HYPERDISK_IN_POOL_NAME, storagePoolLink,
           diskType, 10, 3000, 140);

    Assert.assertNotNull(disk);
    Assert.assertEquals(HYPERDISK_IN_POOL_NAME, disk.getName());
    Assert.assertTrue(disk.getStoragePool().contains(STORAGE_POOL_NAME));
    Assert.assertEquals(3000, disk.getProvisionedIops());
    Assert.assertEquals(140, disk.getProvisionedThroughput());
    Assert.assertEquals(10, disk.getSizeGb());
    Assert.assertTrue(disk.getType().contains("hyperdisk-balanced"));
    Assert.assertTrue(disk.getZone().contains(ZONE));
  }
}