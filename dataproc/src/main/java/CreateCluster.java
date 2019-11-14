/*
 * Copyright 2019 Google Inc.
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

// [START create_cluster]
import com.google.cloud.dataproc.v1.Cluster;
import com.google.cloud.dataproc.v1.ClusterConfig;
import com.google.cloud.dataproc.v1.ClusterControllerClient;
import com.google.cloud.dataproc.v1.ClusterControllerSettings;
import com.google.cloud.dataproc.v1.InstanceGroupConfig;
import java.io.IOException;

public class CreateCluster {

  public void createCluster(String projectId, String region, String clusterName)
      throws IOException {

    String myEndpoint = String.format("%s-dataproc.googleapis.com:443", region);

    // Configure the settings for the cluster controller client
    ClusterControllerSettings clusterControllerSettings =
        ClusterControllerSettings.newBuilder().setEndpoint(myEndpoint).build();

    // Create a cluster controller client with the configured settings
    try (ClusterControllerClient clusterControllerClient = ClusterControllerClient.create(clusterControllerSettings)) {

      // Configure the settings for our cluster
      InstanceGroupConfig masterConfig = InstanceGroupConfig.newBuilder().setMachineTypeUri("n1-standard-1")
          .setNumInstances(1).build();

      InstanceGroupConfig workerConfig = InstanceGroupConfig.newBuilder().setMachineTypeUri("n1-standard-1")
          .setNumInstances(2).build();

      ClusterConfig clusterConfig = ClusterConfig.newBuilder().setMasterConfig(masterConfig)
          .setWorkerConfig(workerConfig).build();

      // Create the cluster object with the desired cluster config
      Cluster cluster = Cluster.newBuilder().setClusterName(clusterName).setConfig(clusterConfig).build();

      // Create the cluster
      Cluster response = clusterControllerClient.createClusterAsync(projectId, region, cluster).get();

      // Print out the response
      System.out.println(response);

    } catch (Exception e) {
      System.out.println("Error during cluster creation connection: \n" + e.toString());
    }
  }
}
// [END create_cluster]