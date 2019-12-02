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

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.dataproc.v1.Cluster;
import com.google.cloud.dataproc.v1.ClusterConfig;
import com.google.cloud.dataproc.v1.ClusterControllerClient;
import com.google.cloud.dataproc.v1.ClusterControllerSettings;
import com.google.cloud.dataproc.v1.ClusterOperationMetadata;
import com.google.cloud.dataproc.v1.InstanceGroupConfig;
import com.google.protobuf.Empty;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UpdateClusterTest {

  private static final String REGION = "us-central1";
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ENDPOINT = REGION + "-dataproc.googleapis.com:443";
  private static final String CLUSTER_NAME = "test-cluster-" + UUID.randomUUID().toString();
  private static final int NUM_WORKERS = 2;
  private static final int NEW_WORKERS = NUM_WORKERS * 2;

  private ByteArrayOutputStream bout;
  private PrintStream standardOutOrig;

  private static void requireEnv(String varName) {
    assertNotNull(
        System.getenv(varName),
        String.format("Environment variable '%s' is required to perform these tests.", varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnv("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnv("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void setUp() throws IOException, InterruptedException {
    bout = new ByteArrayOutputStream();
    standardOutOrig = System.out;
    System.setOut(new PrintStream(bout));

    ClusterControllerSettings clusterControllerSettings =
        ClusterControllerSettings.newBuilder().setEndpoint(ENDPOINT).build();

    // Configure the settings for our cluster
    InstanceGroupConfig masterConfig =
        InstanceGroupConfig.newBuilder()
            .setMachineTypeUri("n1-standard-1")
            .setNumInstances(1)
            .build();
    InstanceGroupConfig workerConfig =
        InstanceGroupConfig.newBuilder()
            .setMachineTypeUri("n1-standard-1")
            .setNumInstances(NUM_WORKERS)
            .build();
    ClusterConfig clusterConfig =
        ClusterConfig.newBuilder()
            .setMasterConfig(masterConfig)
            .setWorkerConfig(workerConfig)
            .build();
    // Create the cluster object with the desired cluster config
    Cluster cluster =
        Cluster.newBuilder().setClusterName(CLUSTER_NAME).setConfig(clusterConfig).build();

    try (ClusterControllerClient clusterControllerClient =
        ClusterControllerClient.create(clusterControllerSettings)) {
      OperationFuture<Cluster, ClusterOperationMetadata> createClusterAsyncRequest =
          clusterControllerClient.createClusterAsync(PROJECT_ID, REGION, cluster);
      createClusterAsyncRequest.get();
    } catch (ExecutionException e) {
      System.err.println("[UpdateCluster] Error during test cluster creation: \n" + e.getMessage());
    }
  }

  @Test
  public void updateClusterTest() throws IOException, InterruptedException, ExecutionException {
    UpdateCluster.updateCluster(PROJECT_ID, REGION, CLUSTER_NAME, NEW_WORKERS);
    String output = bout.toString();

    assertThat(output, CoreMatchers.containsString(String.format("%d workers", NEW_WORKERS)));
  }

  @After
  public void teardown() throws IOException, InterruptedException, ExecutionException {
    System.setOut(standardOutOrig);

    ClusterControllerSettings clusterControllerSettings =
        ClusterControllerSettings.newBuilder().setEndpoint(ENDPOINT).build();

    try (ClusterControllerClient clusterControllerClient =
        ClusterControllerClient.create(clusterControllerSettings)) {
      OperationFuture<Empty, ClusterOperationMetadata> deleteClusterAsyncRequest =
          clusterControllerClient.deleteClusterAsync(PROJECT_ID, REGION, CLUSTER_NAME);
      deleteClusterAsyncRequest.get();
    }
  }
}
