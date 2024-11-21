package compute.disks.consistencygroup;


import com.google.cloud.compute.v1.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StopAsyncReplicationConsistencyGroup {

    public static void main(String[] args)
            throws IOException, InterruptedException, ExecutionException, TimeoutException {
        // TODO(developer): Replace these variables before running the sample.
        String project = "tyaho-softserve-project";
        String disksLocation = "us-central1"; // either zone or region
        String consistencyGroupName = "consistency-group-2";
        String consistencyGroupLocation = "northamerica-northeast1"; // region


        stopReplicationConsistencyGroup(project, disksLocation, consistencyGroupName,
                consistencyGroupLocation);
    }

    public static void stopReplicationConsistencyGroup(String project, String disksLocation,
                                                       String consistencyGroupName, String consistencyGroupLocation)
            throws IOException, InterruptedException, ExecutionException, TimeoutException {

        String resourcePolicy = String.format(
                "projects/%s/regions/%s/resourcePolicies/%s", project, consistencyGroupLocation,
                consistencyGroupName);

//        try (DisksClient disksClient = DisksClient.create()) {
//            // Depending on whether the disksLocation is zone or region - use different clients.
//            Operation operation;
//            if (disksLocation.contains("-")) { // Zone
//                StopGroupAsyncReplicationDiskRequest request =
//                        StopGroupAsyncReplicationDiskRequest.newBuilder()
//                                .setProject(project)
//                                .setZone(disksLocation)
//                                .setDisksStopGroupAsyncReplicationResourceResource(
//                                        DisksStopGroupAsyncReplicationResource.newBuilder()
//                                                .setResourcePolicy(resourcePolicy).build())
//                                .build();
//                operation = disksClient.stopGroupAsyncReplicationAsync(request).get(3, TimeUnit.MINUTES);
        try (RegionDisksClient disksClient = RegionDisksClient.create()) {
            // Depending on whether the disksLocation is zone or region - use different clients.
            Operation operation;
                StopGroupAsyncReplicationRegionDiskRequest request =
                        StopGroupAsyncReplicationRegionDiskRequest.newBuilder()
                                .setProject(project)
                                .setRegion(disksLocation)
                                .setDisksStopGroupAsyncReplicationResourceResource(
                                        DisksStopGroupAsyncReplicationResource.newBuilder()
                                                .setResourcePolicy(resourcePolicy).build())
                                .build();
                operation = disksClient.stopGroupAsyncReplicationAsync(request).get(3, TimeUnit.MINUTES);

                if (operation.hasError()) {
                    return;
                }

                System.out.printf("Replication stopped for consistency group: %s\n", consistencyGroupName);
        }
    }
}
