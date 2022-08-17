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

package compute.windows.windowsinstances;

// [START compute_create_windows_instance_internal_ip]

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

public class CreateWindowsServerInstanceInternalIp {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // projectId - ID or number of the project you want to use.
    String projectId = "your-google-cloud-project-id";

    // zone - Name of the zone you want to use, for example: us-west3-b
    String zone = "europe-central2-b";

    // instanceName - Name of the new machine.
    String instanceName = "instance-name";

    // networkLink - Name of the network you want the new instance to use.
    //  *   For example: "global/networks/default" represents the network
    //  *   named "default", which is created automatically for each project.
    String networkLink = "global/networks/default";

    // subnetworkLink - Name of the subnetwork you want the new instance to use.
    //  *   This value uses the following format:
    //  *   "regions/{region}/subnetworks/{subnetwork_name}"
    String subnetworkLink = "regions/europe-central2/subnetworks/default";

    createWindowsServerInstanceInternalIp(projectId, zone, instanceName, networkLink,
        subnetworkLink);
  }

  // Creates a new Windows Server instance that has only an internal IP address.
  public static void createWindowsServerInstanceInternalIp(String projectId, String zone,
      String instanceName, String networkLink, String subnetworkLink)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    // machineType - Machine type you want to create in following format:
    //  *    "zones/{zone}/machineTypes/{type_name}". For example:
    //  *    "zones/europe-west3-c/machineTypes/f1-micro"
    //  *    You can find the list of available machine types using:
    //  *    https://cloud.google.com/sdk/gcloud/reference/compute/machine-types/list
    String machineType = "n1-standard-1";
    // sourceImageFamily - Name of the public image family for Windows Server or SQL Server images.
    //  *    https://cloud.google.com/compute/docs/images#os-compute-support
    String sourceImageFamily = "windows-2012-r2";

    // Instantiates a client.
    try (InstancesClient instancesClient = InstancesClient.create()) {

      AttachedDisk attachedDisk = AttachedDisk.newBuilder()
          // Describe the size and source image of the boot disk to attach to the instance.
          .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
              .setDiskSizeGb(64)
              .setSourceImage(
                  String.format("projects/windows-cloud/global/images/family/%s",
                      sourceImageFamily))
              .build())
          .setAutoDelete(true)
          .setBoot(true)
          .setType(AttachedDisk.Type.PERSISTENT.toString())
          .build();

      Instance instance = Instance.newBuilder()
          .setName(instanceName)
          .setMachineType(String.format("zones/%s/machineTypes/%s", zone, machineType))
          .addDisks(attachedDisk)
          .addNetworkInterfaces(NetworkInterface.newBuilder()
              // You must verify or configure routes and firewall rules in your VPC network
              // to allow access to kms.windows.googlecloud.com.
              // More information about access to kms.windows.googlecloud.com: https://cloud.google.com/compute/docs/instances/windows/creating-managing-windows-instances#kms-server

              // Additionally, you must enable Private Google Access for subnets in your VPC network
              // that contain Windows instances with only internal IP addresses.
              // More information about Private Google Access: https://cloud.google.com/vpc/docs/configure-private-google-access#enabling
              .setName(networkLink)
              .setSubnetwork(subnetworkLink)
              .build())
          // If you chose an image that supports Shielded VM, you can optionally change the
          // instance's Shielded VM settings.
          // .setShieldedInstanceConfig(ShieldedInstanceConfig.newBuilder()
          //    .setEnableSecureBoot(true)
          //    .setEnableVtpm(true)
          //    .setEnableIntegrityMonitoring(true)
          //    .build())
          .build();

      InsertInstanceRequest request = InsertInstanceRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setInstanceResource(instance)
          .build();

      // Wait for the operation to complete.
      Operation operation = instancesClient.insertAsync(request).get(5, TimeUnit.MINUTES);

      if (operation.hasError()) {
        System.out.printf("Error in creating instance %s", operation.getError());
        return;
      }

      System.out.printf("Instance created %s", instanceName);
    }
  }
}
// [END compute_create_windows_instance_internal_ip]