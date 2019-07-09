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

package com.google.cloud.gameservices.samples.scalingpolicies;

// [START cloud_game_servers_scaling_policy_update]

import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.cloud.gaming.v1alpha.ScalingPoliciesServiceClient;
import com.google.cloud.gaming.v1alpha.ScalingPolicy;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Int32Value;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdateScalingPolicy {
  public static void updateScalingPolicy(String projectId, String policyId)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // String projectId = "your-project-id";
    // String policyId = "your-policy-id";
    try (ScalingPoliciesServiceClient client = ScalingPoliciesServiceClient.create()) {
      String policyName = String.format(
          "projects/%s/locations/global/scalingPolicies/%s", projectId, policyId);

      ScalingPolicy policy = ScalingPolicy
          .newBuilder()
          .setPriority(Int32Value.newBuilder().setValue(10).build())
          .build();

      RetryingFuture<OperationSnapshot> poll = client.updateScalingPolicyAsync(
          policy, FieldMask.newBuilder().addPaths("priority").build())
          .getPollingFuture();

      if (poll.get(1, TimeUnit.MINUTES).isDone()) {
        ScalingPolicy updatedPolicy = client.getScalingPolicy(policyName);
        System.out.println("Scaling Policy updated: " + updatedPolicy.getName());
      } else {
        throw new RuntimeException("Scaling Policy update request unsuccessful.");
      }
    }
  }
}
// [END cloud_game_servers_scaling_policy_update]
