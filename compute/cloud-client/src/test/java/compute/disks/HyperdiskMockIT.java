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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.InsertDiskRequest;
import com.google.cloud.compute.v1.InsertStoragePoolRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.StoragePool;
import com.google.cloud.compute.v1.StoragePoolsClient;
import compute.disks.storagepool.CreateDiskInStoragePool;
import compute.disks.storagepool.CreateHyperdiskStoragePool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
@Timeout(value = 20)
public class HyperdiskMockIT {
  private static final String PROJECT_ID = "project-id";
  private static final String ZONE = "asia-east1-a";
  private static final String HYPERDISK_IN_POOL_NAME = "hyperdisk";
  private static final String STORAGE_POOL_NAME = "storage-pool";
  private static final String PERFORMANCE_PROVISIONING_TYPE = "advanced";
  private static final String CAPACITY_PROVISIONING_TYPE = "advanced";

  @Test
  public void testCreateHyperdiskStoragePool() throws Exception {
    String poolType = String.format(
            "projects/%s/zones/%s/storagePoolTypes/%s", PROJECT_ID, ZONE, "hyperdisk-balanced");

    StoragePool storagePool = StoragePool.newBuilder()
            .setZone(ZONE)
            .setName(STORAGE_POOL_NAME)
            .setStoragePoolType(poolType)
            .setCapacityProvisioningType(CAPACITY_PROVISIONING_TYPE)
            .setPoolProvisionedCapacityGb(10240)
            .setPoolProvisionedIops(10000)
            .setPoolProvisionedThroughput(1024)
            .setPerformanceProvisioningType(PERFORMANCE_PROVISIONING_TYPE)
            .build();
    try (MockedStatic<StoragePoolsClient> mockedStoragePoolsClient =
                 mockStatic(StoragePoolsClient.class)) {
      StoragePoolsClient mockClient = mock(StoragePoolsClient.class);
      OperationFuture<Operation, Operation> mockFuture =
              mock(OperationFuture.class, Mockito.RETURNS_DEEP_STUBS);
      Operation operation = mock(Operation.class, Mockito.RETURNS_DEEP_STUBS);

      mockedStoragePoolsClient.when(StoragePoolsClient::create).thenReturn(mockClient);
      when(mockClient.insertAsync(any(InsertStoragePoolRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Operation.Status.DONE);
      when(mockClient.get(PROJECT_ID, ZONE, STORAGE_POOL_NAME)).thenReturn(storagePool);


      StoragePool expectedStoragePool = CreateHyperdiskStoragePool
              .createHyperdiskStoragePool(PROJECT_ID, ZONE, STORAGE_POOL_NAME, poolType,
                      CAPACITY_PROVISIONING_TYPE, 10240, 10000, 1024,
                      PERFORMANCE_PROVISIONING_TYPE);

      verify(mockClient, times(1)).insertAsync(any(InsertStoragePoolRequest.class));
      verify(mockFuture, times(1)).get();
      assertEquals(storagePool, expectedStoragePool);
    }
  }

  @Test
  public void testCreateDiskInStoragePool() throws Exception {
    String diskType = String.format("zones/%s/diskTypes/hyperdisk-balanced", ZONE);
    Disk expectedHyperdisk = Disk.newBuilder()
            .setZone(ZONE)
            .setName(HYPERDISK_IN_POOL_NAME)
            .setType(diskType)
            .setSizeGb(10L)
            .setProvisionedIops(3000L)
            .setProvisionedThroughput(140L)
            .build();
    String storagePoolLink = String.format("https://www.googleapis.com/compute/v1/projects/%s/zones/%s/storagePools/%s",
            PROJECT_ID, ZONE, STORAGE_POOL_NAME);

    try (MockedStatic<DisksClient> mockedDisksClient = mockStatic(DisksClient.class)) {
      DisksClient mockClient = mock(DisksClient.class);
      OperationFuture<Operation, Operation> mockFuture =
              mock(OperationFuture.class, Mockito.RETURNS_DEEP_STUBS);
      Operation operation = mock(Operation.class, Mockito.RETURNS_DEEP_STUBS);

      mockedDisksClient.when(DisksClient::create).thenReturn(mockClient);
      when(mockClient.insertAsync(any(InsertDiskRequest.class))).thenReturn(mockFuture);
      when(mockFuture.get()).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Operation.Status.DONE);
      when(mockClient.get(PROJECT_ID, ZONE, HYPERDISK_IN_POOL_NAME)).thenReturn(expectedHyperdisk);


      Disk returnedDisk = CreateDiskInStoragePool
              .createDiskInStoragePool(PROJECT_ID, ZONE, HYPERDISK_IN_POOL_NAME, storagePoolLink,
                      diskType, 10, 3000, 140);

      verify(mockClient, times(1)).insertAsync(any(InsertDiskRequest.class));
      verify(mockFuture, times(1)).get();
      assertEquals(expectedHyperdisk, returnedDisk);
    }
  }
}
