/*
 * Copyright 2019 Google LLC
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

package com.google.cloud.gameservices.samples.allocationpolicies;

// [START cloud_game_servers_allocation_policy_update]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.gaming.v1alpha.AllocationPoliciesServiceClient;
import com.google.cloud.gaming.v1alpha.AllocationPolicy;
import com.google.protobuf.Empty;
import com.google.protobuf.FieldMask;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdateAllocationPolicy {
  public static void updateAllocationPolicy(String projectId, String policyId) {
    // String projectId = "your-project-id";
    // String policyId = "your-policy-id";
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (AllocationPoliciesServiceClient client = AllocationPoliciesServiceClient.create()) {
      String policyName = String.format(
          "projects/%s/locations/global/allocationPolicies/%s", projectId, policyId);

      AllocationPolicy policy = client
          .getAllocationPolicy(policyName)
          .toBuilder()
          .setWeight(5)
          .build();

      FieldMask fieldMask = FieldMask.newBuilder().addPaths("weight").build();

      OperationFuture<AllocationPolicy, Empty> call = client.updateAllocationPolicyAsync(
          policy, fieldMask);

      AllocationPolicy updated = call.get(1, TimeUnit.MINUTES);
      System.out.println("Allocation Policy updated: " + updated.getName());
    } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
      System.err.println("Allocation Policy update request unsuccessful.");
      e.printStackTrace(System.err);
    }
  }
}
// [END cloud_game_servers_allocation_policy_update]
