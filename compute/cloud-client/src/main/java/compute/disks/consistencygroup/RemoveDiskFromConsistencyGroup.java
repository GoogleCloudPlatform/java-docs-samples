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

// [START compute_consistency_group_remove_disk]
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.DisksRemoveResourcePoliciesRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.RegionDisksRemoveResourcePoliciesRequest;
import com.google.cloud.compute.v1.RemoveResourcePoliciesDiskRequest;
import com.google.cloud.compute.v1.RemoveResourcePoliciesRegionDiskRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class RemoveDiskFromConsistencyGroup {

  public static void main(String[] args)
        throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // The project that contains the disk.
    String project = "YOUR_PROJECT_ID";
    // The zone or region of the disk.
    String location = "us-central1";
    // The name of the disk.
    String diskName = "DISK_NAME";
    // The name of the consistency group.
    String consistencyGroupName = "CONSISTENCY_GROUP";
    // The region of the consistency group.
    String consistencyGroupLocation = "us-central1";

    removeDiskFromConsistencyGroup(
            project, location, diskName, consistencyGroupName, consistencyGroupLocation);
  }

  // Removes a disk from a Consistency Group.
  public static Operation.Status removeDiskFromConsistencyGroup(
        String project, String location, String diskName,
        String consistencyGroupName, String consistencyGroupLocation)
        throws IOException, ExecutionException, InterruptedException {
    String consistencyGroupUrl = String.format(
            "https://www.googleapis.com/compute/v1/projects/%s/regions/%s/resourcePolicies/%s",
            project, consistencyGroupLocation, consistencyGroupName);
    Operation response;
    if (Character.isDigit(location.charAt(location.length() - 1))) {
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests.
      try (RegionDisksClient disksClient = RegionDisksClient.create()) {
        RemoveResourcePoliciesRegionDiskRequest request =
                RemoveResourcePoliciesRegionDiskRequest.newBuilder()
                        .setDisk(diskName)
                        .setRegion(location)
                        .setProject(project)
                        .setRegionDisksRemoveResourcePoliciesRequestResource(
                                RegionDisksRemoveResourcePoliciesRequest.newBuilder()
                                        .addAllResourcePolicies(Arrays.asList(consistencyGroupUrl))
                                        .build())
                        .build();

        response = disksClient.removeResourcePoliciesAsync(request).get();
      }
    } else {
      try (DisksClient disksClient = DisksClient.create()) {
        RemoveResourcePoliciesDiskRequest request =
                RemoveResourcePoliciesDiskRequest.newBuilder()
                        .setDisk(diskName)
                        .setZone(location)
                        .setProject(project)
                        .setDisksRemoveResourcePoliciesRequestResource(
                                DisksRemoveResourcePoliciesRequest.newBuilder()
                                        .addAllResourcePolicies(Arrays.asList(consistencyGroupUrl))
                                        .build())
                        .build();
        response = disksClient.removeResourcePoliciesAsync(request).get();
      }
    }
    if (response.hasError()) {
      throw new Error("Error removing disk from consistency group! " + response.getError());
    }
    return response.getStatus();
  }
}
// [END compute_consistency_group_remove_disk]
