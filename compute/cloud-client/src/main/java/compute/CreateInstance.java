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
import java.util.concurrent.TimeUnit;

public class CreateInstance {

  public static void main(String[] args) throws IOException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample
    String project = "your-project-id";
    String zone = "zone-name";
    String machineName = "machine-name";
    createInstance(project, zone, machineName);
  }


  // creates a new instance with the provided machineName in the specified project and zone
  public static void createInstance(String project, String zone, String machineName)
      throws IOException, InterruptedException {
    // Below are sample values that can be replaced.
    // machineType: should be of the format zones/zone-name/machineTypes/machine-type. Refer: https://cloud.google.com/compute/docs/machine-types
    // sourceImage: path to the image that is to be mounted should be specified. Refer: https://cloud.google.com/compute/docs/images
    // diskSizeGb: the storage size of the disk that will be attached to the instance
    String machineType = "zones/us-central1-a/machineTypes/n1-standard-1";
    String sourceImage = "https://www.googleapis.com/compute/v1/projects/debian-cloud/global/images/debian-7-wheezy-v20150710";
    String diskSizeGb = "10";

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (InstancesClient instancesClient = InstancesClient.create()) {
      // Instance creation requires at least one persistent disk and one network interface
      AttachedDisk disk =
          AttachedDisk.newBuilder()
              .setBoot(true)
              .setAutoDelete(true)
              .setType(Type.PERSISTENT)  // Can be set to either 'SCRATCH' or 'PERSISTENT'
              .setInitializeParams(
                  AttachedDiskInitializeParams.newBuilder().setSourceImage(sourceImage)
                      .setDiskSizeGb(diskSizeGb).build())
              .build();

      // "default" network interface is created automatically for every project
      NetworkInterface networkInterface = NetworkInterface.newBuilder().setName("default").build();

      // Bind machine_name, machine_type, disk and network interface to an instance
      Instance instanceResource =
          Instance.newBuilder()
              .setName(machineName)
              .setMachineType(machineType)
              .addDisks(disk)
              .addNetworkInterfaces(networkInterface)
              .build();

      System.out.println(String.format("Creating instance: %s at %s ", machineName, zone));
      // Inserting the instance in the specified project and zone
      Operation response = instancesClient.insert(project, zone, instanceResource);

      ZoneOperationsClient zoneOperationsClient = ZoneOperationsClient.create();
      // waits for the delete operation to complete
      // timeout is set at 180000 or 3 minutes
      // the operation status will be fetched once in every 3 seconds to avoid spamming the api
      long startTime = System.currentTimeMillis();
      while (response.getStatus() == Status.RUNNING
          && System.currentTimeMillis() - startTime < 180000) {
        response = zoneOperationsClient.get(project, zone, response.getId());
        TimeUnit.SECONDS.sleep(3);
      }

      if (response.hasError()) {
        System.out.println("Instance creation failed ! ! " + response.getError());
        return;
      }
      System.out.println("####### Instance creation complete #######");
    }
  }
}
//  [END compute_instances_create]