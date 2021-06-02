package src.main.java.compute;

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceClient;
import com.google.cloud.compute.v1.InstancesScopedList;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ProjectZoneInstanceName;
import com.google.cloud.compute.v1.ProjectZoneMachineTypeName;
import com.google.cloud.compute.v1.ProjectZoneName;
import com.google.cloud.compute.v1.ProjectZoneOperationName;
import com.google.cloud.compute.v1.ZoneOperationClient;
import java.io.IOException;

public class Quickstart {


  // [START compute_instances_create]

  /**
   * Creates a GCP Compute Instance
   * @param project Google Cloud Project id
   * @param zone    Google Cloud Project zone eg: "us-central1-a"
   * @param machineType Type of the instance that you want to create, eg: "n1-standard-1"
   * @param machineName Name to identify the machine/ instance
   * @param sourceImage Image to be mounted in the machine. eg: "https://www.googleapis.com/compute/v1/projects/debian-cloud/global/images/debian-7-wheezy-v20150710"
   * @param diskSizeGB Size of the disk to be allocated for the machine. eg: "10" in GBs
   */

  public void createInstance(String project, String zone, String machineName, String machineType, String sourceImage, String diskSizeGB)
      throws IOException {

    try (InstanceClient instanceClient = InstanceClient.create()) {
      // Instance creation requires at least one persistent disk and one network interface
      AttachedDisk disk =
          AttachedDisk.newBuilder()
              .setBoot(true)
              .setAutoDelete(true)
              .setType("PERSISTENT")  // Can be set to either 'SCRATCH' or 'PERSISTENT'
              .setInitializeParams(
                  AttachedDiskInitializeParams.newBuilder().setSourceImage(sourceImage).setDiskSizeGb(diskSizeGB).build())
              .build();

      // "default" network interface is created automatically for every project
      NetworkInterface networkInterface = NetworkInterface.newBuilder().setName("default").build();


      // Bind machine_name, machine_type, disk and network interface to an instance
      Instance instanceResource =
          Instance.newBuilder()
              .setName(machineName)
              .setMachineType(ProjectZoneMachineTypeName.of(machineType, project, zone).toString())
              .addDisks(disk)
              .addNetworkInterfaces(networkInterface)
              .build();

      // Inserting the instance in the specified project and zone
      ProjectZoneName projectZoneName = ProjectZoneName.of(project, zone);
      Operation response = instanceClient.insertInstance(projectZoneName, instanceResource);

      // Waits until the insertion operation is completed
      while(!response.getStatus().equalsIgnoreCase("DONE")) {
        ZoneOperationClient zoneOperationClient = ZoneOperationClient.create();
        response = zoneOperationClient.waitZoneOperation(ProjectZoneOperationName.newBuilder().setOperation(response.getName()).setProject(project).setZone(zone).build());
      }

      if (response.getError() == null) {
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
   * @param project Google Cloud Project id
   * @param zone Google Cloud Project zone
   */

  public void listInstances(String project, String zone) throws IOException {
    try(InstanceClient instanceClient = InstanceClient.create()) {
      // Set the project and zone to retrieve instances present in the zone
      ProjectZoneName projectZoneName = ProjectZoneName.newBuilder().setProject(project).setZone(zone).build();
      System.out.println("Listing instances ! ! ");
      for(Instance zoneInstance : instanceClient.listInstances(projectZoneName).iterateAll()) {
        System.out.println(zoneInstance.getName());
      }
    }
  }

//  [END compute_instances_list]





// [START compute_instances_list_all]

  /**
   * List the compute instances in the given project
   * @param project Google Cloud Project id
   */

  public void listAllInstances(String project) throws IOException {
    try(InstanceClient instanceClient = InstanceClient.create()) {
      // Listing all instances for the project
      InstanceClient.AggregatedListInstancesPagedResponse response = instanceClient.aggregatedListInstances(true, project);
      System.out.println("Listing instances ! ! ");
      for(InstancesScopedList zoneInstances : response.iterateAll()) {
        // Scoped by each zone; Check if instances in a zone is null, else iterate and list them
        if(zoneInstances.getInstancesList() != null) {
          for(Instance instance : zoneInstances.getInstancesList()) {
            // getZone() returns the fully qualified address. Hence, strip it to get the zone name only
            String zone = instance.getZone();
            System.out.println(instance.getName() + " at " + zone.substring(zone.lastIndexOf('/')+1));
          }
        }
      }
    }
  }

// [END compute_instances_list_all]





// [START compute_instances_delete]

  /**
   * Deletes the GCP compute instance specified by the instanceName param
   * @param project Google Cloud Project id
   * @param zone Google Cloud Project zone
   * @param instanceName Name of the instance to be deleted
   */

  public void deleteInstance(String project, String zone, String instanceName)
      throws IOException, InterruptedException {
    try(InstanceClient instanceClient = InstanceClient.create()) {

      // Set the properties of the instance which is to be deleted
      ProjectZoneInstanceName instance = ProjectZoneInstanceName.newBuilder().setProject(project).setZone(zone).setInstance(instanceName).build();
      Operation response = instanceClient.deleteInstance(instance);


//      [START compute_instances_operation_check]
      ZoneOperationClient zoneOperationClient = ZoneOperationClient.create();
      // Waits until the delete operation is completed
      while(!response.getStatus().equalsIgnoreCase("DONE")) {
        response = zoneOperationClient.getZoneOperation(ProjectZoneOperationName.newBuilder().setOperation(response.getId()).setProject(project).setZone(zone).build());
        if(!response.getStatus().equalsIgnoreCase("DONE")) {
          System.out.println("Deletion in process...");
        }
      }
//      [END compute_instances_operation_check]


      if(response.getError() == null) {
        System.out.println("Instance deleted successfully ! ! ");
      }else {
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
        diskSizeGB = "disk_size";          // Default size of the disk - 10

    quickstart(projectId, zone, machineName, machineType, sourceImage, diskSizeGB);
  }





  public void quickstart(String projectId, String zone, String machineName, String machineType, String sourceImage, String diskSizeGB)
      throws IOException, InterruptedException {
    Quickstart quickstart = new Quickstart();

    // Action 1: Create Instance
    quickstart.createInstance(projectId, zone, machineName, machineType, sourceImage, diskSizeGB);

    // Action 2: List the created instance in that zone
    quickstart.listInstances(projectId, zone);

    // Action 3: List all instances in the given project
    quickstart.listAllInstances(projectId);

    // Action 4: Delete the created Instance
    quickstart.deleteInstance(projectId, zone, machineName);

    System.out.println("Successfully completed quickstart ! ! ");
  }

}