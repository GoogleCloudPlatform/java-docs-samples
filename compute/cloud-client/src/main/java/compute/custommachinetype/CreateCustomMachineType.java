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

package compute.custommachinetype;

// [START compute_custom_machine_type_create]

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateCustomMachineType {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-google-cloud-project-id";
    // Name of the zone to create the instance in. For example: "us-west3-b".
    String zone = "google-cloud-zone";
    // Name of the new virtual machine (VM) instance.
    String instanceName = "instance-name";
    // Machine type of the VM being created. This value must be in the
    // following format: "zones/{zone}/machineTypes/{typeName}".
    // For example: "zones/europe-west3-c/machineTypes/f1-micro"
    // OR
    // It can be a CustomMachineType object, describing a custom type you want to use.
    String machineType = "zones/{zone}/machineTypes/{typeName}";

    createInstanceWithCustomMachineType(projectId, zone, instanceName, machineType);
  }

  // Sends an instance creation request to the Compute Engine API and waits for it to complete
  // and returns the created Instance.
  public static void createInstanceWithCustomMachineType(
      String project, String zone, String instanceName, String machineType)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `instancesClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (InstancesClient instancesClient = InstancesClient.create()) {

      AttachedDisk attachedDisk = AttachedDisk.newBuilder()
          .setInitializeParams(
              // Describe the size and source image of the boot disk to attach to the instance.
              // The list of public images available in Compute Engine can be found here:
              // https://cloud.google.com/compute/docs/images#list_of_public_images_available_on
              AttachedDiskInitializeParams.newBuilder()
                  .setSourceImage(
                      String.format("projects/%s/global/images/family/%s", "debian-cloud",
                          "debian-11"))
                  .setDiskSizeGb(10)
                  .build()
          )
          .setAutoDelete(true)
          .setBoot(true)
          .setType(AttachedDisk.Type.PERSISTENT.name())
          .build();

      // Create the Instance object with the relevant information.
      Instance instance = Instance.newBuilder()
          .setName(instanceName)
          .addDisks(attachedDisk)
          .setMachineType(machineType)
          .addNetworkInterfaces(
              NetworkInterface.newBuilder().setName("global/networks/default").build())
          .build();

      // Create the insert instance request object.
      InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
          .setProject(project)
          .setZone(zone)
          .setInstanceResource(instance)
          .build();

      // Invoke the API with the request object and wait for the operation to complete.
      Operation response = instancesClient.insertAsync(insertInstanceRequest)
          .get(3, TimeUnit.MINUTES);

      // Check for errors.
      if (response.hasError()) {
        System.out.println("Instance creation failed!!" + response);
        return;
      }
      System.out.printf("Instance created : %s", instanceName);
      System.out.println("Operation Status: " + response.getStatus());
    }
  }
}
// [END compute_custom_machine_type_create]