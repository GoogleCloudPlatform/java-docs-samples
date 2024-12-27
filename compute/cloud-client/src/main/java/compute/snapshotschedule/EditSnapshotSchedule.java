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

// [START compute_snapshot_schedule_edit]
import static com.google.cloud.compute.v1.ResourcePolicySnapshotSchedulePolicyRetentionPolicy.OnSourceDiskDelete.APPLY_RETENTION_POLICY;

import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.PatchResourcePolicyRequest;
import com.google.cloud.compute.v1.ResourcePoliciesClient;
import com.google.cloud.compute.v1.ResourcePolicy;
import com.google.cloud.compute.v1.ResourcePolicySnapshotSchedulePolicy;
import com.google.cloud.compute.v1.ResourcePolicySnapshotSchedulePolicyRetentionPolicy;
import com.google.cloud.compute.v1.ResourcePolicySnapshotSchedulePolicySchedule;
import com.google.cloud.compute.v1.ResourcePolicySnapshotSchedulePolicySnapshotProperties;
import com.google.cloud.compute.v1.ResourcePolicyWeeklyCycle;
import com.google.cloud.compute.v1.ResourcePolicyWeeklyCycleDayOfWeek;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EditSnapshotSchedule {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the region where your snapshot schedule is located.
    String region = "us-central1";
    // Name of the snapshot schedule you want to update.
    String snapshotScheduleName = "YOUR_SCHEDULE_NAME";

    editSnapshotSchedule(projectId, region, snapshotScheduleName);
  }

  // Edits a snapshot schedule.
  public static Status editSnapshotSchedule(
          String projectId, String region, String snapshotScheduleName)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ResourcePoliciesClient resourcePoliciesClient = ResourcePoliciesClient.create()) {
      Map<String, String> snapshotLabels = new HashMap<>();
      snapshotLabels.put("key", "value");

      ResourcePolicySnapshotSchedulePolicySnapshotProperties.Builder snapshotProperties =
              ResourcePolicySnapshotSchedulePolicySnapshotProperties.newBuilder();
      snapshotProperties.putAllLabels(snapshotLabels);

      ResourcePolicyWeeklyCycleDayOfWeek dayOfWeek = ResourcePolicyWeeklyCycleDayOfWeek.newBuilder()
              .setDay("Tuesday")
              .setStartTime("09:00")
              .build();
      ResourcePolicyWeeklyCycle weeklySchedule = ResourcePolicyWeeklyCycle.newBuilder()
              .addDayOfWeeks(dayOfWeek)
              .build();

      ResourcePolicySnapshotSchedulePolicySchedule.Builder scheduler =
              ResourcePolicySnapshotSchedulePolicySchedule.newBuilder();
      scheduler.clearDailySchedule().clearHourlySchedule();
      scheduler.setWeeklySchedule(weeklySchedule);

      String onSourceDiskDelete = APPLY_RETENTION_POLICY.toString();
      int maxRetentionDays = 3;

      ResourcePolicySnapshotSchedulePolicyRetentionPolicy.Builder retentionPolicy =
              ResourcePolicySnapshotSchedulePolicyRetentionPolicy.newBuilder();
      retentionPolicy.setOnSourceDiskDelete(onSourceDiskDelete);
      retentionPolicy.setMaxRetentionDays(maxRetentionDays);

      String description = "Updated description";

      ResourcePolicy updatedSchedule = ResourcePolicy.newBuilder()
              .setName(snapshotScheduleName)
              .setDescription(description)
              .setSnapshotSchedulePolicy(
                      ResourcePolicySnapshotSchedulePolicy.newBuilder()
                              .setSchedule(scheduler)
                              .setSnapshotProperties(snapshotProperties)
                              .setRetentionPolicy(retentionPolicy.build())
                              .build())
              .build();

      PatchResourcePolicyRequest request = PatchResourcePolicyRequest.newBuilder()
              .setProject(projectId)
              .setRegion(region)
              .setResourcePolicy(snapshotScheduleName)
              .setResourcePolicyResource(updatedSchedule)
              .build();

      Operation response = resourcePoliciesClient.patchAsync(request).get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        throw new Error("Failed to update snapshot schedule! " + response.getError());
      }
      return response.getStatus();
    }
  }
}
// [END compute_snapshot_schedule_edit]