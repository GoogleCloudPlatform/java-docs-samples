/*
 * Copyright 2021 Google LLC
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

// [START compute_instances_create]

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDisk.Type;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.ZoneOperationsClient;
import java.io.IOException;

public class CreateInstance {

  public static void main(String[] args) throws IOException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "your-project-id";
    String zone = "zone-name";
    String instanceName = "instance-name";
    createInstance(project, zone, instanceName);
  }


  // Create a new instance with the provided "instanceName" value in the specified project and zone.
  public static void createInstance(String project, String zone, String instanceName)
      throws IOException, InterruptedException {
    // Below are sample values that can be replaced.
    // machineType: machine type of the VM being created. This value uses the format zones/{zone}/machineTypes/{type_name}. For a list of machine types, see https://cloud.google.com/compute/docs/machine-types
    // sourceImage: path to the operating system image to mount. For details about images you can mount, see https://cloud.google.com/compute/docs/images
    // diskSizeGb: storage size of the boot disk to attach to the instance.
    // networkName: network interface to associate with the instance.
    String machineType = "zones/us-central1-a/machineTypes/n1-standard-1";
    String sourceImage = "projects/debian-cloud/global/images/family/debian-10";
    String diskSizeGb = "10";
    String networkName = "default";

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `instancesClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (InstancesClient instancesClient = InstancesClient.create();
        ZoneOperationsClient zoneOperationsClient = ZoneOperationsClient.create()) {
      // Instance creation requires at least one persistent disk and one network interface.
      AttachedDisk disk =
          AttachedDisk.newBuilder()
              .setBoot(true)
              .setAutoDelete(true)
              .setType(Type.PERSISTENT)
              .setInitializeParams(
                  AttachedDiskInitializeParams.newBuilder().setSourceImage(sourceImage)
                      .setDiskSizeGb(diskSizeGb).build())
              .build();

      // Use the network interface provided in the networkName argument.
      NetworkInterface networkInterface = NetworkInterface.newBuilder().setName(networkName)
          .build();

      // Bind `instanceName`, `machineType`, `disk`, and `networkInterface` to an instance.
      Instance instanceResource =
          Instance.newBuilder()
              .setName(instanceName)
              .setMachineType(machineType)
              .addDisks(disk)
              .addNetworkInterfaces(networkInterface)
              .build();

      System.out.println(String.format("Creating instance: %s at %s ", instanceName, zone));
      // Insert the instance in the specified project and zone.
      Operation response = instancesClient.insert(project, zone, instanceResource);

      if (response.getStatus() == Status.RUNNING) {
        // Wait for the create operation to complete; default timeout is 2 mins
        response = zoneOperationsClient.wait(project, zone, response.getId());
      }

      if (response.hasError()) {
        System.out.println("Instance creation failed ! ! " + response.getError());
        return;
      }
      System.out.println("####### Instance creation complete #######");

    } catch (com.google.api.gax.rpc.UnknownException e) {
      System.out.println("####### Instance creation complete #######");
    }
  }
}
//  [END compute_instances_create]
