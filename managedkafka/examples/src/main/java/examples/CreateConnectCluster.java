/*
 * Copyright 2025 Google LLC
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

// [START managedkafka_create_connect_cluster]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.longrunning.OperationTimedPollAlgorithm;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.api.gax.retrying.TimedRetryAlgorithm;
import com.google.cloud.managedkafka.v1.CapacityConfig;
import com.google.cloud.managedkafka.v1.ConnectAccessConfig;
import com.google.cloud.managedkafka.v1.ConnectCluster;
import com.google.cloud.managedkafka.v1.ConnectGcpConfig;
import com.google.cloud.managedkafka.v1.ConnectNetworkConfig;
import com.google.cloud.managedkafka.v1.CreateConnectClusterRequest;
import com.google.cloud.managedkafka.v1.LocationName;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectClient;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectSettings;
import com.google.cloud.managedkafka.v1.OperationMetadata;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

public class CreateConnectCluster {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String clusterId = "my-connect-cluster";
    String subnet = "my-subnet"; // e.g. projects/my-project/regions/my-region/subnetworks/my-subnet
    String kafkaCluster = "my-kafka-cluster"; // The Kafka cluster to connect to
    int cpu = 12;
    long memoryBytes = 12884901888L; // 12 GiB
    createConnectCluster(projectId, region, clusterId, subnet, kafkaCluster, cpu, memoryBytes);
  }

  public static void createConnectCluster(
      String projectId,
      String region,
      String clusterId,
      String subnet,
      String kafkaCluster,
      int cpu,
      long memoryBytes)
      throws Exception {
    CapacityConfig capacityConfig = CapacityConfig.newBuilder().setVcpuCount(cpu)
        .setMemoryBytes(memoryBytes).build();
    ConnectNetworkConfig networkConfig = ConnectNetworkConfig.newBuilder()
        .setPrimarySubnet(subnet)
        .build();
    // Optionally, you can also specify additional accessible subnets and resolvable
    // DNS domains as part of your network configuration. For example:
    // .addAllAdditionalSubnets(List.of("subnet-1", "subnet-2"))
    // .addAllDnsDomainNames(List.of("dns-1", "dns-2"))
    ConnectGcpConfig gcpConfig = ConnectGcpConfig.newBuilder()
        .setAccessConfig(ConnectAccessConfig.newBuilder().addNetworkConfigs(networkConfig).build())
        .build();
    ConnectCluster connectCluster = ConnectCluster.newBuilder()
        .setCapacityConfig(capacityConfig)
        .setGcpConfig(gcpConfig)
        .setKafkaCluster(kafkaCluster)
        .build();

    // Create the settings to configure the timeout for polling operations
    ManagedKafkaConnectSettings.Builder settingsBuilder = ManagedKafkaConnectSettings.newBuilder();
    TimedRetryAlgorithm timedRetryAlgorithm = OperationTimedPollAlgorithm.create(
        RetrySettings.newBuilder()
            .setTotalTimeoutDuration(Duration.ofHours(1L))
            .build());
    settingsBuilder.createConnectClusterOperationSettings()
        .setPollingAlgorithm(timedRetryAlgorithm);

    try (ManagedKafkaConnectClient managedKafkaConnectClient = ManagedKafkaConnectClient
        .create(settingsBuilder.build())) {
      CreateConnectClusterRequest request = CreateConnectClusterRequest.newBuilder()
          .setParent(LocationName.of(projectId, region).toString())
          .setConnectClusterId(clusterId)
          .setConnectCluster(connectCluster)
          .build();

      // The duration of this operation can vary considerably, typically taking
      // between 10-30 minutes.
      OperationFuture<ConnectCluster, OperationMetadata> future = managedKafkaConnectClient
          .createConnectClusterOperationCallable().futureCall(request);

      // Get the initial LRO and print details.
      OperationSnapshot operation = future.getInitialFuture().get();
      System.out.printf(
          "Connect cluster creation started. Operation name: %s\nDone: %s\nMetadata: %s\n",
          operation.getName(), operation.isDone(), future.getMetadata().get().toString());

      while (!future.isDone()) {
        // The pollingFuture gives us the most recent status of the operation
        RetryingFuture<OperationSnapshot> pollingFuture = future.getPollingFuture();
        OperationSnapshot currentOp = pollingFuture.getAttemptResult().get();
        System.out.printf("Polling Operation:\nName: %s\n Done: %s\n",
            currentOp.getName(),
            currentOp.isDone());
      }

      // NOTE: future.get() blocks completion until the operation is complete (isDone
      // = True)
      ConnectCluster response = future.get();
      System.out.printf("Created connect cluster: %s\n", response.getName());
    } catch (ExecutionException e) {
      System.err.printf("managedKafkaConnectClient.createConnectCluster got err: %s\n", 
          e.getMessage());
      throw e;
    }
  }
}
// [END managedkafka_create_connect_cluster]
