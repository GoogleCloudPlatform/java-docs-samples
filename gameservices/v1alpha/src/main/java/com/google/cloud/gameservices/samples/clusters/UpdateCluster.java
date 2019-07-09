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

package com.google.cloud.gameservices.samples.clusters;

// [START cloud_game_servers_cluster_update]

import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.cloud.gaming.v1alpha.GameServerCluster;
import com.google.cloud.gaming.v1alpha.GameServerClusterConnectionInfo;
import com.google.cloud.gaming.v1alpha.GameServerClustersServiceClient;
import com.google.protobuf.FieldMask;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdateCluster {
  public static void updateGameServerCluster(
      String projectId, String regionId, String realmId, String clusterId)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // String projectId = "your-project-id";
    // String regionId = "us-central1-f";
    // String realmId = "your-realm-id";
    // String clusterId = "your-game-server-cluster-id";
    try (GameServerClustersServiceClient client = GameServerClustersServiceClient.create()) {
      String parent = String.format(
          "projects/%s/locations/%s/realms/%s", projectId, regionId, realmId);
      String clusterName = String.format(
          "%s/gameServerClusters/%s", parent, clusterId);

      GameServerCluster cluster = GameServerCluster
          .newBuilder()
          .setConnectionInfo(
              GameServerClusterConnectionInfo
                  .newBuilder()
                  .setNamespace("default"))
          .build();

      RetryingFuture<OperationSnapshot> poll = client.updateGameServerClusterAsync(
          cluster, FieldMask.newBuilder().addPaths("connection_info").build())
          .getPollingFuture();

      if (poll.get(1, TimeUnit.MINUTES).isDone()) {
        GameServerCluster updatedPolicy = client.getGameServerCluster(clusterName);
        System.out.println("Game Server Cluster updated: " + updatedPolicy.getName());
      } else {
        throw new RuntimeException("Game Server Cluster update request unsuccessful.");
      }
    }
  }
}
// [END cloud_game_servers_cluster_update]
