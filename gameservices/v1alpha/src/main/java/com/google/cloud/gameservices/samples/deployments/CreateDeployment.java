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

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.gaming.v1alpha.CreateGameServerDeploymentRequest;
import com.google.cloud.gaming.v1alpha.GameServerDeployment;
import com.google.cloud.gaming.v1alpha.GameServerDeploymentsServiceClient;
import com.google.cloud.gaming.v1alpha.GameServerTemplate;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.protobuf.Empty;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateDeployment {
  public static void createGameServerDeployment(String projectId, String deploymentId) {
    // String projectId = "your-project-id";
    // String deploymentId = "your-game-server-deployment-id";
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (GameServerDeploymentsServiceClient client = GameServerDeploymentsServiceClient.create()) {

      // Build a spec as shown at https://agones.dev/site/docs/reference/gameserver/
      JsonObject container = new JsonObject();
      container.addProperty("name", "default");
      container.addProperty("image", "gcr.io/agones-images/default:1.0");

      JsonArray containers = new JsonArray();
      containers.add(container);

      JsonObject spec = new JsonObject();
      spec.add("containers", containers);

      JsonObject template = new JsonObject();
      template.add("spec", spec);

      JsonObject port = new JsonObject();
      port.addProperty("name", "default");

      JsonArray ports = new JsonArray();
      ports.add(port);

      JsonObject specObject = new JsonObject();
      specObject.add("ports", ports);
      specObject.add("template", template);

      String parent = String.format("projects/%s/locations/global", projectId);
      String deploymentName = String.format("%s/gameServerDeployments/%s", parent, deploymentId);

      GameServerTemplate gameServerTemplate = GameServerTemplate
          .newBuilder()
          .setSpec(specObject.toString())
          .setTemplateId("default")
          .build();

      GameServerDeployment gameServerDeployment = GameServerDeployment
          .newBuilder()
          .setName(deploymentName)
          .setNewGameServerTemplate(gameServerTemplate)
          .build();

      CreateGameServerDeploymentRequest request = CreateGameServerDeploymentRequest
          .newBuilder()
          .setParent(parent)
          .setDeploymentId(deploymentId)
          .setGameServerDeployment(gameServerDeployment)
          .build();

      OperationFuture<GameServerDeployment, Empty> call = client.createGameServerDeploymentAsync(
          request);

      GameServerDeployment created = call.get(1, TimeUnit.MINUTES);
      System.out.println("Game Server Deployment created: " + created.getName());
    } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
      System.err.println("Game Server Deployment create request unsuccessful.");
      e.printStackTrace(System.err);
    }
  }
}
// [END cloud_game_servers_deployment_create]
