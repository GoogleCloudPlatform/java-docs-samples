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

package compute.preemptible;

// [START compute_preemptible_create]

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Scheduling;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CreatePreemptibleInstance {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // projectId: project ID or project number of the Cloud project you want to use.
    // zone: name of the zone you want to use. For example: “us-west3-b”
    // instanceName: name of the new virtual machine.
    String projectId = "your-project-id-or-number";
    String zone = "zone-name";
    String instanceName = "instance-name";

    createPremptibleInstance(projectId, zone, instanceName);
  }

  // Send an instance creation request with preemptible settings to the Compute Engine API
  // and wait for it to complete.
  public static void createPremptibleInstance(String projectId, String zone, String instanceName)
      throws IOException, ExecutionException, InterruptedException {

    String machineType = String.format("zones/%s/machineTypes/e2-small", zone);
    String sourceImage = ""projects/debian-cloud/global/images/family/debian-11";
    long diskSizeGb = 10L;
    String networkName = "default";

    try (InstancesClient instancesClient = InstancesClient.create()) {

      AttachedDisk disk =
          AttachedDisk.newBuilder()
              .setBoot(true)
              .setAutoDelete(true)
              .setType(AttachedDisk.Type.PERSISTENT.toString())
              .setInitializeParams(
                  // Describe the size and source image of the boot disk to attach to the instance.
                  AttachedDiskInitializeParams.newBuilder()
                      .setSourceImage(sourceImage)
                      .setDiskSizeGb(diskSizeGb)
                      .build())
              .build();

      // Use the default VPC network.
      NetworkInterface networkInterface = NetworkInterface.newBuilder()
          .setName(networkName)
          .build();

      // Collect information into the Instance object.
      Instance instanceResource =
          Instance.newBuilder()
              .setName(instanceName)
              .setMachineType(machineType)
              .addDisks(disk)
              .addNetworkInterfaces(networkInterface)
              // Set the preemptible setting.
              .setScheduling(Scheduling.newBuilder()
                  .setPreemptible(true)
                  .build())
              .build();

      System.out.printf("Creating instance: %s at %s %n", instanceName, zone);

      // Prepare the request to insert an instance.
      InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setInstanceResource(instanceResource)
          .build();

      // Wait for the create operation to complete.
      Operation response = instancesClient.insertAsync(insertInstanceRequest).get();

      if (response.hasError()) {
        System.out.println("Instance creation failed ! ! " + response);
        return;
      }

      System.out.printf("Instance created : %s\n", instanceName);
      System.out.println("Operation Status: " + response.getStatus());
    }
  }
}
// [END compute_preemptible_create]
