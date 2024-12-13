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

// [START compute_consistency_group_remove_zonal_disk]
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.DisksRemoveResourcePoliciesRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RemoveResourcePoliciesDiskRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RemoveZonalDiskFromConsistencyGroup {

  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project that contains the disk.
    String project = "YOUR_PROJECT_ID";
    // Zone of the disk.
    String diskLocation = "us-central1-a";
    // Name of the disk.
    String diskName = "DISK_NAME";
    // Name of the consistency group.
    String consistencyGroupName = "CONSISTENCY_GROUP";

    removeZonalDiskFromConsistencyGroup(project, diskLocation, diskName, consistencyGroupName);
  }

  // Removes a disk from a consistency group.
  public static Operation.Status removeZonalDiskFromConsistencyGroup(
      String project, String diskLocation, String diskName, String consistencyGroupName)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String region = diskLocation.substring(0, diskLocation.lastIndexOf('-'));
    String consistencyGroupUrl = String.format(
        "https://www.googleapis.com/compute/v1/projects/%s/regions/%s/resourcePolicies/%s",
        project, region, consistencyGroupName);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (DisksClient disksClient = DisksClient.create()) {
      RemoveResourcePoliciesDiskRequest request =
              RemoveResourcePoliciesDiskRequest.newBuilder()
                      .setDisk(diskName)
                      .setZone(diskLocation)
                      .setProject(project)
                      .setDisksRemoveResourcePoliciesRequestResource(
                              DisksRemoveResourcePoliciesRequest.newBuilder()
                                      .addAllResourcePolicies(Arrays.asList(consistencyGroupUrl))
                                      .build())
                      .build();
      Operation response = disksClient.removeResourcePoliciesAsync(request)
              .get(1, TimeUnit.MINUTES);


      if (response.hasError()) {
        throw new Error("Error removing disk from consistency group! " + response.getError());
      }
      return response.getStatus();
    }
  }
}
// [END compute_consistency_group_remove_zonal_disk]