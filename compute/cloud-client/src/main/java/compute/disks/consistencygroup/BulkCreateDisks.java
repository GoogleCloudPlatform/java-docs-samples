package compute.disks.consistencygroup;

import com.google.cloud.compute.v1.BulkInsertDiskRequest;
import com.google.cloud.compute.v1.BulkInsertDiskResource;
import com.google.cloud.compute.v1.BulkInsertRegionDiskRequest;
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.ListDisksRequest;
import com.google.cloud.compute.v1.ListRegionDisksRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BulkCreateDisks {

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "tyaho-softserve-project";
    String region = "us-central1";  // Or region
    String consistencyGroupPolicy = "my-group"; // Replace with your consistency group self-link


    bulkCreateDisks(project, region, consistencyGroupPolicy);
  }

  // Creates multiple disks from a consistency group policy.
  public static List<Disk> bulkCreateDisks(String project, String region, String consistencyGroupPolicy) throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String sourceConsistencyGroupPolicy = String.format("projects/%s/regions/%s/resourcePolicies/%s", project,region, consistencyGroupPolicy);
    try (RegionDisksClient disksClient = RegionDisksClient.create()) {
      BulkInsertDiskResource.Builder bulkInsertDiskResource = BulkInsertDiskResource.newBuilder()
          .setSourceConsistencyGroupPolicy(sourceConsistencyGroupPolicy);

      BulkInsertRegionDiskRequest bulkCreateDisksRequest = BulkInsertRegionDiskRequest.newBuilder()
          .setProject(project)
          .setRegion(region)
          .setBulkInsertDiskResourceResource(bulkInsertDiskResource.build())
          .build();

      Operation operation = disksClient.bulkInsertAsync(bulkCreateDisksRequest).get(3, TimeUnit.MINUTES);
      ListRegionDisksRequest listRequest =

          ListRegionDisksRequest.newBuilder()
              .setProject(project)
              .setRegion(region)
//              .setFilter(filter)
              .build();
      List<Disk> createdDisks = new ArrayList<>();
      for (Disk disk : disksClient.list(listRequest).iterateAll()) {
        createdDisks.add(disk);
        System.out.println("Created Disk: " + disk.getName()); // Or other relevant info
      }
      if (operation.hasError()) {
       return null;
      }
      System.out.println(createdDisks);
      return createdDisks;

    }
  }
}