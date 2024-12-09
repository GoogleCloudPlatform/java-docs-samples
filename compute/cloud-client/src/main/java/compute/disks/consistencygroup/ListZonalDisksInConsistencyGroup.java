package compute.disks.consistencygroup;

// [START compute_consistency_group_list_disks_zonal]
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.ListDisksRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ListZonalDisksInConsistencyGroup {
  public static void main(String[] args)
          throws IOException, InterruptedException, ExecutionException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String project = "tyaho-softserve-project";//"YOUR_PROJECT_ID";
    // Name of the consistency group.
    String consistencyGroupName = "group-name";//"CONSISTENCY_GROUP_ID";
    // Zone of the disk.
    String disksLocation = "us-central1-a";
    // Region of the consistency group.
    String consistencyGroupLocation = "us-central1";

    listZonalDisksInConsistencyGroup(
            project, consistencyGroupName, consistencyGroupLocation, disksLocation);
  }
  // Lists disks in a consistency group.
  public static List<Disk> listZonalDisksInConsistencyGroup(String project, String consistencyGroupName,
                                                       String consistencyGroupLocation, String disksLocation) throws IOException {
    String filter = String
            .format("resourcePolicies=projects/%s/regions/%s/resourcePolicies/%s",
                    project, consistencyGroupLocation, consistencyGroupName);
    List<Disk> disksList = new ArrayList<>();
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
     try (DisksClient disksClient = DisksClient.create()) {
        ListDisksRequest request =
                ListDisksRequest.newBuilder()
                        .setProject(project)
                        .setZone(disksLocation)
                        .setFilter(filter)
                        .build();
        DisksClient.ListPagedResponse response = disksClient.list(request);

//        for (Disk disk : response.iterateAll()) {
//          if (disk.getResourcePoliciesList().contains(filter)) {
//            disksList.add(disk);
//          }
//      }
    }
    System.out.println(disksList.size());
    return disksList;
  }
}
// [END compute_consistency_group_list_disks_zonal]
