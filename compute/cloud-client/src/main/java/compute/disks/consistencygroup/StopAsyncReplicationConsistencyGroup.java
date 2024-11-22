/*
 * Copyright 2024 Google LLC
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

package compute.disks.consistencygroup;

// [START compute_consistency_group_stop_replication]
// Uncomment these lines if your disk has zonal location.
//import com.google.cloud.compute.v1.DisksClient;
//import com.google.cloud.compute.v1.StopGroupAsyncReplicationDiskRequest;
import com.google.cloud.compute.v1.DisksStopGroupAsyncReplicationResource;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.StopGroupAsyncReplicationRegionDiskRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StopAsyncReplicationConsistencyGroup {

  public static void main(String[] args)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "tyaho-softserve-project";
    String disksLocation = "us-central1-a"; // either zone or region
    String consistencyGroupName = "consistency-group-3";
    String consistencyGroupLocation = "us-central1"; // region

    stopReplicationConsistencyGroup(project, disksLocation, consistencyGroupName,
            consistencyGroupLocation);
  }

  public static void stopReplicationConsistencyGroup(String project, String disksLocation,
         String consistencyGroupName, String consistencyGroupLocation)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    String resourcePolicy = String.format(
            "projects/%s/regions/%s/resourcePolicies/%s", project, consistencyGroupLocation,
            consistencyGroupName);
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.

    // Depending on whether the disksLocation is zone or region - use different clients.
    // Uncomment these lines if your disk has zonal location.
    //try (DisksClient disksClient = DisksClient.create()) {
    //  StopGroupAsyncReplicationDiskRequest request =
    //      StopGroupAsyncReplicationDiskRequest.newBuilder()
    //        .setProject(project)
    //        .setZone(disksLocation)
    //        .setDisksStopGroupAsyncReplicationResourceResource(
    //            DisksStopGroupAsyncReplicationResource.newBuilder()
    //                  .setResourcePolicy(resourcePolicy).build())
    //        .build();

    try (RegionDisksClient disksClient = RegionDisksClient.create()) {
      StopGroupAsyncReplicationRegionDiskRequest request =
              StopGroupAsyncReplicationRegionDiskRequest.newBuilder()
                    .setProject(project)
                    .setRegion(disksLocation)
                    .setDisksStopGroupAsyncReplicationResourceResource(
                        DisksStopGroupAsyncReplicationResource.newBuilder()
                              .setResourcePolicy(resourcePolicy).build())
                    .build();
      Operation operation = disksClient.stopGroupAsyncReplicationAsync(request)
              .get(3, TimeUnit.MINUTES);

      if (operation.hasError()) {
        return;
      }
      System.out.printf("Replication stopped for consistency group: %s\n", consistencyGroupName);
    }
  }
}
// [END compute_consistency_group_stop_replication]
