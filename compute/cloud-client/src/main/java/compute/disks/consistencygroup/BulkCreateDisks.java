package compute.disks.consistencygroup;

import com.google.cloud.compute.v1.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BulkCreateDisks {

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "tyaho-softserve-project";
    String disksLocation = "northamerica-northeast1-a";
    String consistencyGroupLocation = "northamerica-northeast1";  // Or region
    String consistencyGroupName = "consistency-group-2"; // Replace with your consistency group self-link
    cloneDisksFromConsistencyGroup(project, disksLocation, consistencyGroupName,
            consistencyGroupLocation);
  }

  // Creates multiple disks from a consistency group policy.
  public static void cloneDisksFromConsistencyGroup(String project, String disksLocation, String consistencyGroupLocation, String consistencyGroupName) throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String sourceConsistencyGroupPolicy = String.format("projects/%s/regions/%s/resourcePolicies/%s", project,consistencyGroupLocation, consistencyGroupName);
    String region = disksLocation.substring(0, disksLocation.lastIndexOf('-'));

    try (RegionDisksClient regionDisksClient = RegionDisksClient.create()) {

      BulkInsertDiskResource bulkInsertDiskResource = BulkInsertDiskResource.newBuilder()
              .setSourceConsistencyGroupPolicy(sourceConsistencyGroupPolicy)
              .build();

      BulkInsertRegionDiskRequest bulkInsertRegionDiskRequest = BulkInsertRegionDiskRequest
              .newBuilder()
              .setProject(project)
              .setRegion(region)
              .setBulkInsertDiskResourceResource(bulkInsertDiskResource)
              .build();
      Operation operation = regionDisksClient.bulkInsertAsync(bulkInsertRegionDiskRequest).get(3,
              TimeUnit.MINUTES);


      if(operation.hasError() ) {
        System.out.println("Clone disks from consistency group failed! " + operation.getError().toString());
        return;
      }
      System.out.println(String.format("Disks cloned from consistency group: %s.",
              consistencyGroupName));

    }
  }
}