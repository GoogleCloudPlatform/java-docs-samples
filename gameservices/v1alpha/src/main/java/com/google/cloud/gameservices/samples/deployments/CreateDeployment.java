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

// [START cloud_game_servers_deployment_create]

import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.cloud.gaming.v1alpha.CreateGameServerDeploymentRequest;
import com.google.cloud.gaming.v1alpha.GameServerDeployment;
import com.google.cloud.gaming.v1alpha.GameServerDeploymentsServiceClient;
import com.google.cloud.gaming.v1alpha.GameServerTemplate;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateDeployment {
  private static String templateSpec = "{\"ports\": [{\"name\": \"default\"}],\"template\":{"
      + "\"spec\":{\"containers\":[{\"name\": \"default\", \"image\": "
      + "\"gcr.io/agones-images/default:1.0\"}]}}}";

  public static void createGameServerDeployment(String projectId, String deploymentId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // String projectId = "your-project-id";
    // String deploymentId = "your-game-server-deployment-id";
    try (GameServerDeploymentsServiceClient client = GameServerDeploymentsServiceClient.create()) {
      String parent = String.format("projects/%s/locations/global", projectId);
      String deploymentName = String.format("%s/gameServerDeployments/%s", parent, deploymentId);

      GameServerDeployment gameServerDeployment = GameServerDeployment
          .newBuilder()
          .setName(deploymentName)
          .setNewGameServerTemplate(GameServerTemplate
              .newBuilder()
              .setSpec(templateSpec)
              .setTemplateId("default")
              .build())
          .build();

      RetryingFuture<OperationSnapshot> poll = client.createGameServerDeploymentAsync(
          CreateGameServerDeploymentRequest
              .newBuilder()
              .setParent(parent)
              .setDeploymentId(deploymentId)
              .setGameServerDeployment(gameServerDeployment)
              .build()).getPollingFuture();

      if (poll.get(1, TimeUnit.MINUTES).isDone()) {
        System.out.println("Game Server Deployment created: " + gameServerDeployment.getName());
      } else {
        throw new RuntimeException("Game Server Deployment create request unsuccessful.");
      }
    }
  }
}
// [END cloud_game_servers_deployment_create]
