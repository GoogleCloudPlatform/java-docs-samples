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

// [START cloud_game_servers_deployment_update]

import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.cloud.gaming.v1alpha.GameServerDeployment;
import com.google.cloud.gaming.v1alpha.GameServerDeploymentsServiceClient;
import com.google.cloud.gaming.v1alpha.GameServerTemplate;
import com.google.protobuf.FieldMask;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdateDeployment {
  public static void updateGameServerDeployment(String projectId, String deploymentId)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // String projectId = "your-project-id";
    // String deploymentId = "your-game-server-deployment-id";
    try (GameServerDeploymentsServiceClient client = GameServerDeploymentsServiceClient.create()) {
      String deploymentName = String.format(
          "projects/%s/locations/global/gameServerDeployments/%s",
          projectId,
          deploymentId);

      GameServerDeployment deployment = GameServerDeployment
          .newBuilder()
          .setNewGameServerTemplate(GameServerTemplate
              .newBuilder()
              .setDescription("Updated deployment template.")
              .build())
          .build();

      RetryingFuture<OperationSnapshot> poll = client.updateGameServerDeploymentAsync(
          deployment, FieldMask
              .newBuilder()
              .addPaths("new_game_server_template.description")
              .build())
          .getPollingFuture();

      if (poll.get(1, TimeUnit.MINUTES).isDone()) {
        GameServerDeployment updatedPolicy = client.getGameServerDeployment(deploymentName);
        System.out.println("Game Server Deployment updated: " + updatedPolicy.getName());
      } else {
        throw new RuntimeException("Game Server Deployment update request unsuccessful.");
      }
    }
  }
}
// [END cloud_game_servers_deployment_update]
