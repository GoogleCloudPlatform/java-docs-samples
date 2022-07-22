// Copyright 2022 Google LLC
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

package compute;

// [START compute_instances_create_with_existing_disks]

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateInstanceWithExistingDisks {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.

    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";

    // Name of the zone to create the instance in. For example: "us-west3-b"
    String zone = "europe-central2-b";

    // Name of the new virtual machine (VM) instance.
    String instanceName = "YOUR_INSTANCE_NAME";

    // Array of disk names to be attached to the new virtual machine.
    // First disk in this list will be used as the boot device.
    List<String> diskNames = new ArrayList<>();

    createInstanceWithExistingDisks(projectId, zone, instanceName, diskNames);
  }

  // Create a new VM instance using selected disks.
  // The first disk in diskNames will be used as boot disk.
  public static void createInstanceWithExistingDisks(String projectId, String zone,
      String instanceName, List<String> diskNames)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `instancesClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (InstancesClient instancesClient = InstancesClient.create();
        DisksClient disksClient = DisksClient.create()) {

      if (diskNames.size() == 0) {
        throw new Error("At least one disk should be provided");
      }

      // Create the list of attached disks to be used in instance creation.
      List<AttachedDisk> attachedDisks = new ArrayList<>();
      Disk disk = null;
      for (String name : diskNames) {
        disk = disksClient.get(projectId, zone, name);
        attachedDisks.add(
            AttachedDisk.newBuilder()
                .setSource(disk.getSelfLink())
                .build()
        );
      }

      // Make the first disk in the list as the boot device.
      attachedDisks.set(0,
          AttachedDisk.newBuilder(attachedDisks.get(0)).setBoot(true).build());

      // Create the instance.
      Instance instance = Instance.newBuilder()
          .setName(instanceName)
          // Add the attached disks to the instance.
          .addAllDisks(attachedDisks)
          .setMachineType(String.format("zones/%s/machineTypes/n1-standard-1", zone))
          .addNetworkInterfaces(
              NetworkInterface.newBuilder().setName("global/networks/default").build())
          .build();

      // Create the insert instance request.
      InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setInstanceResource(instance)
          .build();

      // Wait for the create operation to complete.
      Operation response = instancesClient.insertAsync(insertInstanceRequest)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance creation failed!" + response);
        return;
      }
      System.out.println("Operation Status: " + response.getStatus());

    }
  }
}
// [END compute_instances_create_with_existing_disks]