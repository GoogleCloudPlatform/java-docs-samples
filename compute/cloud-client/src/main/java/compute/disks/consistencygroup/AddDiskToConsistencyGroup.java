package compute.disks.consistencygroup;

// [START compute_consistency_group_add_disk]
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksAddResourcePoliciesRequest;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksAddResourcePoliciesRequest;
import com.google.cloud.compute.v1.RegionDisksClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class AddDiskToConsistencyGroup {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "tyaho-softserve-project";//"YOUR_PROJECT_ID";
    String locationFlag = "zone";
    String location = "us-central1-a";
    String diskName = "disk-name";//"DISK_NAME";
    String consistencyGroup = "my-group";//"CONSISTENCY_GROUP";

    addResourcePoliciesToDisk(project, locationFlag, location, diskName, consistencyGroup);
  }

  // Adds a resource policy to a disk.
  public static Disk addResourcePoliciesToDisk(
      String project, String locationFlag, String location, String diskName,
      String consistencyGroup)
      throws IOException, ExecutionException, InterruptedException {
    String consistencyGroupUrl = String.format(
        "https://www.googleapis.com/compute/v1/projects/%s/regions/%s/resourcePolicies/%s",
        project, location, consistencyGroup);
    if (locationFlag.equals("zone")) {
      try (DisksClient disksClient = DisksClient.create()) {
        DisksAddResourcePoliciesRequest disksRequest =
            DisksAddResourcePoliciesRequest.newBuilder()
            .addAllResourcePolicies(Arrays.asList(consistencyGroupUrl))
            .build();

        Operation response = disksClient.addResourcePoliciesAsync(
            project, location, diskName,disksRequest).get();
        if (response.hasError()) {
          return null;
        }
        return disksClient.get(project, location, diskName);
      }
    } else if (locationFlag.equals("region")) {
      try (RegionDisksClient regionDisksClient = RegionDisksClient.create()) {
        RegionDisksAddResourcePoliciesRequest disksRequest =
            RegionDisksAddResourcePoliciesRequest.newBuilder()
                .addAllResourcePolicies(Arrays.asList(consistencyGroupUrl))
                .build();
        Operation response = regionDisksClient.addResourcePoliciesAsync(
            project, location, diskName,disksRequest).get();
        if (response.hasError()) {
          return null;
        }
        return regionDisksClient.get(project, location, diskName);
      }
    } else {
      System.out.println("Invalid location flag. Use 'zone' or 'region'.");
    }
    return null;
  }
}
// [END compute_consistency_group_add_disk]