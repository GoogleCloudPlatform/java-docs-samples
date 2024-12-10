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

// [START compute_consistency_group_clone_zonal_disk]
import com.google.cloud.compute.v1.BulkInsertDiskRequest;
import com.google.cloud.compute.v1.BulkInsertDiskResource;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CloneZonalDisksFromConsistencyGroup {
  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String project = "YOUR_PROJECT_ID";
    // Zone in which your disk is located.
    String disksLocation = "us-central1-a";
    // Name of the consistency group you want to clone disks from.
    String consistencyGroupName = "YOUR_CONSISTENCY_GROUP_NAME";

    cloneZonalDisksFromConsistencyGroup(project, disksLocation, consistencyGroupName);
  }

  // Clones disks with zonal location from a consistency group.
  public static Operation.Status cloneZonalDisksFromConsistencyGroup(
          String project, String zone, String consistencyGroupName)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    String region = zone.substring(0, zone.lastIndexOf('-'));
    String sourceConsistencyGroupPolicy = String.format(
            "projects/%s/regions/%s/resourcePolicies/%s", project, region, consistencyGroupName);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (DisksClient disksClient = DisksClient.create()) {
      BulkInsertDiskRequest request = BulkInsertDiskRequest.newBuilder()
              .setProject(project)
              .setZone(zone)
              .setBulkInsertDiskResourceResource(
                      BulkInsertDiskResource.newBuilder()
                              .setSourceConsistencyGroupPolicy(sourceConsistencyGroupPolicy)
                              .build())
              .build();

      Operation response = disksClient.bulkInsertAsync(request).get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        throw new Error("Error cloning disks! " + response.getError());
      }
      return response.getStatus();
    }
  }
}
// [END compute_consistency_group_clone_zonal_disk]
