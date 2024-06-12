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

package compute.spots;

// [START compute_spot_create]

import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.AccessConfig.Type;
import com.google.cloud.compute.v1.Address.NetworkTier;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Scheduling;
import com.google.cloud.compute.v1.Scheduling.ProvisioningModel;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateSpotVm {
  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "your-project-id";
    // Name of the virtual machine to check.
    String instanceName = "your-instance-name";
    // Name of the zone you want to use. For example: "us-west3-b"
    String zone = "your-zone";

    createSpotInstance(projectId, instanceName, zone);
  }

  // Create a new Spot VM instance with Debian 11 operating system.
  public static Instance createSpotInstance(String projectId, String instanceName, String zone)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String image;
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ImagesClient imagesClient = ImagesClient.create()) {
      image = imagesClient.getFromFamily("debian-cloud", "debian-11").getSelfLink();
    }
    AttachedDisk attachedDisk = buildAttachedDisk(image, zone);
    String machineTypes = String.format("zones/%s/machineTypes/%s", zone, "n1-standard-1");

    // Send an instance creation request to the Compute Engine API and wait for it to complete.
    Instance instance =
            createInstance(projectId, zone, instanceName, attachedDisk, true, machineTypes, false);

    System.out.printf("Spot instance '%s' has been created successfully", instance.getName());

    return instance;
  }

  // disks: a list of compute_v1.AttachedDisk objects describing the disks
  //     you want to attach to your new instance.
  // machine_type: machine type of the VM being created. This value uses the
  //     following format: "zones/{zone}/machineTypes/{type_name}".
  //     For example: "zones/europe-west3-c/machineTypes/f1-micro"
  // external_access: boolean flag indicating if the instance should have an external IPv4
  //     address assigned.
  // spot: boolean value indicating if the new instance should be a Spot VM or not.
  private static Instance createInstance(String projectId, String zone, String instanceName,
                                         AttachedDisk disk, boolean isSpot, String machineType,
                                         boolean externalAccess)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (InstancesClient client = InstancesClient.create()) {
      Instance instanceResource =
              buildInstanceResource(instanceName, disk, machineType, externalAccess, isSpot);

      InsertInstanceRequest build = InsertInstanceRequest.newBuilder()
              .setProject(projectId)
              .setRequestId(UUID.randomUUID().toString())
              .setZone(zone)
              .setInstanceResource(instanceResource)
              .build();
      client.insertCallable().futureCall(build).get(60, TimeUnit.SECONDS);

      return client.get(projectId, zone, instanceName);
    }
  }

  private static Instance buildInstanceResource(String instanceName, AttachedDisk disk,
                                                String machineType, boolean externalAccess,
                                                boolean isSpot) {
    NetworkInterface networkInterface =
            networkInterface(externalAccess);
    Instance.Builder builder = Instance.newBuilder()
            .setName(instanceName)
            .addDisks(disk)
            .setMachineType(machineType)
            .addNetworkInterfaces(networkInterface);

    if (isSpot) {
      // Set the Spot VM setting
      Scheduling.Builder scheduling = builder.getScheduling()
              .toBuilder()
              .setProvisioningModel(ProvisioningModel.SPOT.name())
              .setInstanceTerminationAction("STOP");
      builder.setScheduling(scheduling);
    }

    return builder.build();
  }

  private static NetworkInterface networkInterface(boolean externalAccess) {
    NetworkInterface.Builder build = NetworkInterface.newBuilder()
            .setNetwork("global/networks/default");

    if (externalAccess) {
      AccessConfig.Builder accessConfig = AccessConfig.newBuilder()
              .setType(Type.ONE_TO_ONE_NAT.name())
              .setName("External NAT")
              .setNetworkTier(NetworkTier.PREMIUM.name());
      build.addAccessConfigs(accessConfig.build());
    }

    return build.build();
  }

  private static AttachedDisk buildAttachedDisk(String sourceImage, String zone) {
    AttachedDiskInitializeParams initializeParams = AttachedDiskInitializeParams.newBuilder()
            .setSourceImage(sourceImage)
            .setDiskSizeGb(10)
            .setDiskType(String.format("zones/%s/diskTypes/pd-standard", zone))
            .build();
    return AttachedDisk.newBuilder()
            .setInitializeParams(initializeParams)
            // Remember to set auto_delete to True if you want the disk to be deleted
            // when you delete your VM instance.
            .setAutoDelete(true)
            .setBoot(true)
            .build();
  }
}
// [END compute_spot_create]