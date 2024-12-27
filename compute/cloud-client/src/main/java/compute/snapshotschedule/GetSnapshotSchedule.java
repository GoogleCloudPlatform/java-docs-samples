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

// [START compute_snapshot_schedule_get]
import com.google.cloud.compute.v1.GetResourcePolicyRequest;
import com.google.cloud.compute.v1.ResourcePoliciesClient;
import com.google.cloud.compute.v1.ResourcePolicy;
import java.io.IOException;

public class GetSnapshotSchedule {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the region in which your snapshot schedule is located.
    String region = "us-central1";
    // Name of your snapshot schedule.
    String snapshotScheduleName = "YOUR_SCHEDULE_NAME";

    getSnapshotSchedule(projectId, region, snapshotScheduleName);
  }

  // Retrieves the details of a snapshot schedule.
  public static ResourcePolicy getSnapshotSchedule(
        String projectId, String region, String snapshotScheduleName) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ResourcePoliciesClient resourcePoliciesClient = ResourcePoliciesClient.create()) {
      GetResourcePolicyRequest request = GetResourcePolicyRequest.newBuilder()
              .setProject(projectId)
              .setRegion(region)
              .setResourcePolicy(snapshotScheduleName)
              .build();
      ResourcePolicy resourcePolicy = resourcePoliciesClient.get(request);
      System.out.println(resourcePolicy);

      return resourcePolicy;
    }
  }
}
// [END compute_snapshot_schedule_get]
