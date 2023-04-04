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

// [START compute_instances_create_with_subnet]
// [START compute_instances_create_from_image_plus_snapshot_disk]
// [START compute_instances_create_from_snapshot]
// [START compute_instances_create_from_image_plus_empty_disk]
// [START compute_instances_create_from_custom_image]
// [START compute_instances_create_from_image]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDisk.Type;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateInstancesAdvanced {
  // [END compute_instances_create_from_image]
  // [END compute_instances_create_from_custom_image]
  // [END compute_instances_create_from_image_plus_empty_disk]
  // [END compute_instances_create_from_snapshot]
  // [END compute_instances_create_from_image_plus_snapshot_disk]
  // [END compute_instances_create_with_subnet]

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "your-project-id";
    String zone = "zone-name";
    String instanceName = "instance-name";
  }

  // [START compute_instances_create_with_subnet]
  // [START compute_instances_create_from_image_plus_snapshot_disk]
  // [START compute_instances_create_from_image_plus_empty_disk]
  // [START compute_instances_create_from_custom_image]
  // [START compute_instances_create_from_image]

  /**
   * Create an AttachedDisk object to be used in VM instance creation. Uses an image as the source
   * for the new disk.
   *
   * @param diskType the type of disk you want to create. This value uses the following format:
   * "zones/{zone}/diskTypes/(pd-standard|pd-ssd|pd-balanced|pd-extreme)". For example:
   * "zones/us-west3-b/diskTypes/pd-ssd"
   * @param diskSizeGb size of the new disk in gigabytes
   * @param boot boolean flag indicating whether this disk should be used as a boot disk of an
   * instance
   * @param sourceImage source image to use when creating this disk. You must have read access to
   * this disk. This can be one of the publicly available images or an image from one of your
   * projects. This value uses the following format:
   * "projects/{project_name}/global/images/{image_name}"
   * @return AttachedDisk object configured to be created using the specified image.
   */
  private static AttachedDisk diskFromImage(String diskType, int diskSizeGb, boolean boot,
      String sourceImage) {
    AttachedDisk disk =
        AttachedDisk.newBuilder()
            .setBoot(boot)
            // Remember to set auto_delete to True if you want the disk to be deleted when
            // you delete your VM instance.
            .setAutoDelete(true)
            .setType(Type.PERSISTENT.toString())
            .setInitializeParams(
                AttachedDiskInitializeParams.newBuilder()
                    .setSourceImage(sourceImage)
                    .setDiskSizeGb(diskSizeGb)
                    .setDiskType(diskType)
                    .build())
            .build();
    return disk;
  }

  // [END compute_instances_create_from_image]
  // [END compute_instances_create_from_custom_image]
  // [END compute_instances_create_from_image_plus_empty_disk]
  // [END compute_instances_create_from_image_plus_snapshot_disk]
  // [END compute_instances_create_with_subnet]

  // [START compute_instances_create_from_image_plus_empty_disk]

  /**
   * Create an AttachedDisk object to be used in VM instance creation. The created disk contains no
   * data and requires formatting before it can be used.
   *
   * @param diskType the type of disk you want to create. This value uses the following format:
   * "zones/{zone}/diskTypes/(pd-standard|pd-ssd|pd-balanced|pd-extreme)". For example:
   * "zones/us-west3-b/diskTypes/pd-ssd"
   * @param diskSizeGb size of the new disk in gigabytes
   * @return AttachedDisk object configured to be created as an empty disk.
   */
  private static AttachedDisk emptyDisk(String diskType, int diskSizeGb) {
    AttachedDisk disk =
        AttachedDisk.newBuilder()
            .setBoot(false)
            // Remember to set auto_delete to True if you want the disk to be deleted when
            // you delete your VM instance.
            .setAutoDelete(true)
            .setType(Type.PERSISTENT.toString())
            .setInitializeParams(
                AttachedDiskInitializeParams.newBuilder()
                    .setDiskSizeGb(diskSizeGb)
                    .setDiskType(diskType)
                    .build())
            .build();
    return disk;
  }
  // [END compute_instances_create_from_image_plus_empty_disk]

  // [START compute_instances_create_from_image_plus_snapshot_disk]
  // [START compute_instances_create_from_snapshot]

  /**
   * @param diskType the type of disk you want to create. This value uses the following format:
   * "zones/{zone}/diskTypes/(pd-standard|pd-ssd|pd-balanced|pd-extreme)". For example:
   * "zones/us-west3-b/diskTypes/pd-ssd"
   * @param diskSizeGb size of the new disk in gigabytes
   * @param boot boolean flag indicating whether this disk should be used as a boot disk of an
   * instance
   * @param diskSnapshot disk snapshot to use when creating this disk. You must have read access to
   * this disk. This value uses the following format:
   * "projects/{project_name}/global/snapshots/{snapshot_name}"
   * @return AttachedDisk object configured to be created using the specified snapshot.
   */
  private static AttachedDisk diskFromSnapshot(String diskType, int diskSizeGb, boolean boot,
      String diskSnapshot) {
    AttachedDisk disk =
        AttachedDisk.newBuilder()
            .setBoot(boot)
            // Remember to set auto_delete to True if you want the disk to be deleted when
            // you delete your VM instance.
            .setAutoDelete(true)
            .setType(Type.PERSISTENT.toString())
            .setInitializeParams(
                AttachedDiskInitializeParams.newBuilder()
                    .setSourceSnapshot(diskSnapshot)
                    .setDiskSizeGb(diskSizeGb)
                    .setDiskType(diskType)
                    .build())
            .build();
    return disk;
  }

  // [END compute_instances_create_from_image_plus_snapshot_disk]
  // [END compute_instances_create_from_snapshot]

  // [START compute_instances_create_with_subnet]
  // [START compute_instances_create_from_image_plus_snapshot_disk]
  // [START compute_instances_create_from_snapshot]
  // [START compute_instances_create_from_image_plus_empty_disk]
  // [START compute_instances_create_from_custom_image]
  // [START compute_instances_create_from_image]

  /**
   * Send an instance creation request to the Compute Engine API and wait for it to complete.
   *
   * @param project project ID or project number of the Cloud project you want to use.
   * @param zone name of the zone to create the instance in. For example: "us-west3-b"
   * @param instanceName name of the new virtual machine (VM) instance.
   * @param disks a list of compute_v1.AttachedDisk objects describing the disks you want to attach
   * to your new instance.
   * @param machineType machine type of the VM being created. This value uses the following format:
   * "zones/{zone}/machineTypes/{type_name}".
   * For example: "zones/europe-west3-c/machineTypes/f1-micro"
   * @param network name of the network you want the new instance to use. For example:
   * "global/networks/default" represents the network named "default", which is created
   * automatically for each project.
   * @param subnetwork name of the subnetwork you want the new instance to use. This value uses the
   * following format: "regions/{region}/subnetworks/{subnetwork_name}"
   * @return Instance object.
   */
  private static Instance createWithDisks(String project, String zone, String instanceName,
      Vector<AttachedDisk> disks, String machineType, String network, String subnetwork)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (InstancesClient instancesClient = InstancesClient.create()) {
      // Use the network interface provided in the networkName argument.
      NetworkInterface networkInterface;
      if (subnetwork != null) {
        networkInterface = NetworkInterface.newBuilder()
            .setName(network).setSubnetwork(subnetwork)
            .build();
      } else {
        networkInterface = NetworkInterface.newBuilder()
            .setName(network).build();
      }

      machineType = String.format("zones/%s/machineTypes/%s", zone, machineType);

      // Bind `instanceName`, `machineType`, `disk`, and `networkInterface` to an instance.
      Instance instanceResource =
          Instance.newBuilder()
              .setName(instanceName)
              .setMachineType(machineType)
              .addAllDisks(disks)
              .addNetworkInterfaces(networkInterface)
              .build();

      System.out.printf("Creating instance: %s at %s ", instanceName, zone);

      // Insert the instance in the specified project and zone.
      InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
          .setProject(project)
          .setZone(zone)
          .setInstanceResource(instanceResource).build();

      OperationFuture<Operation, Operation> operation = instancesClient.insertAsync(
          insertInstanceRequest);

      // Wait for the operation to complete.
      Operation response = operation.get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance creation failed ! ! " + response);
        return null;
      }
      System.out.println("Operation Status: " + response.getStatus());

      return instancesClient.get(project, zone, instanceName);
    }
  }
  // [END compute_instances_create_from_image]
  // [END compute_instances_create_from_custom_image]
  // [END compute_instances_create_from_image_plus_empty_disk]
  // [END compute_instances_create_from_snapshot]
  // [END compute_instances_create_from_image_plus_snapshot_disk]
  // [END compute_instances_create_with_subnet]

  // [START compute_instances_create_from_image]

  /**
   * Create a new VM instance with Debian 10 operating system.
   *
   * @param project project ID or project number of the Cloud project you want to use.
   * @param zone name of the zone to create the instance in. For example: "us-west3-b"
   * @param instanceName name of the new virtual machine (VM) instance.
   * @return Instance object.
   */
  public static Instance createFromPublicImage(String project, String zone, String instanceName)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (ImagesClient imagesClient = ImagesClient.create()) {
      // List of public operating system (OS) images: https://cloud.google.com/compute/docs/images/os-details
      Image image = imagesClient.getFromFamily("debian-cloud", "debian-10");
      String diskType = String.format("zones/%s/diskTypes/pd-standard", zone);
      Vector<AttachedDisk> disks = new Vector<>();
      disks.add(diskFromImage(diskType, 10, true, image.getSelfLink()));
      return createWithDisks(project, zone, instanceName, disks, "n1-standard-1",
          "global/networks/default", null);
    }
  }
  // [END compute_instances_create_from_image]

  // [START compute_instances_create_from_custom_image]

  /**
   * Create a new VM instance with custom image used as its boot disk.
   *
   * @param project project ID or project number of the Cloud project you want to use.
   * @param zone name of the zone to create the instance in. For example: "us-west3-b"
   * @param instanceName name of the new virtual machine (VM) instance.
   * @param customImage link to the custom image you want to use in the form of:
   * "projects/{project_name}/global/images/{image_name}"
   * @return Instance object.
   */
  public static Instance createFromCustomImage(String project, String zone, String instanceName,
      String customImage)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    String diskType = String.format("zones/%s/diskTypes/pd-standard", zone);
    Vector<AttachedDisk> disks = new Vector<>();
    disks.add(diskFromImage(diskType, 10, true, customImage));
    return createWithDisks(project, zone, instanceName, disks, "n1-standard-1",
        "global/networks/default", null);
  }
  // [END compute_instances_create_from_custom_image]

  // [START compute_instances_create_from_image_plus_empty_disk]

  /**
   * Create a new VM instance with Debian 10 operating system and a 11 GB additional empty disk.
   *
   * @param project project ID or project number of the Cloud project you want to use.
   * @param zone name of the zone to create the instance in. For example: "us-west3-b"
   * @param instanceName name of the new virtual machine (VM) instance.
   * @return Instance object.
   */
  public static Instance createWithAdditionalDisk(String project, String zone, String instanceName)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (ImagesClient imagesClient = ImagesClient.create()) {
      // List of public operating system (OS) images: https://cloud.google.com/compute/docs/images/os-details
      Image image = imagesClient.getFromFamily("debian-cloud", "debian-10");
      String diskType = String.format("zones/%s/diskTypes/pd-standard", zone);
      Vector<AttachedDisk> disks = new Vector<>();
      disks.add(diskFromImage(diskType, 10, true, image.getSelfLink()));
      disks.add(emptyDisk(diskType, 11));
      return createWithDisks(project, zone, instanceName, disks, "n1-standard-1",
          "global/networks/default", null);
    }
  }
  // [END compute_instances_create_from_image_plus_empty_disk]

  // [START compute_instances_create_from_snapshot]

  /**
   * Create a new VM instance with boot disk created from a snapshot.
   *
   * @param project project ID or project number of the Cloud project you want to use.
   * @param zone name of the zone to create the instance in. For example: "us-west3-b"
   * @param instanceName name of the new virtual machine (VM) instance.
   * @param snapshotName link to the snapshot you want to use as the source of your boot disk in the
   * form of: "projects/{project_name}/global/snapshots/{snapshot_name}"
   * @return Instance object.
   */
  public static Instance createFromSnapshot(String project, String zone, String instanceName,
      String snapshotName)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    String diskType = String.format("zones/%s/diskTypes/pd-standard", zone);
    Vector<AttachedDisk> disks = new Vector<>();
    disks.add(diskFromSnapshot(diskType, 11, true, snapshotName));
    return createWithDisks(project, zone, instanceName, disks, "n1-standard-1",
        "global/networks/default", null);
  }
  // [END compute_instances_create_from_snapshot]

  // [START compute_instances_create_from_image_plus_snapshot_disk]

  /**
   * Create a new VM instance with Debian 10 operating system and data disk created from snapshot.
   *
   * @param project project ID or project number of the Cloud project you want to use.
   * @param zone name of the zone to create the instance in. For example: "us-west3-b"
   * @param instanceName name of the new virtual machine (VM) instance.
   * @param snapshotName link to the snapshot you want to use as the source of your data disk in the
   * form of: "projects/{project_name}/global/snapshots/{snapshot_name}"
   * @return Instance object.
   */
  public static Instance createWithSnapshottedDataDisk(String project, String zone,
      String instanceName, String snapshotName)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (ImagesClient imagesClient = ImagesClient.create()) {
      // List of public operating system (OS) images: https://cloud.google.com/compute/docs/images/os-details
      Image image = imagesClient.getFromFamily("debian-cloud", "debian-10");
      String diskType = String.format("zones/%s/diskTypes/pd-standard", zone);
      Vector<AttachedDisk> disks = new Vector<>();
      disks.add(diskFromImage(diskType, 10, true, image.getSelfLink()));
      disks.add(diskFromSnapshot(diskType, 11, false, snapshotName));
      return createWithDisks(project, zone, instanceName, disks, "n1-standard-1",
          "global/networks/default", null);
    }
  }
  // [END compute_instances_create_from_image_plus_snapshot_disk]

  // [START compute_instances_create_from_image]

  /**
   * Create a new VM instance with Debian 10 operating system in specified network and subnetwork.
   *
   * @param project project ID or project number of the Cloud project you want to use.
   * @param zone name of the zone to create the instance in. For example: "us-west3-b"
   * @param instanceName name of the new virtual machine (VM) instance.
   * @param networkLink name of the network you want the new instance to use. For example:
   * "global/networks/default" represents the network named "default", which is created
   * automatically for each project.
   * @param subnetworkLink name of the subnetwork you want the new instance to use. This value uses
   * the following format: "regions/{region}/subnetworks/{subnetwork_name}"
   * @return Instance object.
   */
  public static Instance createWithSubnetwork(String project, String zone, String instanceName,
      String networkLink, String subnetworkLink)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (ImagesClient imagesClient = ImagesClient.create()) {
      // List of public operating system (OS) images: https://cloud.google.com/compute/docs/images/os-details
      Image image = imagesClient.getFromFamily("debian-cloud", "debian-10");
      String diskType = String.format("zones/%s/diskTypes/pd-standard", zone);
      Vector<AttachedDisk> disks = new Vector<>();
      disks.add(diskFromImage(diskType, 10, true, image.getSelfLink()));
      return createWithDisks(project, zone, instanceName, disks, "n1-standard-1", networkLink,
          subnetworkLink);
    }
  }
  // [END compute_instances_create_from_image]
}
