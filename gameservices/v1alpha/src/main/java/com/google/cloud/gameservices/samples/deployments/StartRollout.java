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

import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.cloud.gaming.v1alpha.GameServerDeploymentsServiceClient;
import com.google.cloud.gaming.v1alpha.GameServerTemplate;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StartRollout {
  private static String templateSpec = "{\"ports\": [{\"name\": \"default\"}],\"template\":{"
      + "\"spec\":{\"containers\":[{\"name\": \"default\", \"image\": "
      + "\"gcr.io/agones-images/default:1.0\"}]}}}";

  public static void startRollout(String deploymentName, String templateId)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // String deploymentName =
    //     "projects/{project_id}/locations/{location}/gameServerDeployments/{deployment_id}";
    // String templateId = "your-game-server-template-id";
    try (GameServerDeploymentsServiceClient client = GameServerDeploymentsServiceClient.create()) {
      RetryingFuture<OperationSnapshot> poll = client.startRolloutAsync(
          deploymentName,
          GameServerTemplate
              .newBuilder()
              .setTemplateId(templateId)
              .setSpec(templateSpec)
              .build())
          .getPollingFuture();

      OperationSnapshot response = poll.get(1, TimeUnit.MINUTES);
      if (response.isDone()) {
        System.out.println("Rollout started: " + response.getResponse());
      } else {
        throw new RuntimeException("Start Rollout request unsuccessful.");
      }
    }
  }
}
// [END cloud_game_servers_deployment_start_rollout]
