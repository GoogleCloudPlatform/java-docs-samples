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

import com.google.cloud.gaming.v1alpha.DeploymentTarget;
import com.google.cloud.gaming.v1alpha.GameServerDeploymentsServiceClient;

import java.io.IOException;

public class GetDeploymentTarget {
  public static void getDeploymentTarget(String deploymentName) throws IOException {
    // String deploymentName =
    //     "projects/{project_id}/locations/{location}/gameServerDeployments/{deployment_id}";
    try (GameServerDeploymentsServiceClient client = GameServerDeploymentsServiceClient.create()) {
      DeploymentTarget target = client.getDeploymentTarget(deploymentName);

      System.out.printf("Found target with %d clusters.", target.getClustersCount());
    }
  }
}
// [END cloud_game_servers_deployment_start_rollout]
