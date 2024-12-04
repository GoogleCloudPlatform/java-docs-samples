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

package compute.disks;

// [START compute_disk_create_with_snapshot_schedule]
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateDiskWithSnapshotSchedule {
  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the zone in which you want to create the disk.
    String zone = "us-central1-a";
    // Name of the disk you want to create.
    String diskName = "YOUR_DISK_NAME";
    // Name of the schedule you want to link to the disk.
    String snapshotScheduleName = "YOUR_SCHEDULE_NAME";

    createDiskWithSnapshotSchedule(projectId, zone, diskName, snapshotScheduleName);
  }

  // Creates disk with linked snapshot schedule.
  public static Operation.Status createDiskWithSnapshotSchedule(
      String projectId, String zone, String diskName, String snapshotScheduleName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (DisksClient disksClient = DisksClient.create()) {

      // Get the resource policy to link to the disk
      String resourcePolicyLink = String.format("projects/%s/regions/%s/resourcePolicies/%s",
              projectId, zone.substring(0, zone.lastIndexOf('-')), snapshotScheduleName);

      Disk disk = Disk.newBuilder()
              .setName(diskName)
              .setZone(zone)
              .addAllResourcePolicies(List.of(resourcePolicyLink))
              .build();

      Operation response = disksClient.insertAsync(projectId, zone, disk).get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        throw new Error("Disk creation failed! " + response.getError());
      }
      return response.getStatus();
    }
  }
}
// [END compute_disk_create_with_snapshot_schedule]
