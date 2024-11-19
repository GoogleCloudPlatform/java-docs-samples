/*
 * Copyright 2024 Google LLC
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

package examples;

// [START managedkafka_create_cluster]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.longrunning.OperationTimedPollAlgorithm;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.api.gax.retrying.TimedRetryAlgorithm;
import com.google.cloud.managedkafka.v1.AccessConfig;
import com.google.cloud.managedkafka.v1.CapacityConfig;
import com.google.cloud.managedkafka.v1.Cluster;
import com.google.cloud.managedkafka.v1.CreateClusterRequest;
import com.google.cloud.managedkafka.v1.GcpConfig;
import com.google.cloud.managedkafka.v1.LocationName;
import com.google.cloud.managedkafka.v1.ManagedKafkaClient;
import com.google.cloud.managedkafka.v1.ManagedKafkaSettings;
import com.google.cloud.managedkafka.v1.NetworkConfig;
import com.google.cloud.managedkafka.v1.OperationMetadata;
import com.google.cloud.managedkafka.v1.RebalanceConfig;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

public class CreateCluster {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String clusterId = "my-cluster";
    String subnet = "my-subnet"; // e.g. projects/my-project/regions/my-region/subnetworks/my-subnet
    int cpu = 3;
    long memoryBytes = 3221225472L; // 3 GiB
    createCluster(projectId, region, clusterId, subnet, cpu, memoryBytes);
  }

  public static void createCluster(
      String projectId, String region, String clusterId, String subnet, int cpu, long memoryBytes)
      throws Exception {
    CapacityConfig capacityConfig =
        CapacityConfig.newBuilder().setVcpuCount(cpu).setMemoryBytes(memoryBytes).build();
    NetworkConfig networkConfig = NetworkConfig.newBuilder().setSubnet(subnet).build();
    GcpConfig gcpConfig =
        GcpConfig.newBuilder()
            .setAccessConfig(AccessConfig.newBuilder().addNetworkConfigs(networkConfig).build())
            .build();
    RebalanceConfig rebalanceConfig =
        RebalanceConfig.newBuilder()
            .setMode(RebalanceConfig.Mode.AUTO_REBALANCE_ON_SCALE_UP)
            .build();
    Cluster cluster =
        Cluster.newBuilder()
            .setCapacityConfig(capacityConfig)
            .setGcpConfig(gcpConfig)
            .setRebalanceConfig(rebalanceConfig)
            .build();

    // Create the settings to configure the timeout for polling operations
    ManagedKafkaSettings.Builder settingsBuilder = ManagedKafkaSettings.newBuilder();
    TimedRetryAlgorithm timedRetryAlgorithm = OperationTimedPollAlgorithm.create(
        RetrySettings.newBuilder()
            .setInitialRetryDelayDuration(Duration.ofSeconds(5L))
            .setRetryDelayMultiplier(1.5)
            .setMaxRetryDelayDuration(Duration.ofSeconds(30L))
            .setInitialRpcTimeoutDuration(Duration.ZERO) // ignored for LROs
            .setRpcTimeoutMultiplier(1.0) // ignored for LROs
            .setMaxRpcTimeoutDuration(Duration.ZERO) // ignored for LROs
            .setTotalTimeoutDuration(Duration.ofHours(1L)) // set polling timeout to 1 hour
            .build());
    settingsBuilder.createClusterOperationSettings()
        .setPollingAlgorithm(timedRetryAlgorithm);

    try (ManagedKafkaClient managedKafkaClient = ManagedKafkaClient.create(
        settingsBuilder.build())) {

      CreateClusterRequest request =
          CreateClusterRequest.newBuilder()
              .setParent(LocationName.of(projectId, region).toString())
              .setClusterId(clusterId)
              .setCluster(cluster)
              .build();

      // The duration of this operation can vary considerably, typically taking between 10-40
      // minutes.
      OperationFuture<Cluster, OperationMetadata> future =
          managedKafkaClient.createClusterOperationCallable().futureCall(request);

      // Get the initial LRO and print details.
      OperationSnapshot operation = future.getInitialFuture().get();
      System.out.printf("Cluster creation started. Operation name: %s\nDone: %s\nMetadata: %s\n",
          operation.getName(),
          operation.isDone(),
          future.getMetadata().get().toString());

      while (!future.isDone()) {
        // The pollingFuture gives us the most recent status of the operation
        RetryingFuture<OperationSnapshot> pollingFuture = future.getPollingFuture();
        OperationSnapshot currentOp = pollingFuture.getAttemptResult().get();
        System.out.printf("Polling Operation:\nName: %s\n Done: %s\n",
            currentOp.getName(),
            currentOp.isDone());
      }

      // NOTE: future.get() blocks completion until the operation is complete (isDone =  True)
      Cluster response = future.get();
      System.out.printf("Created cluster: %s\n", response.getName());
    } catch (ExecutionException e) {
      System.err.printf("managedKafkaClient.createCluster got err: %s", e.getMessage());
    }
  }
}

// [END managedkafka_create_cluster]
