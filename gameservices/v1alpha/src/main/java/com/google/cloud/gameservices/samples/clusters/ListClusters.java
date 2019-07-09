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

// [START cloud_game_servers_cluster_list]

import com.google.cloud.gaming.v1alpha.GameServerCluster;
import com.google.cloud.gaming.v1alpha.GameServerClustersServiceClient;
import com.google.cloud.gaming.v1alpha.GameServerClustersServiceClient.ListGameServerClustersPagedResponse;

import java.io.IOException;

public class ListClusters {

  public static void listGameServerClusters(String projectId, String regionId, String realmId)
      throws IOException {
    // String projectId = "your-project-id";
    // String regionId = "us-central1-f";
    try (GameServerClustersServiceClient client = GameServerClustersServiceClient.create()) {
      String parent = String.format(
          "projects/%s/locations/%s/realms/%s", projectId, regionId, realmId);

      ListGameServerClustersPagedResponse response = client.listGameServerClusters(parent);

      for (GameServerCluster cluster : response.iterateAll()) {
        System.out.println("Game Server Cluster found: " + cluster.getName());
      }
    }
  }
}
// [END cloud_game_servers_cluster_list]
