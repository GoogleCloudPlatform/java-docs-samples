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

import static org.junit.Assert.assertTrue;

import com.google.cloud.gameservices.samples.clusters.CreateCluster;
import com.google.cloud.gameservices.samples.clusters.DeleteCluster;
import com.google.cloud.gameservices.samples.clusters.GetCluster;
import com.google.cloud.gameservices.samples.clusters.ListClusters;
import com.google.cloud.gameservices.samples.clusters.UpdateCluster;
import com.google.cloud.gameservices.samples.realms.CreateRealm;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ClusterTests {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

  private static String parentName = String.format(
      "projects/%s/locations/%s", PROJECT_ID, REGION_ID);

  private static String realmId = "realm-1";
  private static String realmName = String.format("%s/realms/%s", parentName, realmId);

  private static String clusterId = "cluster-1";
  private static String clusterName = String.format(
      "%s/gameServerClusters/%s", realmName, clusterId);

  private static String gkeClusterName = System.getenv("GKE_CLUSTER");

  private final PrintStream originalOut = System.out;
  private ByteArrayOutputStream bout;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @BeforeClass
  public static void init() {
    // Delete all existing clusters and realms.
    GameServicesTestUtil.deleteExistingClusters(realmName);
    GameServicesTestUtil.deleteExistingRealms(parentName);
    CreateRealm.createRealm(PROJECT_ID, REGION_ID, realmId);
    CreateCluster.createGameServerCluster(
        PROJECT_ID, REGION_ID, realmId, clusterId, gkeClusterName);
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    bout.reset();
  }

  @AfterClass
  public static void tearDownClass() {
    GameServicesTestUtil.deleteExistingClusters(realmName);
    GameServicesTestUtil.deleteExistingRealms(parentName);
  }

  @Test
  public void createDeleteClusterTest() {
    String newClusterId = "cluster-2";
    String newClusterName = String.format(
        "%s/gameServerClusters/%s", realmName, newClusterId);
    CreateCluster.createGameServerCluster(
        PROJECT_ID, REGION_ID, realmId, newClusterId, gkeClusterName);
    DeleteCluster.deleteGameServerCluster(PROJECT_ID, REGION_ID, realmId, newClusterId);
    assertTrue(bout.toString().contains("Game Server Cluster created: " + newClusterName));
    assertTrue(bout.toString().contains("Game Server Cluster deleted: " + newClusterName));
  }

  @Test
  public void getClusterTest() {
    GetCluster.getGameServerCluster(PROJECT_ID, REGION_ID, realmId, clusterId);

    assertTrue(bout.toString().contains("Game Server Cluster found: " + clusterName));
  }

  @Test
  public void listClustersTest() {
    ListClusters.listGameServerClusters(PROJECT_ID, REGION_ID, realmId);

    assertTrue(bout.toString().contains("Game Server Cluster found: " + clusterName));
  }

  @Test
  public void updateClusterTest() {
    UpdateCluster.updateGameServerCluster(PROJECT_ID, REGION_ID, realmId, clusterId);

    assertTrue(bout.toString().contains("Game Server Cluster updated: " + clusterName));
  }
}
