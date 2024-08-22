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

import com.google.cloud.compute.v1.StoragePool;
import com.google.cloud.compute.v1.StoragePoolsClient;
import compute.disks.storagepool.CreateHyperdiskStoragePool;
import java.io.IOException;
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
@Timeout(value = 40, unit = TimeUnit.MINUTES)
class CreateHyperdiskStoragePoolIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "europe-west4-a";
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

    STORAGE_POOL_NAME = "test-storage-pool-enc-" + UUID.randomUUID();
  }

  @AfterAll
  public static void cleanup()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {

    // Delete Storage Pool created for testing.
    try (StoragePoolsClient client = StoragePoolsClient.create()) {
      client.deleteAsync(PROJECT_ID, ZONE, STORAGE_POOL_NAME);
    }
  }

  @Test
  public void createHyperdiskStoragePoolTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String poolType = String.format("projects/%s/zones/%s/storagePoolTypes/hyperdisk-balanced",
        PROJECT_ID, ZONE);
    StoragePool storagePool = CreateHyperdiskStoragePool
        .createHyperdiskStoragePool(PROJECT_ID, ZONE, STORAGE_POOL_NAME, poolType,
            "advanced", 10240, 10000, 10240);

    Assert.assertNotNull(storagePool);
    Assert.assertEquals(STORAGE_POOL_NAME, storagePool.getName());
    Assert.assertEquals(10000, storagePool.getPoolProvisionedIops());
    Assert.assertEquals(10240, storagePool.getPoolProvisionedThroughput());
    Assert.assertEquals(10240, storagePool.getPoolProvisionedCapacityGb());
    Assert.assertTrue(storagePool.getStoragePoolType().contains("hyperdisk-balanced"));
    Assert.assertTrue(storagePool.getCapacityProvisioningType().equalsIgnoreCase("advanced"));
    Assert.assertTrue(storagePool.getZone().contains(ZONE));
  }
}





