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

// [START compute_instances_create_encrypted]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDisk.Type;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.CustomerEncryptionKey;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateEncryptedInstance {

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "your-project-id";
    String zone = "zone-name";
    String instanceName = "instance-name";
    String diskEncryptionKey = "disk-encryption-key"; // Base64 encoded
    createEncryptedInstance(project, zone, instanceName, diskEncryptionKey);
  }


  // Create a new encrypted instance with the provided "instanceName" value and encryption key
  // in the specified project and zone.
  public static void createEncryptedInstance(String project, String zone, String instanceName,
      String diskEncryptionKey)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    /* Below are sample values that can be replaced.
       machineType: machine type of the VM being created.
       (This value uses the format zones/{zone}/machineTypes/{type_name}.
       For a list of machine types, see https://cloud.google.com/compute/docs/machine-types)
       sourceImage: path to the operating system image to mount.
       (For details about images you can mount, see https://cloud.google.com/compute/docs/images)
       diskSizeGb: storage size of the boot disk to attach to the instance.
       networkName: network interface to associate with the instance. */
    String machineType = String.format("zones/%s/machineTypes/n1-standard-1", zone);
    String sourceImage = String
        .format("projects/debian-cloud/global/images/family/%s", "debian-11");
    long diskSizeGb = 10L;
    String networkName = "default";

    /* Initialize client that will be used to send requests. This client only needs to be created
       once, and can be reused for multiple requests. After completing all of your requests, call
       the `instancesClient.close()` method on the client to safely
       clean up any remaining background resources. */
    try (InstancesClient instancesClient = InstancesClient.create()) {
      // Instance creation requires at least one persistent disk and one network interface.
      AttachedDisk disk =
          AttachedDisk.newBuilder()
              .setBoot(true)
              .setAutoDelete(true)
              .setType(Type.PERSISTENT.toString())
              .setInitializeParams(
                  AttachedDiskInitializeParams.newBuilder()
                      .setSourceImage(sourceImage)
                      .setDiskSizeGb(diskSizeGb).build())
              .setDiskEncryptionKey(
                  CustomerEncryptionKey.newBuilder()
                      .setRawKey(diskEncryptionKey).build())
              .build();

      // Use the network interface provided in the networkName argument.
      NetworkInterface networkInterface =
          NetworkInterface.newBuilder()
              .setName(networkName)
              .build();

      // Bind `instanceName`, `machineType`, `disk`, and `networkInterface` to an instance.
      Instance instanceResource =
          Instance.newBuilder()
              .setName(instanceName)
              .setMachineType(machineType)
              .addDisks(disk)
              .addNetworkInterfaces(networkInterface)
              .build();

      System.out.printf("Creating instance: %s at %s ", instanceName, zone);

      // Insert the instance in the specified project and zone.
      InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
          .setProject(project)
          .setZone(zone)
          .setInstanceResource(instanceResource)
          .build();

      OperationFuture<Operation, Operation> operation =
          instancesClient.insertAsync(insertInstanceRequest);

      // Wait for the operation to complete.
      Operation response = operation.get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance creation failed ! ! " + response);
        return;
      }
      System.out.println("Operation Status: " + response.getStatus());
    }
  }
}
//  [END compute_instances_create_encrypted]
