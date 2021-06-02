/*
 * Copyright 2020 Google LLC
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

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDisk.Type;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesClient.AggregatedListPagedResponse;
import com.google.cloud.compute.v1.InstancesScopedList;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.ZoneOperationsClient;
import java.io.IOException;
import java.util.Map.Entry;

public class Quickstart {

  // [START compute_instances_create]

  /**
   * Creates a GCP Compute Instance
   *
   * @param project     Google Cloud Project id
   * @param zone        Google Cloud Project zone eg: "us-central1-a"
   * @param machineType Type of the instance that you want to create, eg: "n1-standard-1"
   * @param machineName Name to identify the machine/ instance
   * @param sourceImage Image to be mounted in the machine. eg: "https://www.googleapis.com/compute/v1/projects/debian-cloud/global/images/debian-7-wheezy-v20150710"
   * @param diskSizeGb  Size of the disk to be allocated for the machine. eg: "10" in GBs
   */

  public void createInstance(String project, String zone, String machineName, String machineType,
      String sourceImage, String diskSizeGb)
      throws IOException {

    try (InstancesClient instancesClient = InstancesClient.create()) {
      // Instance creation requires at least one persistent disk and one network interface
      AttachedDisk disk =
          AttachedDisk.newBuilder()
              .setBoot(true)
              .setAutoDelete(true)
              .setType(Type.PERSISTENT)  // Can be set to either 'SCRATCH' or 'PERSISTENT'
              .setInitializeParams(
                  AttachedDiskInitializeParams.newBuilder().setSourceImage(sourceImage)
                      .setDiskSizeGb(diskSizeGb).build())
              .build();

      // "default" network interface is created automatically for every project
      NetworkInterface networkInterface = NetworkInterface.newBuilder().setName("default").build();

      // Bind machine_name, machine_type, disk and network interface to an instance
      Instance instanceResource =
          Instance.newBuilder()
              .setName(machineName)
              .setMachineType(machineType)
              .addDisks(disk)
              .addNetworkInterfaces(networkInterface)
              .build();

      // Inserting the instance in the specified project and zone
      Operation response = instancesClient.insert(project, zone, instanceResource);

      // Waits until the insertion operation is completed
      while (response.getStatus() != Status.DONE) {
        ZoneOperationsClient zoneOperationsClient = ZoneOperationsClient.create();
        response = zoneOperationsClient.wait(project, zone, response.getId());
      }

      if (response.getError().toString() == "") {
        System.out.println("Instance creation successful ! ! ");
      } else {
        System.out.println("Instance creation failed ! ! " + response.getError());
      }
    }
  }

  //  [END compute_instances_create]

  //  [START compute_instances_list]

  /**
   * List the compute instances present in the given project and zone
   *
   * @param project Google Cloud Project id
   * @param zone    Google Cloud Project zone
   */

  public void listInstances(String project, String zone) throws IOException {

    try (InstancesClient instancesClient = InstancesClient.create()) {
      // Set the project and zone to retrieve instances present in the zone
      System.out.println("Listing instances ! ! ");
      for (Instance zoneInstance : instancesClient.list(project, zone).iterateAll()) {
        System.out.println(zoneInstance.getName());
      }
    }
  }

  //  [END compute_instances_list]

  // [START compute_instances_list_all]

  /**
   * List the compute instances in the given project
   *
   * @param project Google Cloud Project id
   */

  public void listAllInstances(String project) throws IOException {
    try (InstancesClient instancesClient = InstancesClient.create()) {
      // Listing all instances for the project
      AggregatedListPagedResponse response = instancesClient.aggregatedList(project);
      System.out.println("Listing instances ! ! ");
      for (Entry<String, InstancesScopedList> zoneInstances : response.iterateAll()) {
        // Scoped by each zone; Check if instances in a zone is null, else iterate and list them
        String zone = zoneInstances.getKey();
        if (zoneInstances.getValue().getInstancesList() != null) {
          for (Instance instance : zoneInstances.getValue().getInstancesList()) {
            // getZone() returns the fully qualified address. Hence, strip it to get the zone name only
            System.out
                .println(instance.getName() + " at " + zone.substring(zone.lastIndexOf('/') + 1));
          }
        }
      }
    }
  }

  // [END compute_instances_list_all]

  // [START compute_instances_delete]

  /**
   * Deletes the GCP compute instance specified by the instanceName param
   *
   * @param project      Google Cloud Project id
   * @param zone         Google Cloud Project zone
   * @param instanceName Name of the instance to be deleted
   */
  public void deleteInstance(String project, String zone, String instanceName)
      throws IOException, InterruptedException {
    try (InstancesClient instancesClient = InstancesClient.create()) {

      // Set the properties of the instance which is to be deleted
      Operation response = instancesClient.delete(project, zone, instanceName);

      // [START compute_instances_operation_check]
      ZoneOperationsClient zoneOperationClient = ZoneOperationsClient.create();
      // Waits until the delete operation is completed
      while (response.getStatus() != Status.DONE) {
        response = zoneOperationClient.get(project, zone, response.getId());
        if (response.getStatus() != Status.DONE) {
          System.out.println("Deletion in process...");
        }
      }
      // [END compute_instances_operation_check]

      if (response.getError().toString() == "") {
        System.out.println("Instance deleted successfully ! ! ");
      } else {
        System.out.println("Failed to delete instance ! ! " + response.getError());
      }
    }
  }
  // [END compute_instances_delete]


  public void quickstart() throws IOException, InterruptedException {
    // TODO(developer): Replace the values for below variables before running the template
    String projectId = "your_project_id",  // Google Cloud project id
        zone = "zone_name",                // Google Cloud zone name  eg: "us-central1-a"
        machineName = "machine_name",      // Name to identify the machine/ instance
        machineType = "machine_type",      // Type of the machine - "n1-standard-1"
        sourceImage = "source_image",      // Image to be mounted on the machine - "https://www.googleapis.com/compute/v1/projects/debian-cloud/global/images/debian-7-wheezy-v20150710"
        diskSizeGb = "disk_size";          // Default size of the disk - 10

    quickstart(projectId, zone, machineName, machineType, sourceImage, diskSizeGb);
  }

  public void quickstart(String projectId, String zone, String machineName, String machineType,
      String sourceImage, String diskSizeGb)
      throws IOException, InterruptedException {
    Quickstart quickstart = new Quickstart();

    // Action 1: Create Instance
    quickstart.createInstance(projectId, zone, machineName, machineType, sourceImage, diskSizeGb);

    // Action 2: List the created instance in that zone
    quickstart.listInstances(projectId, zone);

    // Action 3: List all instances in the given project
    quickstart.listAllInstances(projectId);

    // Action 4: Delete the created Instance
    quickstart.deleteInstance(projectId, zone, machineName);

    System.out.println("Successfully completed quickstart ! ! ");
  }

}