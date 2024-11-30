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

// [START compute_consistency_group_add_disk]
import com.google.cloud.compute.v1.AddResourcePoliciesDiskRequest;
import com.google.cloud.compute.v1.AddResourcePoliciesRegionDiskRequest;
import com.google.cloud.compute.v1.DisksAddResourcePoliciesRequest;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksAddResourcePoliciesRequest;
import com.google.cloud.compute.v1.RegionDisksClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AddDiskToConsistencyGroup {
  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
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

    addDiskToConsistencyGroup(
        project, location, diskName, consistencyGroupName, consistencyGroupLocation);
  }

  // Adds a disk to a Consistency Group.
  public static Operation.Status addDiskToConsistencyGroup(
      String project, String location, String diskName,
      String consistencyGroupName, String consistencyGroupLocation)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String consistencyGroupUrl = String.format(
        "https://www.googleapis.com/compute/v1/projects/%s/regions/%s/resourcePolicies/%s",
        project, consistencyGroupLocation, consistencyGroupName);
    Operation response;
    if (Character.isDigit(location.charAt(location.length() - 1))) {
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests.
      try (RegionDisksClient disksClient = RegionDisksClient.create()) {
        AddResourcePoliciesRegionDiskRequest request =
                AddResourcePoliciesRegionDiskRequest.newBuilder()
                    .setDisk(diskName)
                    .setRegion(location)
                    .setProject(project)
                    .setRegionDisksAddResourcePoliciesRequestResource(
                            RegionDisksAddResourcePoliciesRequest.newBuilder()
                                .addAllResourcePolicies(Arrays.asList(consistencyGroupUrl))
                                .build())
                    .build();
        response = disksClient.addResourcePoliciesAsync(request).get(1, TimeUnit.MINUTES);
      }
    } else {
      try (DisksClient disksClient = DisksClient.create()) {
        AddResourcePoliciesDiskRequest request =
                AddResourcePoliciesDiskRequest.newBuilder()
                    .setDisk(diskName)
                    .setZone(location)
                    .setProject(project)
                    .setDisksAddResourcePoliciesRequestResource(
                            DisksAddResourcePoliciesRequest.newBuilder()
                                .addAllResourcePolicies(Arrays.asList(consistencyGroupUrl))
                                .build())
                    .build();
        response = disksClient.addResourcePoliciesAsync(request).get(1, TimeUnit.MINUTES);
      }
    }
    if (response.hasError()) {
      throw new Error("Error adding disk to consistency group! " + response.getError());
    }
    return response.getStatus();
  }
}
// [END compute_consistency_group_add_disk]