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

// [START compute_create_windows_instance_external_ip]

import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CreateWindowsServerInstanceExternalIp {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // projectId - ID or number of the project you want to use.
    String projectId = "your-google-cloud-project-id";

    // zone - Name of the zone you want to use, for example: us-west3-b
    String zone = "europe-central2-b";

    // instanceName - Name of the new machine.
    String instanceName = "instance-name";

    createWindowsServerInstanceExternalIp(projectId, zone, instanceName);
  }

  // Creates a new Windows Server instance that has an external IP address.
  public static void createWindowsServerInstanceExternalIp(String projectId, String zone,
      String instanceName)
      throws IOException, ExecutionException, InterruptedException {

    // machineType - Machine type you want to create in following format:
    //  *    "zones/{zone}/machineTypes/{type_name}". For example:
    //  *    "zones/europe-west3-c/machineTypes/f1-micro"
    //  *    You can find the list of available machine types using:
    //  *    https://cloud.google.com/sdk/gcloud/reference/compute/machine-types/list
    String machineType = "n1-standard-1";
    // sourceImageFamily - Name of the public image family for Windows Server or SQL Server images.
    //  *    https://cloud.google.com/compute/docs/images#os-compute-support
    String sourceImageFamily = "windows-2012-r2";

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
              .addAccessConfigs(AccessConfig.newBuilder()
                  .setType("ONE_TO_ONE_NAT")
                  .setName("External NAT")
                  .build())
              // If you going you use custom VPC network, it must be configured
              // to allow access to kms.windows.googlecloud.com.
              // https://cloud.google.com/compute/docs/instances/windows/creating-managing-windows-instances#kms-server.
              .setName("global/networks/default")
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

      Operation operation = instancesClient.insertAsync(request).get();

      if (operation.hasError()) {
        System.out.printf("Error in creating instance %s", operation.getError());
        return;
      }

      System.out.printf("Instance created %s", instanceName);
    }
  }
}
// [END compute_create_windows_instance_external_ip]