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

// [START compute_consistency_group_delete]
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ResourcePoliciesClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DeleteDiskConsistencyGroup {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String project = "YOUR_PROJECT_ID";
    // Name of the region in which your consistency group is located.
    String region = "us-central1";
    // Name of the consistency group you want to delete.
    String consistencyGroupName = "YOUR_CONSISTENCY_GROUP_NAME";

    deleteDiskConsistencyGroup(project, region, consistencyGroupName);
  }

  // Deletes a disk consistency group resource policy in the specified project and region.
  public static Operation.Status deleteDiskConsistencyGroup(
      String project, String region, String consistencyGroupName)
      throws IOException, ExecutionException, InterruptedException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ResourcePoliciesClient regionResourcePoliciesClient = ResourcePoliciesClient.create()) {
      Operation response = regionResourcePoliciesClient
          .deleteAsync(project, region, consistencyGroupName).get();

      if (response.hasError()) {
        System.out.printf("Error deleting disk: %s%n", response.getError());
        return null;
      }
      return response.getStatus();
    }
  }
}
// [END compute_consistency_group_delete]
