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
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ResourcePoliciesClient;
import com.google.cloud.compute.v1.ResourcePolicy;
import com.google.cloud.compute.v1.ResourcePolicyHourlyCycle;
import com.google.cloud.compute.v1.ResourcePolicySnapshotSchedulePolicy;
import com.google.cloud.compute.v1.ResourcePolicySnapshotSchedulePolicyRetentionPolicy;
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
    String scheduleName = "YOUR_SCHEDULE_NAME";
    // Description of the snapshot schedule.
    String scheduleDescription = "YOUR_SCHEDULE_DESCRIPTION";
    // Maximum number of days to retain snapshots.
    int maxRetentionDays = 10;
    // Storage location for the snapshots.
    // More about storage locations:
    // https://cloud.google.com/compute/docs/disks/snapshots?authuser=0#selecting_a_storage_location
    String storageLocation = "US";
    // Determines what happens to your snapshots if the source disk is deleted.
    String onSourceDiskDelete = "KEEP_AUTO_SNAPSHOTS";

    createSnapshotSchedule(projectId, region, scheduleName, scheduleDescription, maxRetentionDays,
             storageLocation, onSourceDiskDelete);
  }

  // Creates a snapshot schedule policy.
  public static Operation.Status createSnapshotSchedule(String projectId, String region,
            String scheduleName, String scheduleDescription, int maxRetentionDays,
            String storageLocation, String onSourceDiskDelete)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String startTime = "08:00";
    ResourcePolicySnapshotSchedulePolicySnapshotProperties snapshotProperties =
            ResourcePolicySnapshotSchedulePolicySnapshotProperties.newBuilder()
                    .addStorageLocations(storageLocation)
                    .build();

    // Define the hourly schedule:
    int snapshotInterval = 10; // Create a snapshot every 10 hours
    ResourcePolicyHourlyCycle hourlyCycle = ResourcePolicyHourlyCycle.newBuilder()
            .setHoursInCycle(snapshotInterval)
            .setStartTime(startTime)
            .build();

    // Define the daily schedule.
    // ResourcePolicyDailyCycle dailySchedule =
    //         ResourcePolicyDailyCycle.newBuilder()
    //                 .setDaysInCycle(1)  // Every day
    //                 .setStartTime(startTime)
    //                 .build();

    // Define the weekly schedule.
    // List<ResourcePolicyWeeklyCycleDayOfWeek> dayOfWeeks = new ArrayList<>();
    // ResourcePolicyWeeklyCycleDayOfWeek tuesdaySchedule =
    //         ResourcePolicyWeeklyCycleDayOfWeek.newBuilder()
    //                 .setDay(ResourcePolicyWeeklyCycleDayOfWeek.Day.TUESDAY.toString())
    //                 .setStartTime(startTime)
    //                 .build();
    // dayOfWeeks.add(tuesdaySchedule);
    //
    // ResourcePolicyWeeklyCycle weeklyCycle = ResourcePolicyWeeklyCycle.newBuilder()
    //         .addAllDayOfWeeks(dayOfWeeks)
    //         .build();

    ResourcePolicySnapshotSchedulePolicyRetentionPolicy retentionPolicy =
            ResourcePolicySnapshotSchedulePolicyRetentionPolicy.newBuilder()
                    .setMaxRetentionDays(maxRetentionDays)
                    .setOnSourceDiskDelete(onSourceDiskDelete)
            .build();

    ResourcePolicySnapshotSchedulePolicy snapshotSchedulePolicy =
            ResourcePolicySnapshotSchedulePolicy.newBuilder()
                    .setRetentionPolicy(retentionPolicy)
                    .setSchedule(ResourcePolicySnapshotSchedulePolicySchedule.newBuilder()
                             // You can set only one of the following options:
                             .setHourlySchedule(hourlyCycle)  //Set Hourly Schedule
                             // .setDailySchedule(dailySchedule) //Set Daily Schedule
                             // .setWeeklySchedule(weeklyCycle)    // Set Weekly Schedule
                             .build())
                    .setSnapshotProperties(snapshotProperties)
                    .build();

    ResourcePolicy resourcePolicy = ResourcePolicy.newBuilder()
            .setName(scheduleName)
            .setDescription(scheduleDescription)
            .setSnapshotSchedulePolicy(snapshotSchedulePolicy)
            .build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ResourcePoliciesClient resourcePoliciesClient = ResourcePoliciesClient.create()) {

      Operation response = resourcePoliciesClient.insertAsync(projectId, region, resourcePolicy)
              .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.printf("Snapshot schedule creation failed: %s%n", response.getError());
        return null;
      }
      return response.getStatus();
    }
  }
}
// [END compute_snapshot_schedule_create]