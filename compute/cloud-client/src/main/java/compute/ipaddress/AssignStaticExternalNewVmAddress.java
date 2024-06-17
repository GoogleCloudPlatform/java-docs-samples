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

package compute.ipaddress;

// [START compute_ip_address_assign_static_external_new_vm]

import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.AccessConfig.Type;
import com.google.cloud.compute.v1.Address.NetworkTier;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.GetInstanceRequest;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AssignStaticExternalNewVmAddress {

  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "your-project-id";
    // Instance ID of the Google Cloud project you want to use.
    String instanceId = "your-instance-id";
    // Name of the zone to create the instance in. For example: "us-west3-b"
    String zone = "your-zone-id";
    // machine type of the VM being created. This value uses the
    // following format: "zones/{zone}/machineTypes/{type_name}".
    // For example: "zones/europe-west3-c/machineTypes/f1-micro"
    String machineType = String.format("zones/%s/machineTypes/{your-machineType-id}", zone);
    // boolean flag indicating if the instance should have an external IPv4 address assigned.
    boolean externalAccess = true;
    // external IPv4 address to be assigned to this instance. If you specify
    // an external IP address, it must live in the same region as the zone of the instance.
    // This setting requires `external_access` to be set to True to work.
    String externalIpv4 = "your-externalIpv4-id";

    assignStaticExternalNewVmAddress(projectId, instanceId, zone,
            externalAccess, machineType, externalIpv4);
  }

  // Create a new VM instance with assigned static external IP address.
  public static Instance assignStaticExternalNewVmAddress(String projectId, String instanceName,
                                                          String zone, boolean externalAccess,
                                                          String machineType, String externalIpv4)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String sourceImage;
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ImagesClient imagesClient = ImagesClient.create()) {
      sourceImage = imagesClient.getFromFamily("debian-cloud", "debian-11").getSelfLink();
    }
    AttachedDisk attachedDisk = buildAttachedDisk(sourceImage, zone);

    return createInstance(projectId, instanceName, zone,
            attachedDisk, machineType, externalAccess, externalIpv4);
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

  // Send an instance creation request to the Compute Engine API and wait for it to complete.
  private static Instance createInstance(String projectId, String instanceName,
                                         String zone, AttachedDisk disks,
                                         String machineType, boolean externalAccess,
                                         String externalIpv4)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (InstancesClient client = InstancesClient.create()) {
      Instance instanceResource =
              buildInstanceResource(instanceName, disks, machineType, externalAccess, externalIpv4);

      InsertInstanceRequest build = InsertInstanceRequest.newBuilder()
              .setProject(projectId)
              .setRequestId(UUID.randomUUID().toString())
              .setZone(zone)
              .setInstanceResource(instanceResource)
              .build();
      client.insertCallable().futureCall(build).get(60, TimeUnit.SECONDS);

      GetInstanceRequest getInstanceRequest = GetInstanceRequest.newBuilder()
              .setInstance(instanceName)
              .setProject(projectId)
              .setZone(zone)
              .build();

      return client.get(getInstanceRequest);
    }
  }

  private static Instance buildInstanceResource(String instanceName, AttachedDisk disk,
                                                String machineType, boolean externalAccess,
                                                String externalIpv4) {
    NetworkInterface networkInterface =
            networkInterface(externalAccess, externalIpv4);

    return Instance.newBuilder()
            .setName(instanceName)
            .addDisks(disk)
            .setMachineType(machineType)
            .addNetworkInterfaces(networkInterface)
            .build();
  }

  private static NetworkInterface networkInterface(boolean externalAccess, String externalIpv4) {
    NetworkInterface.Builder build = NetworkInterface.newBuilder()
            .setNetwork("global/networks/default");
    if (externalAccess) {
      AccessConfig.Builder accessConfig = AccessConfig.newBuilder()
              .setType(Type.ONE_TO_ONE_NAT.name())
              .setName("External NAT")
              .setNetworkTier(NetworkTier.PREMIUM.name());
      if (externalIpv4 != null) {
        accessConfig.setNatIP(externalIpv4);
      }
      build.addAccessConfigs(accessConfig.build());
    }

    return build.build();
  }
}
// [END compute_ip_address_assign_static_external_new_vm]