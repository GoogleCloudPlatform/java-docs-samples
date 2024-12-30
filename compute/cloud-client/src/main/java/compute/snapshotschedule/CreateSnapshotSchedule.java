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

// [START compute_snapshot_schedule_create]
import com.google.cloud.compute.v1.InsertResourcePolicyRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.ResourcePoliciesClient;
import com.google.cloud.compute.v1.ResourcePolicy;
import com.google.cloud.compute.v1.ResourcePolicyHourlyCycle;
import com.google.cloud.compute.v1.ResourcePolicySnapshotSchedulePolicy;
import com.google.cloud.compute.v1.ResourcePolicySnapshotSchedulePolicyRetentionPolicy;
import com.google.cloud.compute.v1.ResourcePolicySnapshotSchedulePolicyRetentionPolicy.OnSourceDiskDelete;
import com.google.cloud.compute.v1.ResourcePolicySnapshotSchedulePolicySchedule;
import com.google.cloud.compute.v1.ResourcePolicySnapshotSchedulePolicySnapshotProperties;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateSnapshotSchedule {
  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the region in which you want to create the snapshot schedule.
    String region = "us-central1";
    // Name of the snapshot schedule you want to create.
    String snapshotScheduleName = "YOUR_SCHEDULE_NAME";
    // Description of the snapshot schedule.
    String scheduleDescription = "YOUR_SCHEDULE_DESCRIPTION";
    // Maximum number of days to retain snapshots.
    int maxRetentionDays = 10;
    // Storage location for the snapshots.
    // More about storage locations:
    // https://cloud.google.com/compute/docs/disks/snapshots?authuser=0#selecting_a_storage_location
    String storageLocation = "US";

    createSnapshotSchedule(projectId, region, snapshotScheduleName, scheduleDescription,
            maxRetentionDays, storageLocation);
  }

  // Creates a snapshot schedule policy.
  public static Status createSnapshotSchedule(String projectId, String region,
            String snapshotScheduleName, String scheduleDescription, int maxRetentionDays,
            String storageLocation)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ResourcePoliciesClient resourcePoliciesClient = ResourcePoliciesClient.create()) {
      int snapshotInterval = 10; // Create a snapshot every 10 hours
      String startTime = "08:00"; // Define the hourly schedule
      
      ResourcePolicyHourlyCycle hourlyCycle = ResourcePolicyHourlyCycle.newBuilder()
              .setHoursInCycle(snapshotInterval)
              .setStartTime(startTime)
              .build();

      ResourcePolicySnapshotSchedulePolicyRetentionPolicy retentionPolicy =
              ResourcePolicySnapshotSchedulePolicyRetentionPolicy.newBuilder()
                      .setMaxRetentionDays(maxRetentionDays)
                      .setOnSourceDiskDelete(OnSourceDiskDelete.KEEP_AUTO_SNAPSHOTS.toString())
              .build();

      ResourcePolicySnapshotSchedulePolicySnapshotProperties snapshotProperties =
              ResourcePolicySnapshotSchedulePolicySnapshotProperties.newBuilder()
                      .addStorageLocations(storageLocation)
                      .build();

      ResourcePolicySnapshotSchedulePolicy snapshotSchedulePolicy =
              ResourcePolicySnapshotSchedulePolicy.newBuilder()
                      .setRetentionPolicy(retentionPolicy)
                      .setSchedule(ResourcePolicySnapshotSchedulePolicySchedule.newBuilder()
                               .setHourlySchedule(hourlyCycle)
                               .build())
                      .setSnapshotProperties(snapshotProperties)
                      .build();

      ResourcePolicy resourcePolicy = ResourcePolicy.newBuilder()
              .setName(snapshotScheduleName)
              .setDescription(scheduleDescription)
              .setSnapshotSchedulePolicy(snapshotSchedulePolicy)
              .build();
      InsertResourcePolicyRequest request = InsertResourcePolicyRequest.newBuilder()
              .setProject(projectId)
              .setRegion(region)
              .setResourcePolicyResource(resourcePolicy)
              .build();

      Operation response = resourcePoliciesClient.insertAsync(request)
              .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        throw new Error("Snapshot schedule creation failed! " + response.getError());
      }
      return response.getStatus();
    }
  }
}
// [END compute_snapshot_schedule_create]