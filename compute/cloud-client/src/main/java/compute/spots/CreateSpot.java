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

import com.google.cloud.compute.v1.AcceleratorConfig;
import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.Address;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Scheduling;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateSpot {
  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-project-id";
    // Name of the virtual machine to check.
    String instanceName = "your-route-name";
    // Name of the zone you want to use. For example: "us-west3-b"
    String zone = "your-zone";

    createSpotInstance(projectId, instanceName, zone);
  }

  // Create a new Spot VM instance with Debian 11 operating system.
  public static Instance createSpotInstance(String projectId, String instanceName, String zone)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String image;
    try (ImagesClient imagesClient = ImagesClient.create()) {
      image = imagesClient.getFromFamily("debian-cloud", "debian-11").getSelfLink();
    }
    AttachedDisk attachedDisk = buildAttachedDisk(image, zone);
    String machineTypes = String.format("zones/%s/machineTypes/%s", zone, "n1-standard-1");

    // Send an instance creation request to the Compute Engine API and wait for it to complete.
    return createInstance(projectId, zone, instanceName, attachedDisk, true,
            machineTypes, null, null, false,
            null, new ArrayList<>(), false, "STOP", null, false);
  }

  // disks: a list of compute_v1.AttachedDisk objects describing the disks
  //     you want to attach to your new instance.
  // machine_type: machine type of the VM being created. This value uses the
  //     following format: "zones/{zone}/machineTypes/{type_name}".
  //     For example: "zones/europe-west3-c/machineTypes/f1-micro"
  // network_link: name of the network you want the new instance to use.
  //     For example: "global/networks/default" represents the network
  //     named "default", which is created automatically for each project.
  // subnetwork_link: name of the subnetwork you want the new instance to use.
  //     This value uses the following format:
  //     "regions/{region}/subnetworks/{subnetwork_name}"
  // internal_ip: internal IP address you want to assign to the new instance.
  //     By default, a free address from the pool of available internal IP addresses of
  //     used subnet will be used.
  // external_access: boolean flag indicating if the instance should have an external IPv4
  //     address assigned.
  // external_ipv4: external IPv4 address to be assigned to this instance. If you specify
  //     an external IP address, it must live in the same region as the zone of the instance.
  //     This setting requires `external_access` to be set to True to work.
  // accelerators: a list of AcceleratorConfig objects describing the accelerators that will
  //     be attached to the new instance.
  // preemptible: boolean value indicating if the new instance should be preemptible
  //     or not. Preemptible VMs have been deprecated and you should now use Spot VMs.
  // spot: boolean value indicating if the new instance should be a Spot VM or not.
  // instance_termination_action: What action should be taken once a Spot VM is terminated.
  //     Possible values: "STOP", "DELETE"
  // custom_hostname: Custom hostname of the new VM instance.
  //     Custom hostnames must conform to RFC 1035 requirements for valid hostnames.
  // delete_protection: boolean value indicating if the new virtual machine should be
  //     protected against deletion or not.
  private static Instance createInstance(String projectId, String zone, String instanceName,
                                         AttachedDisk disk, boolean isSpot, String machineType,
                                         String subnetworkLink, String internalIp,
                                         boolean externalAccess, String externalIpv4,
                                         List<AcceleratorConfig> acceleratorConfigs,
                                         boolean preemptible, String instanceTerminationAction,
                                         String customHostName, boolean deleteProtection)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (InstancesClient client = InstancesClient.create()) {
      Instance instanceResource =
              buildInstanceResource(instanceName, disk, machineType, externalAccess,
                      externalIpv4, subnetworkLink, internalIp, acceleratorConfigs, preemptible,
                      isSpot, instanceTerminationAction, customHostName, deleteProtection);

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
                                                String externalIpv4, String subnetworkLink,
                                                String internalIp,
                                                List<AcceleratorConfig> acceleratorConfigs,
                                                boolean preemptible, boolean isSpot,
                                                String instanceTerminationAction,
                                                String customHostName, boolean deleteProtection) {
    NetworkInterface networkInterface =
            networkInterface(externalAccess, externalIpv4, subnetworkLink, internalIp);
    Instance.Builder builder = Instance.newBuilder()
            .setName(instanceName)
            .addDisks(disk)
            .setMachineType(machineType)
            .addNetworkInterfaces(networkInterface);
    if (!acceleratorConfigs.isEmpty()) {
      builder.addAllGuestAccelerators(acceleratorConfigs);
      builder.setScheduling(Scheduling.newBuilder()
              .setOnHostMaintenance(Scheduling.OnHostMaintenance.TERMINATE.name()));
    }
    if (preemptible) {
      // Set the preemptible setting
      builder.setScheduling(Scheduling.newBuilder().setPreemptible(preemptible));
    }
    if (isSpot) {
      // Set the Spot VM setting
      Scheduling.Builder scheduling = builder.getScheduling()
              .toBuilder()
              .setProvisioningModel(Scheduling.ProvisioningModel.SPOT.name())
              .setInstanceTerminationAction(instanceTerminationAction);
      builder.setScheduling(scheduling);
    }
    if (customHostName != null) {
      // Set the custom hostname for the instance
      builder.setHostname(customHostName);
    }
    if (deleteProtection) {
      // Set the delete protection bit
      builder.setDeletionProtection(deleteProtection);
    }
    return builder.build();
  }

  private static NetworkInterface networkInterface(boolean externalAccess, String externalIpv4,
                                                   String subnetworkLink, String internalIp) {
    NetworkInterface.Builder build = NetworkInterface.newBuilder()
            .setNetwork("global/networks/default");
    if (subnetworkLink != null) {
      build.setSubnetwork(subnetworkLink);
    }
    if (internalIp != null) {
      build.setNetworkIP(internalIp);
    }
    if (externalAccess) {
      AccessConfig.Builder accessConfig = AccessConfig.newBuilder()
              .setType(AccessConfig.Type.ONE_TO_ONE_NAT.name())
              .setName("External NAT")
              .setNetworkTier(Address.NetworkTier.PREMIUM.name());
      if (externalIpv4 != null) {
        accessConfig.setNatIP(externalIpv4);
      }
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