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

// [START compute_snapshot_schedule_delete]
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.ResourcePoliciesClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteSnapshotSchedule {
  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the region where your snapshot schedule is located.
    String region = "us-central1";
    // Name of the snapshot schedule you want to delete.
    String snapshotScheduleName = "YOUR_SCHEDULE_NAME";

    deleteSnapshotSchedule(projectId, region, snapshotScheduleName);
  }

  // Deletes a snapshot schedule policy.
  public static Status deleteSnapshotSchedule(
          String projectId, String region, String snapshotScheduleName)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ResourcePoliciesClient resourcePoliciesClient = ResourcePoliciesClient.create()) {
      Operation response =  resourcePoliciesClient
              .deleteAsync(projectId, region, snapshotScheduleName)
              .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.printf("Snapshot schedule deletion failed: %s%n", response.getError());
        return null;
      }
      return response.getStatus();
    }
  }
}
// [END compute_snapshot_schedule_delete]