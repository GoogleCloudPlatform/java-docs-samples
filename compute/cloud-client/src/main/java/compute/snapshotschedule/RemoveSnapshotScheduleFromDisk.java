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

package compute.snapshotschedule;

// [START compute_snapshot_schedule_remove]
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.DisksRemoveResourcePoliciesRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RemoveResourcePoliciesDiskRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RemoveSnapshotScheduleFromDisk {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the zone where your disk is located.
    String zone = "us-central1-a";
    // Name of the disk you want to remove the snapshot schedule from.
    String diskName = "YOUR_DISK_NAME";
    // Name of the region where your snapshot schedule is located.
    String region = "us-central1";
    // Name of the snapshot schedule you want to attach.
    String snapshotScheduleName = "YOUR_SNAPSHOT_SCHEDULE_NAME";

    removeSnapshotScheduleFromDisk(projectId, zone, diskName, region, snapshotScheduleName);
  }

  // Removes snapshot schedule from a zonal disk.
  public static Operation.Status removeSnapshotScheduleFromDisk(
          String project, String zone, String diskName, String region, String snapshotScheduleName)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    String snapshotSchedulePath = String.format("projects/%s/regions/%s/resourcePolicies/%s",
            project, region, snapshotScheduleName);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (DisksClient disksClient = DisksClient.create()) {
      DisksRemoveResourcePoliciesRequest disksRequest =
              DisksRemoveResourcePoliciesRequest.newBuilder()
                      .addResourcePolicies(snapshotSchedulePath)
                      .build();

      RemoveResourcePoliciesDiskRequest request =
              RemoveResourcePoliciesDiskRequest.newBuilder()
                      .setDisk(diskName)
                      .setProject(project)
                      .setDisksRemoveResourcePoliciesRequestResource(disksRequest)
                      .setZone(zone)
                      .build();

      Operation response = disksClient.removeResourcePoliciesAsync(request)
              .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        throw new Error("Failed to remove resource policies from disk!" + response.getError());
      }
      return response.getStatus();
    }
  }
}
// [END compute_snapshot_schedule_remove]