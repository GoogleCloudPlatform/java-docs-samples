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
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.DisksStopGroupAsyncReplicationResource;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.StopGroupAsyncReplicationDiskRequest;
import com.google.cloud.compute.v1.StopGroupAsyncReplicationRegionDiskRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StopDiskReplicationConsistencyGroup {

  public static void main(String[] args)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project that contains the disk.
    String project = "YOUR_PROJECT_ID";
    // Zone or region of the disk.
    String location = "us-central1";
    // Name of the consistency group.
    String consistencyGroupName = "CONSISTENCY_GROUP";
    // Region of the consistency group.
    String consistencyGroupLocation = "us-central1";

    stopDiskReplicationConsistencyGroup(project, location, consistencyGroupName,
            consistencyGroupLocation);
  }

  public static Operation.Status stopDiskReplicationConsistencyGroup(String project,
      String location, String consistencyGroupName, String consistencyGroupLocation)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    String resourcePolicy = String.format(
            "projects/%s/regions/%s/resourcePolicies/%s", project, consistencyGroupLocation,
            consistencyGroupName);
    Operation response;
    if (Character.isDigit(location.charAt(location.length() - 1))) {
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests.
      try (RegionDisksClient disksClient = RegionDisksClient.create()) {
        StopGroupAsyncReplicationRegionDiskRequest request =
                StopGroupAsyncReplicationRegionDiskRequest.newBuilder()
                      .setProject(project)
                      .setRegion(location)
                      .setDisksStopGroupAsyncReplicationResourceResource(
                          DisksStopGroupAsyncReplicationResource.newBuilder()
                                .setResourcePolicy(resourcePolicy).build())
                      .build();
        response = disksClient.stopGroupAsyncReplicationAsync(request)
                .get(3, TimeUnit.MINUTES);
      }
    } else {
      try (DisksClient disksClient = DisksClient.create()) {
        StopGroupAsyncReplicationDiskRequest request =
            StopGroupAsyncReplicationDiskRequest.newBuilder()
              .setProject(project)
              .setZone(location)
              .setDisksStopGroupAsyncReplicationResourceResource(
                  DisksStopGroupAsyncReplicationResource.newBuilder()
                        .setResourcePolicy(resourcePolicy).build())
              .build();
        response = disksClient.stopGroupAsyncReplicationAsync(request)
                .get(3, TimeUnit.MINUTES);
      }
    }
    if (response.hasError()) {
      throw new Error("Error stopping disk replication! " + response.getError());
    }
    return response.getStatus();
  }
}
// [END compute_consistency_group_stop_replication]
