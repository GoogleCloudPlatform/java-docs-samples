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

// [START cloud_game_servers_scaling_policy_create]

import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.cloud.gaming.v1alpha.CreateScalingPolicyRequest;
import com.google.cloud.gaming.v1alpha.FleetAutoscalerSettings;
import com.google.cloud.gaming.v1alpha.ScalingPoliciesServiceClient;
import com.google.cloud.gaming.v1alpha.ScalingPolicy;
import com.google.protobuf.Int32Value;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateScalingPolicy {
  public static void createScalingPolicy(
      String projectId, String policyId, String deploymentId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // String projectId = "your-project-id";
    // String policyId = "your-policy-id";
    // String deploymentId = "your-deployment-id";
    try (ScalingPoliciesServiceClient client = ScalingPoliciesServiceClient.create()) {
      String parent = String.format("projects/%s/locations/global", projectId);
      String policyName = String.format("%s/scalingPolicies/%s", parent, policyId);

      String deploymentName = String.format("%s/gameServerDeployments/%s", parent, deploymentId);

      ScalingPolicy policy = ScalingPolicy
          .newBuilder()
          .setName(policyName)
          .setPriority(Int32Value.newBuilder().setValue(1))
          .setFleetAutoscalerSettings(FleetAutoscalerSettings
              .newBuilder()
              .setBufferSizeAbsolute(1)
              .setMinReplicas(1)
              .setMaxReplicas(2)
              .build())
          .setGameServerDeployment(deploymentName)
          .build();

      RetryingFuture<OperationSnapshot> poll = client.createScalingPolicyAsync(
          CreateScalingPolicyRequest
              .newBuilder()
              .setParent(parent)
              .setScalingPolicyId(policyId)
              .setScalingPolicy(policy)
              .build()).getPollingFuture();

      if (poll.get(1, TimeUnit.MINUTES).isDone()) {
        System.out.println("Scaling Policy created: " + policy.getName());
      } else {
        throw new RuntimeException("Scaling Policy create request unsuccessful.");
      }
    }
  }
}
// [END cloud_game_servers_scaling_policy_create]
