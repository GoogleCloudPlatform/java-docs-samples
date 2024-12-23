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

package compute;

// [START compute_instance_create_replicated_boot_disk]
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateInstanceWithRegionalDiskFromSnapshot {

  public static void main(String[] args) throws IOException, ExecutionException,
          InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the zone in which you want to create the instance.
    String zone = "us-central1-a";
    // Name of the instance you want to create.
    String instanceName = "YOUR_INSTANCE_NAME";
    // Name for the replicated disk.
    String diskName = "YOUR_REPLICATED_DISK_NAME";
    String region = zone.substring(0, zone.length() - 2);
    // Type of the disk.
    String diskType = String.format(
            "projects/%s/regions/%s/diskTypes/pd-standard", projectId, region);
    // The full path and name of the snapshot that you want to use as the source for the new disk.
    String snapshotLink = String.format("projects/%s/global/snapshots/%s", projectId,
            "SNAPSHOT_NAME");
    // An iterable collection of zone names in which you want to keep
    // the new disks' replicas. One of the replica zones of the clone must match
    // the zone of the source disk.
    List<String> replicaZones = new ArrayList<>();

    createInstanceWithRegionalDiskFromSnapshot(projectId, zone, instanceName, diskName, diskType,
            snapshotLink, replicaZones);
  }

  // Creates a new VM instance with regional disk from a snapshot and specifies replica zones.
  public static Status createInstanceWithRegionalDiskFromSnapshot(
      String projectId, String zone, String instanceName, String diskName,
      String diskType, String snapshotLink, List<String> replicaZones)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (InstancesClient instancesClient = InstancesClient.create()) {
      AttachedDiskInitializeParams initializeParams = AttachedDiskInitializeParams.newBuilder()
              .setSourceSnapshot(snapshotLink)
              .setDiskType(diskType)
              .setDiskName(diskName)
              .addAllReplicaZones(replicaZones)
              .build();

      // Boot disk configuration
      AttachedDisk bootDisk = AttachedDisk.newBuilder()
              .setBoot(true)
              .setAutoDelete(true)  // Optional: Delete disk when instance is deleted.
              .setType(AttachedDisk.Type.PERSISTENT.toString())
              .setInitializeParams(initializeParams)
              .build();

      // Network interface configuration (using the default network)
      NetworkInterface networkInterface = NetworkInterface.newBuilder()
              .setNetwork("global/networks/default")
              .build();

      // Create the instance resource
      Instance instanceResource = Instance.newBuilder()
              .setName(instanceName)
              .setMachineType(String.format("zones/%s/machineTypes/n1-standard-1", zone))
              .addDisks(bootDisk)
              .addNetworkInterfaces(networkInterface)
              .build();

      Operation response = instancesClient.insertAsync(projectId, zone, instanceResource).get(3,
              TimeUnit.MINUTES);

      if (response.hasError()) {
        throw new Error("Error creating instance! " + response.getError());
      }
      return response.getStatus();
    }
  }
}
// [END compute_instance_create_replicated_boot_disk]