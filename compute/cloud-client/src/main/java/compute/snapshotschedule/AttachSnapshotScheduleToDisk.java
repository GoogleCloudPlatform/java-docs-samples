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

// [START compute_snapshot_schedule_attach]
import com.google.cloud.compute.v1.AddResourcePoliciesDiskRequest;
import com.google.cloud.compute.v1.DisksAddResourcePoliciesRequest;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AttachSnapshotScheduleToDisk {
  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the zone where your disk is located.
    String zone = "us-central1-a";
    // Name of the disk you want to attach the snapshot schedule to.
    String diskName = "YOUR_DISK_NAME";
    // Name of the snapshot schedule you want to attach.
    String snapshotScheduleName = "YOUR_SNAPSHOT_SCHEDULE_NAME";
    // Name of the region where your snapshot schedule is located.
    String region = "us-central1";

    attachSnapshotScheduleToDisk(projectId, zone, diskName, snapshotScheduleName, region);
  }

  // Attaches a snapshot schedule to a disk.
  public static Status attachSnapshotScheduleToDisk(
        String projectId, String zone, String diskName, String snapshotScheduleName, String region)
        throws IOException, ExecutionException, InterruptedException, TimeoutException {

    String resourcePolicyLink = String.format(
            "projects/%s/regions/%s/resourcePolicies/%s", projectId, region, snapshotScheduleName);
    try (DisksClient disksClient = DisksClient.create()) {

      AddResourcePoliciesDiskRequest request = AddResourcePoliciesDiskRequest.newBuilder()
              .setDisk(diskName)
              .setDisksAddResourcePoliciesRequestResource(
                      DisksAddResourcePoliciesRequest.newBuilder()
                              .addResourcePolicies(resourcePolicyLink)
                              .build())
              .setProject(projectId)
              .setZone(zone)
              .build();

      Operation response = disksClient.addResourcePoliciesAsync(request).get(1, TimeUnit.MINUTES);

      if (response.hasError()) {
        throw new Error("Error attaching snapshot schedule to disk: " + response.getError());
      }
      return response.getStatus();
    }
  }
}
// [END compute_snapshot_schedule_attach]