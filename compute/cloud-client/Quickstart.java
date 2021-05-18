import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.GlobalOperationClient;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceClient;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstancesScopedList;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ProjectGlobalOperationName;
import com.google.cloud.compute.v1.ProjectRegionOperationName;
import com.google.cloud.compute.v1.ProjectZoneInstanceName;
import com.google.cloud.compute.v1.ProjectZoneMachineTypeName;
import com.google.cloud.compute.v1.ProjectZoneName;
import com.google.cloud.compute.v1.ProjectZoneOperationName;
import com.google.cloud.compute.v1.RegionOperationClient;
import com.google.cloud.compute.v1.ZoneOperationClient;
import java.io.IOException;

public class Quickstart {

//  [START compute_instances_create]

  /**
   * Creates a GCP Compute Instance
   * @param project Google Cloud Project id
   * @param zone    Google Cloud Project zone eg: "us-central1-a"
   * @param machineType Type of the instance that you want to create, eg: "n1-standard-1"
   * @param machineName Name to identify the machine
   * @param sourceImage Image to be mounted in the machine. eg: "https://www.googleapis.com/compute/v1/projects/debian-cloud/global/images/debian-7-wheezy-v20150710"
   * @param diskSizeGB Size of the disk to be allocated for the machine. eg: "10" in GBs
   */

  private void createInstance(String project, String zone, String machineName, String machineType, String sourceImage, String diskSizeGB) {

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

      ProjectZoneName projectZoneName = ProjectZoneName.of(project, zone);
      Operation response = instanceClient.insertInstance(projectZoneName.toString(), instanceResource);

      if (response.getError() == null) {
        System.out.println("Instance creation successful ! ! ");
      } else {
        System.out.println("Instance creation failed ! ! " + response.getError());
      }

    }catch (Exception e) {
      System.out.println("Instance creation failed ! ! " + e.getCause());
    }
  }

//  [END compute_instances_create]





//  [START compute_instances_list]

  /**
   * List the compute instances present in the given project and zone
   * @param project Google Cloud Project id
   * @param zone Google Cloud Project zone
   */

  private void listInstances(String project, String zone) {
    try(InstanceClient instanceClient = InstanceClient.create()) {
      // Set the project and zone to retrieve instances present in the zone
      ProjectZoneName projectZoneName = ProjectZoneName.newBuilder().setProject(project).setZone(zone).build();
      for(Instance zoneInstance : instanceClient.listInstances(projectZoneName).iterateAll()) {
        System.out.println(zoneInstance.getName());
      }
    }catch (Exception e) {
      System.out.println("Failed to retrieve instances ! ! " + e.getCause());
    }
  }
//  [END compute_instances_list]





// [START compute_instances_list_all]

  /**
   * List the compute instances in the given project
   * @param project Google Cloud Project id
   */

  private void listAllInstances(String project) {
    try(InstanceClient instanceClient = InstanceClient.create()) {
      // Listing all instances for the project
      InstanceClient.AggregatedListInstancesPagedResponse response = instanceClient.aggregatedListInstances(false, project);
      for(InstancesScopedList element : response.iterateAll()) {
        // Scoped by each zone; Check if instances in a zone is null, else iterate and list them
        if(element.getInstancesList() != null) {
          for(Instance zoneInstances : element.getInstancesList()) {
            // getZone() returns the fully qualified address. Hence, strip it to get the zone name only
            String zone = zoneInstances.getZone();
            System.out.println(zoneInstances.getName() + " at " + zone.substring(zone.lastIndexOf('/')+1));
          }
        }
      }
    }catch (Exception e) {
      System.out.println("Failed to retrieve instances ! ! " + e.getCause());
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
  private void deleteInstance(String project, String zone, String instanceName) {
    try(InstanceClient instanceClient = InstanceClient.create()) {
      // Set the properties of the instance which is to be deleted
      ProjectZoneInstanceName instance = ProjectZoneInstanceName.newBuilder().setProject(project).setZone(zone).setInstance(instanceName).build();
      Operation response = instanceClient.deleteInstance(instance);

      if(response.getError() == null) {
        System.out.println("Instance deleted successfully ! ! ");
      }else {
        System.out.println("Error in instance deletion ! ! " + response.getError());
      }
    }catch (Exception e) {
      System.out.println("Failed to delete the specified instance ! ! " + e.getCause());
    }
  }
// [END compute_instances_delete]





  private void initialize(String projectId, String zone, String machineName, String machineType, String sourceImage, String diskSizeGB) {

    // Action 1: Create Instance
    this.createInstance(projectId, zone, machineName, machineType, sourceImage, diskSizeGB);

    // Action 2: List Instances
    this.listInstances(projectId, zone);

    // Action 3: List all Instances
    this.listAllInstances(projectId);

    // Action 4: Delete Instance
    String instanceName = "instance_name_to_delete";
    this.deleteInstance(projectId, zone, instanceName);

  }




  public static void main(String[] args) {
//    String projectId = args[0],  // Google Cloud project id
//        zone = args[1],          // Google Cloud zone name  eg: "us-central1-a"
//        machineName = args[2],   // Name to identify the machine eg: "instance-name"
//        machineType = args[3],   // Type of the machine eg: "n1-standard-1"
//        sourceImage = args[4],  // image to be mounted on the machine eg: "https://www.googleapis.com/compute/v1/projects/debian-cloud/global/images/debian-7-wheezy-v20150710"
//        diskSizeGB = args[5];   // default size of the disk eg: "10"
//
//    new Quickstart().initialize(projectId, zone, machineName, machineType, sourceImage, diskSizeGB);
    new Quickstart().createInstance(ServiceOptions.getDefaultProjectId(), "us-central1-a", "test-new",
        "n1-standard-1",
        "https://www.googleapis.com/compute/v1/projects/debian-cloud/global/images/debian-7-wheezy-v20150710", "10");
  }

}