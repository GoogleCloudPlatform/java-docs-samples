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

// [START cloud_game_servers_allocation_policy_create]

import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.cloud.gaming.v1alpha.AllocationPoliciesServiceClient;
import com.google.cloud.gaming.v1alpha.AllocationPolicy;
import com.google.cloud.gaming.v1alpha.CreateAllocationPolicyRequest;
import com.google.protobuf.Int32Value;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateAllocationPolicy {
  public static void createAllocationPolicy(String projectId, String policyId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // String projectId = "your-project-id";
    // String policyId = "your-policy-id";
    try (AllocationPoliciesServiceClient client = AllocationPoliciesServiceClient.create()) {
      String parent = String.format("projects/%s/locations/global", projectId);
      String policyName = String.format("%s/allocationPolicies/%s", parent, policyId);

      AllocationPolicy policy = AllocationPolicy
          .newBuilder()
          .setName(policyName)
          .setPriority(Int32Value.newBuilder().setValue(1))
          .build();

      RetryingFuture<OperationSnapshot> poll = client.createAllocationPolicyAsync(
          CreateAllocationPolicyRequest
              .newBuilder()
              .setParent(parent)
              .setAllocationPolicyId(policyId)
              .setAllocationPolicy(policy)
              .build()).getPollingFuture();

      if (poll.get(1, TimeUnit.MINUTES).isDone()) {
        System.out.println("Allocation Policy created: " + policy.getName());
      } else {
        throw new RuntimeException("Allocation Policy create request unsuccessful.");
      }
    }
  }
}
// [END cloud_game_servers_allocation_policy_create]
