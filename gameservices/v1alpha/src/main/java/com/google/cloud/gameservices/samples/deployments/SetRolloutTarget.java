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

package com.google.cloud.gameservices.samples.deployments;

// [START cloud_game_servers_deployment_start_rollout]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.gaming.v1alpha.ClusterPercentageSelector;
import com.google.cloud.gaming.v1alpha.GameServerDeployment;
import com.google.cloud.gaming.v1alpha.GameServerDeploymentsServiceClient;
import com.google.cloud.gaming.v1alpha.SetRolloutTargetRequest;
import com.google.protobuf.Empty;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SetRolloutTarget {
  public static void setRolloutTarget(String deploymentName) {
    // String deploymentName =
    //     "projects/{project_id}/locations/{location}/gameServerDeployments/{deployment_id}";
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (GameServerDeploymentsServiceClient client = GameServerDeploymentsServiceClient.create()) {
      ClusterPercentageSelector percentageSelector = ClusterPercentageSelector
          .newBuilder()
          .setPercent(50)
          .build();
      SetRolloutTargetRequest request = SetRolloutTargetRequest
          .newBuilder()
          .setName(deploymentName)
          .addClusterPercentageSelector(percentageSelector)
          .build();

      OperationFuture<GameServerDeployment, Empty> call = client.setRolloutTargetAsync(request);

      GameServerDeployment result = call.get(1, TimeUnit.MINUTES);
      System.out.println("Rollout target set: " + result.getName());
    } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
      System.err.println("Set Rollout Target request unsuccessful.");
      e.printStackTrace(System.err);
    }
  }
}
// [END cloud_game_servers_deployment_set_rollout_target]
