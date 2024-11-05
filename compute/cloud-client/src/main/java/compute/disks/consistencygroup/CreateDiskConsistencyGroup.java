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

// [START compute_consistency_group_create]
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ResourcePoliciesClient;
import com.google.cloud.compute.v1.ResourcePolicy;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CreateDiskConsistencyGroup {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String project = "YOUR_PROJECT_ID";
    // Name of the region in which you want to create the consistency group.
    String region = "us-central1";
    // Name of the consistency group you want to create.
    String consistencyGroupName = "YOUR_CONSISTENCY_GROUP_NAME";

    createDiskConsistencyGroup(project, region, consistencyGroupName);
  }

  // Creates a new disk consistency group resource policy in the specified project and region.
  // Return a link to the consistency group.
  public static String createDiskConsistencyGroup(
      String project, String region, String consistencyGroupName)
      throws IOException, ExecutionException, InterruptedException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ResourcePoliciesClient  regionResourcePoliciesClient = ResourcePoliciesClient.create()) {
      ResourcePolicy resourcePolicy =
          ResourcePolicy.newBuilder()
              .setName(consistencyGroupName)
              .setRegion(region)
              .setDiskConsistencyGroupPolicy(
                  ResourcePolicy.newBuilder().getDiskConsistencyGroupPolicy())
              .build();

      Operation response =
          regionResourcePoliciesClient.insertAsync(project, region, resourcePolicy).get();

      if (response.hasError()) {
        return null;
      }
      return regionResourcePoliciesClient.get(project, region, consistencyGroupName).getSelfLink();
    }
  }
}
// [END compute_consistency_group_create]