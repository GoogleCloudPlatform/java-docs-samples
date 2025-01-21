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

// [START compute_snapshot_schedule_list]
import com.google.cloud.compute.v1.ListResourcePoliciesRequest;
import com.google.cloud.compute.v1.ResourcePoliciesClient;
import com.google.cloud.compute.v1.ResourcePoliciesClient.ListPagedResponse;
import com.google.cloud.compute.v1.ResourcePolicy;
import java.io.IOException;

public class ListSnapshotSchedules {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the region you want to list snapshot schedules from.
    String region = "us-central1";
    // Name of the snapshot schedule you want to list.
    String snapshotScheduleName = "YOUR_SCHEDULE_NAME";

    listSnapshotSchedules(projectId, region, snapshotScheduleName);
  }

  // Lists snapshot schedules in a specified region, optionally filtered.
  public static ListPagedResponse listSnapshotSchedules(
          String projectId, String region, String snapshotScheduleName) throws IOException {
    String filter = String.format("name = %s", snapshotScheduleName);
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ResourcePoliciesClient resourcePoliciesClient = ResourcePoliciesClient.create()) {

      ListResourcePoliciesRequest request = ListResourcePoliciesRequest.newBuilder()
              .setProject(projectId)
              .setRegion(region)
              .setFilter(filter)
              .build();
      ListPagedResponse response = resourcePoliciesClient.list(request);
      for (ResourcePolicy resourcePolicy : response.iterateAll()) {
        System.out.println(resourcePolicy);
      }
      return response;
    }
  }
}
// [END compute_snapshot_schedule_list]