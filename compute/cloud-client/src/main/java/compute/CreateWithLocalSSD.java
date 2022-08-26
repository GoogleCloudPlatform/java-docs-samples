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

package compute;

// [START compute_instances_create_with_local_ssd]

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateWithLocalSSD {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // project: project ID or project number of the Cloud project you want to use.
    String project = "your-project-id";
    // zone: name of the zone to create the instance in. For example: "us-west3-b"
    String zone = "zone-name";
    // instanceName: name of the new virtual machine (VM) instance.
    String instanceName = "instance-name";

    createWithLocalSSD(project, zone, instanceName);
  }

  // Create a new VM instance with Debian 10 operating system and SSD local disk.
  public static void createWithLocalSSD(String project, String zone, String instanceName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    // Get the latest debian image.
    Image newestDebian = getImageFromFamily("debian-cloud", "debian-10");

    // Create the disks to be included in the instance.
    List<AttachedDisk> disks = new ArrayList<>();
    String diskType = String.format("zones/%s/diskTypes/pd-standard", zone);
    disks.add(diskFromImage(diskType, 10, true, newestDebian.getSelfLink(), true));
    disks.add(localSSDDisk(zone));

    // Create the instance.
    Instance instance = createInstance(project, zone, instanceName, disks);

    if (instance != null) {
      System.out.printf("Instance created with local SSD: %s", instance.getName());
    }

  }

  // Retrieve the newest image that is part of a given family in a project.
  // Args:
  //    project: project ID or project number of the Cloud project you want to get image from.
  //    family: name of the image family you want to get image from.
  private static Image getImageFromFamily(String project, String family) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `imagesClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (ImagesClient imagesClient = ImagesClient.create()) {
      // List of public operating system (OS) images: https://cloud.google.com/compute/docs/images/os-details
      return imagesClient.getFromFamily(project, family);
    }
  }

  // Create an AttachedDisk object to be used in VM instance creation. Uses an image as the
  // source for the new disk.
  //
  // Args:
  //    diskType: the type of disk you want to create. This value uses the following format:
  //        "zones/{zone}/diskTypes/(pd-standard|pd-ssd|pd-balanced|pd-extreme)".
  //        For example: "zones/us-west3-b/diskTypes/pd-ssd"
  //
  //    diskSizeGb: size of the new disk in gigabytes.
  //
  //    boot: boolean flag indicating whether this disk should be used as a
  //    boot disk of an instance.
  //
  //    sourceImage: source image to use when creating this disk.
  //    You must have read access to this disk. This can be one of the publicly available images
  //    or an image from one of your projects.
  //    This value uses the following format: "projects/{project_name}/global/images/{image_name}"
  //
  //    autoDelete: boolean flag indicating whether this disk should be deleted
  //    with the VM that uses it.
  private static AttachedDisk diskFromImage(String diskType, int diskSizeGb, boolean boot,
      String sourceImage, boolean autoDelete) {

    AttachedDiskInitializeParams attachedDiskInitializeParams = AttachedDiskInitializeParams.newBuilder()
        .setSourceImage(sourceImage)
        .setDiskSizeGb(diskSizeGb)
        .setDiskType(diskType)
        .build();

    AttachedDisk bootDisk = AttachedDisk.newBuilder()
        .setInitializeParams(attachedDiskInitializeParams)
        // Remember to set auto_delete to True if you want the disk to be deleted when you delete
        // your VM instance.
        .setAutoDelete(autoDelete)
        .setBoot(boot)
        .build();

    return bootDisk;
  }

  // Create an AttachedDisk object to be used in VM instance creation. The created disk contains
  // no data and requires formatting before it can be used.
  // Args:
  //    zone: The zone in which the local SSD drive will be attached.
  private static AttachedDisk localSSDDisk(String zone) {

    AttachedDiskInitializeParams attachedDiskInitializeParams = AttachedDiskInitializeParams.newBuilder()
        .setDiskType(String.format("zones/%s/diskTypes/local-ssd", zone))
        .build();

    AttachedDisk disk = AttachedDisk.newBuilder()
        .setType(AttachedDisk.Type.SCRATCH.name())
        .setInitializeParams(attachedDiskInitializeParams)
        .setAutoDelete(true)
        .build();

    return disk;
  }

  // Send an instance creation request to the Compute Engine API and wait for it to complete.
  // Args:
  //    project: project ID or project number of the Cloud project you want to use.
  //    zone: name of the zone to create the instance in. For example: "us-west3-b"
  //    instanceName: name of the new virtual machine (VM) instance.
  //    disks: a list of compute.v1.AttachedDisk objects describing the disks
  //           you want to attach to your new instance.
  private static Instance createInstance(String project, String zone, String instanceName,
      List<AttachedDisk> disks)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `instancesClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (InstancesClient instancesClient = InstancesClient.create()) {

      // machineType: machine type of the VM being created. This value uses the
      // following format: "zones/{zone}/machineTypes/{type_name}".
      // For example: "zones/europe-west3-c/machineTypes/f1-micro"
      String machineType = String.format("zones/%s/machineTypes/%s", zone, "n1-standard-1");

      // networkLink: name of the network you want the new instance to use.
      // For example: "global/networks/default" represents the network
      // named "default", which is created automatically for each project.
      String networkLink = "global/networks/default";

      // Collect information into the Instance object.
      Instance instance = Instance.newBuilder()
          .setName(instanceName)
          .setMachineType(machineType)
          .addNetworkInterfaces(NetworkInterface.newBuilder().setName(networkLink).build())
          .addAllDisks(disks)
          .build();

      Operation response = instancesClient.insertAsync(project, zone, instance)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance creation failed ! ! " + response);
        return null;
      }
      System.out.println("Operation Status: " + response.getStatus());
      return instancesClient.get(project, zone, instanceName);
    }

  }

}
// [END compute_instances_create_with_local_ssd]