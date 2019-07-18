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

package com.google.cloud.gameservices.samples;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.gaming.v1alpha.AllocationPoliciesServiceClient;
import com.google.cloud.gaming.v1alpha.AllocationPoliciesServiceClient.ListAllocationPoliciesPagedResponse;
import com.google.cloud.gaming.v1alpha.AllocationPolicy;
import com.google.cloud.gaming.v1alpha.GameServerCluster;
import com.google.cloud.gaming.v1alpha.GameServerClustersServiceClient;
import com.google.cloud.gaming.v1alpha.GameServerClustersServiceClient.ListGameServerClustersPagedResponse;
import com.google.cloud.gaming.v1alpha.GameServerDeployment;
import com.google.cloud.gaming.v1alpha.GameServerDeploymentsServiceClient;
import com.google.cloud.gaming.v1alpha.GameServerDeploymentsServiceClient.ListGameServerDeploymentsPagedResponse;
import com.google.cloud.gaming.v1alpha.Realm;
import com.google.cloud.gaming.v1alpha.RealmsServiceClient;
import com.google.cloud.gaming.v1alpha.RealmsServiceClient.ListRealmsPagedResponse;
import com.google.cloud.gaming.v1alpha.ScalingPoliciesServiceClient;
import com.google.cloud.gaming.v1alpha.ScalingPoliciesServiceClient.ListScalingPoliciesPagedResponse;
import com.google.cloud.gaming.v1alpha.ScalingPolicy;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class GameServicesTestUtil {
  private static AllocationPoliciesServiceClient allocationsClient;
  private static GameServerClustersServiceClient clustersClient;
  private static GameServerDeploymentsServiceClient deploymentsClient;
  private static RealmsServiceClient realmsClient;
  private static ScalingPoliciesServiceClient scalingsClient;

  static {
    try {
      allocationsClient = AllocationPoliciesServiceClient.create();
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
    try {
      clustersClient = GameServerClustersServiceClient.create();
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
    try {
      deploymentsClient = GameServerDeploymentsServiceClient.create();
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
    try {
      scalingsClient = ScalingPoliciesServiceClient.create();
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
    try {
      realmsClient = RealmsServiceClient.create();
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
  }


  public static void deleteExistingAllocationPolicies(String parent) {
    try {
      ListAllocationPoliciesPagedResponse response =
          allocationsClient.listAllocationPolicies(parent);

      for (AllocationPolicy policy : response.iterateAll()) {
        System.out.println("Deleting allocation policy " + policy.getName());
        OperationFuture poll = allocationsClient.deleteAllocationPolicyAsync(policy.getName());
        poll.get(1, TimeUnit.MINUTES);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }

  public static void deleteExistingClusters(String parent) {
    try {
      ListGameServerClustersPagedResponse response = clustersClient.listGameServerClusters(parent);

      for (GameServerCluster cluster : response.iterateAll()) {
        System.out.println("Deleting game cluster " + cluster.getName());
        OperationFuture poll =
            clustersClient.deleteGameServerClusterAsync(cluster.getName());
        poll.get(1, TimeUnit.MINUTES);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }

  public static void deleteExistingDeployments(String parent) {
    try {
      ListGameServerDeploymentsPagedResponse response =
          deploymentsClient.listGameServerDeployments(parent);

      for (GameServerDeployment deployment : response.iterateAll()) {
        System.out.println("Deleting game server deployment " + deployment.getName());
        OperationFuture poll =
            deploymentsClient.deleteGameServerDeploymentAsync(deployment.getName());
        poll.get(1, TimeUnit.MINUTES);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }

  public static void deleteExistingRealms(String parent) {
    try {
      ListRealmsPagedResponse response = realmsClient.listRealms(parent);

      for (Realm realm : response.iterateAll()) {
        System.out.println("Deleting realm " + realm.getName());
        OperationFuture poll = realmsClient.deleteRealmAsync(realm.getName());
        poll.get(1, TimeUnit.MINUTES);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }

  public static void deleteExistingScalingPolicies(String parent) {
    try {
      ListScalingPoliciesPagedResponse response = scalingsClient.listScalingPolicies(parent);

      for (ScalingPolicy policy : response.iterateAll()) {
        System.out.println("Deleting scaling policy " + policy.getName());
        OperationFuture poll = scalingsClient.deleteScalingPolicyAsync(policy.getName());
        poll.get(1, TimeUnit.MINUTES);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }
}
