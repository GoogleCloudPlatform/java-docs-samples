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

// [START cloud_game_servers_deployment_delete]

import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.cloud.gaming.v1alpha.GameServerDeploymentsServiceClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteDeployment {
  public static void deleteGameServerDeployment(String projectId, String deploymentId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // String projectId = "your-project-id";
    // String deploymentId = "your-game-server-deployment-id";
    try (GameServerDeploymentsServiceClient client = GameServerDeploymentsServiceClient.create()) {
      String parent = String.format("projects/%s/locations/global", projectId);
      String deploymentName = String.format("%s/gameServerDeployments/%s", parent, deploymentId);

      RetryingFuture<OperationSnapshot> poll = client
          .deleteGameServerDeploymentAsync(deploymentName)
          .getPollingFuture();

      if (poll.get(1, TimeUnit.MINUTES).isDone()) {
        System.out.println("Game Server Deployment deleted: " + deploymentName);
      } else {
        throw new RuntimeException("Game Server Deployment delete request unsuccessful.");
      }
    }
  }
}
// [END cloud_game_servers_deployment_delete]
