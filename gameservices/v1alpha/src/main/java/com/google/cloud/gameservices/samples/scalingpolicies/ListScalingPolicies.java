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

// [START cloud_game_servers_scaling_policy_list]

import com.google.cloud.gaming.v1alpha.ListScalingPoliciesRequest;
import com.google.cloud.gaming.v1alpha.ScalingPoliciesServiceClient;
import com.google.cloud.gaming.v1alpha.ScalingPoliciesServiceClient.ListScalingPoliciesPagedResponse;
import com.google.cloud.gaming.v1alpha.ScalingPolicy;
import com.google.common.base.Strings;

import java.io.IOException;

public class ListScalingPolicies {
  public static void listScalingPolicies(String projectId) {
    // String projectId = "your-project-id";
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (ScalingPoliciesServiceClient client = ScalingPoliciesServiceClient.create()) {
      String parent = String.format("projects/%s/locations/global", projectId);

      ListScalingPoliciesPagedResponse response = client.listScalingPolicies(parent);
      for (ScalingPolicy policy : response.iterateAll()) {
        System.out.println("Scaling Policy found: " + policy.getName());
      }

      while (!Strings.isNullOrEmpty(response.getNextPageToken())) {
        ListScalingPoliciesRequest request = ListScalingPoliciesRequest
            .newBuilder()
            .setParent(parent)
            .setPageToken(response.getNextPageToken())
            .build();
        response = client.listScalingPolicies(request);
        for (ScalingPolicy policy : response.iterateAll()) {
          System.out.println("Scaling Policy found: " + policy.getName());
        }
      }
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
  }
}
// [END cloud_game_servers_scaling_policy_list]
