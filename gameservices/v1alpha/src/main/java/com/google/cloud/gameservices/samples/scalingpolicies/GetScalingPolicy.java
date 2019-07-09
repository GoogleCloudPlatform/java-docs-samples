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

// [START cloud_game_servers_scaling_policy_get]

import com.google.cloud.gaming.v1alpha.ScalingPoliciesServiceClient;
import com.google.cloud.gaming.v1alpha.ScalingPolicy;

import java.io.IOException;

public class GetScalingPolicy {
  public static void getScalingPolicy(String projectId, String policyId)
      throws IOException {
    // String projectId = "your-project-id";
    // String policyId = "your-policy-id";
    try (ScalingPoliciesServiceClient client = ScalingPoliciesServiceClient.create()) {
      String policyName = String.format(
          "projects/%s/locations/global/scalingPolicies/%s", projectId, policyId);

      ScalingPolicy scalingPolicy = client.getScalingPolicy(policyName);

      System.out.println("Scaling Policy found: " + scalingPolicy.getName());
    }
  }
}
// [END cloud_game_servers_scaling_policy_get]
