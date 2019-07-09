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

import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.cloud.gaming.v1alpha.AllocationPoliciesServiceClient;
import com.google.cloud.gaming.v1alpha.AllocationPolicy;
import com.google.protobuf.FieldMask;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdateAllocationPolicy {
  public static void updateAllocationPolicy(String projectId, String policyId)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // String projectId = "your-project-id";
    // String policyId = "your-policy-id";
    try (AllocationPoliciesServiceClient client = AllocationPoliciesServiceClient.create()) {
      String policyName = String.format(
          "projects/%s/locations/global/allocationPolicies/%s", projectId, policyId);

      AllocationPolicy policy = client.getAllocationPolicy(policyName);

      RetryingFuture<OperationSnapshot> poll = client.updateAllocationPolicyAsync(
          policy.toBuilder().setWeight(5).build(),
          FieldMask.newBuilder().addPaths("weight").build())
          .getPollingFuture();

      if (poll.get(1, TimeUnit.MINUTES).isDone()) {
        AllocationPolicy updatedPolicy = client.getAllocationPolicy(policyName);
        System.out.println("Allocation Policy updated: " + updatedPolicy.getName());
      } else {
        throw new RuntimeException("Allocation Policy update request unsuccessful.");
      }
    }
  }
}
// [END cloud_game_servers_allocation_policy_update]
