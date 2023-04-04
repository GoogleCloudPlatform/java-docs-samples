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

package compute.customhostname;

// [START compute_instances_create_custom_hostname]

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

public class CreateInstanceWithCustomHostname {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // hostName: Custom hostname of the new VM instance.
    // *    Custom hostnames must conform to RFC 1035 requirements for valid hostnames.
    String project = "your-project-id";
    String zone = "zone-name"; // eg: "us-central1-a"
    String instanceName = "instance-name";
    String hostName = "host.example.com";
    createInstanceWithCustomHostname(project, zone, instanceName, hostName);
  }

  // Creates an instance with custom hostname.
  public static void createInstanceWithCustomHostname(String projectId, String zone,
      String instanceName, String hostName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    //  machineType - Machine type for the VM instance specified in the following format:
    //  *    "zones/{zone}/machineTypes/{type_name}". For example:
    //  *    "zones/europe-west3-c/machineTypes/f1-micro"
    //  *    You can find the list of available machine types by using this gcloud command:
    //  *    $ gcloud compute machine-types list
    //  sourceImage - Path of the disk image you want to use for your boot
    //  *    disk. This image can be one of the public images
    //  *    eg: "projects/...
    //  *    or a private image you have access to.
    //  *    You can check the list of available public images using:
    //  *    $ gcloud compute images list
    //  networkName - Name of the network you want the new instance to use.
    //  *    For example: global/networks/default - if you want to use the default network.
    String machineType = "n1-standard-1";
    String sourceImage = String.format("projects/%s/global/images/family/%s", "debian-cloud",
        "debian-11");
    String networkName = "global/networks/default";

    try (InstancesClient instancesClient = InstancesClient.create()) {
      System.out.printf("Creating the %s instance in %s with hostname %s...", instanceName, zone,
          hostName);

      AttachedDisk disk =
          AttachedDisk.newBuilder()
              .setBoot(true)
              .setAutoDelete(true)
              .setType(AttachedDisk.Type.PERSISTENT.toString())
              .setInitializeParams(
                  // Describe the size and source image of the boot disk to attach to the instance.
                  AttachedDiskInitializeParams.newBuilder()
                      .setSourceImage(sourceImage)
                      .setDiskSizeGb(10).build())
              .build();

      // Use the network interface provided in the networkName argument.
      NetworkInterface networkInterface = NetworkInterface.newBuilder()
          .setName(networkName)
          .build();

      Instance instanceResource = Instance.newBuilder()
          // Custom hostnames are not resolved by the automatically created records
          // provided by Compute Engine internal DNS.
          // You must manually configure the DNS record for your custom hostname.
          .setName(instanceName)
          .setHostname(hostName)
          .addDisks(disk)
          .setMachineType(String.format("zones/%s/machineTypes/%s", zone, machineType))
          .addNetworkInterfaces(networkInterface).build();

      InsertInstanceRequest request = InsertInstanceRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setInstanceResource(instanceResource).build();

      // Wait for the create operation to complete.
      Operation response = instancesClient.insertAsync(request).get(3, TimeUnit.MINUTES);
      ;

      if (response.hasError()) {
        System.out.printf("Instance creation failed for instance: %s ; Response: %s ! ! ",
            instanceName, response);
        return;
      }
      System.out.printf("Instance created : %s", instanceName);
      System.out.printf("Operation Status for instance %s is %s: ", instanceName,
          response.getStatus());
    }

  }

}
// [END compute_instances_create_custom_hostname]