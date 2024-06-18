// Copyright 2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package compute.disks.storagepool;

// [START compute_hyperdisk_pool_create]

import com.google.cloud.compute.v1.InsertStoragePoolRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.StoragePool;
import com.google.cloud.compute.v1.StoragePoolsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateHyperdiskStoragePool {
  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the zone in which you want to create the storagePool.
    String zone = "europe-central2-b";
    // Name of the storagePool you want to create.
    String storagePoolName = "YOUR_STORAGE_POOL_NAME";
    // The type of disk you want to create. This value uses the following format:
    // "projects/%s/zones/%s/storagePoolTypes/hyperdisk-throughput|hyperdisk-balanced"
    String storagePoolType = "hyperdisk-balanced";
    // Optional: the capacity provisioning type of the storage pool.
    // The allowed values are advanced and standard. If not specified, the value advanced is used.
    String capacityProvisioningType = "advanced";
    // The total capacity to provision for the new storage pool, specified in GiB by default.
    long provisionedCapacity = 128;
    // the IOPS to provision for the storage pool.
    // You can use this flag only with Hyperdisk Balanced Storage Pools.
    long provisionedIops = 3000;
    // the throughput in MBps to provision for the storage pool.
    long provisionedThroughput = 140;

    createHyperdiskStoragePool(projectId, zone, storagePoolName, storagePoolType,
            capacityProvisioningType, provisionedCapacity, provisionedIops, provisionedThroughput);
  }

  // Creates a hyperdisk storagePool in a project
  public static StoragePool createHyperdiskStoragePool(String projectId, String zone,
                                                String storagePoolName, String storagePoolType,
                                                String capacityProvisioningType, long capacity,
                                                long iops, long throughput)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (StoragePoolsClient client = StoragePoolsClient.create()) {
      // Create a storagePool.
      StoragePool resource = StoragePool.newBuilder()
              .setZone(zone)
              .setName(storagePoolName)
              .setStoragePoolType(storagePoolType)
              .setCapacityProvisioningType(capacityProvisioningType)
              .setPoolProvisionedCapacityGb(capacity)
              .setPoolProvisionedIops(iops)
              .setPoolProvisionedThroughput(throughput)
              .build();

      InsertStoragePoolRequest request = InsertStoragePoolRequest.newBuilder()
              .setProject(projectId)
              .setZone(zone)
              .setStoragePoolResource(resource)
              .build();

      // Wait for the insert disk operation to complete.
      Operation operation = client.insertAsync(request).get(1, TimeUnit.MINUTES);

      if (operation.hasError()) {
        System.out.println("StoragePool creation failed!");
        throw new Error(operation.getError().toString());
      }

      // Wait for server update
      TimeUnit.SECONDS.sleep(10);

      StoragePool storagePool = client.get(projectId, zone, storagePoolName);

      System.out.printf("Storage pool '%s' has been created successfully", storagePool.getName());

      return storagePool;
    }
  }
}
// [END compute_hyperdisk_pool_create]