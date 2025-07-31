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

// [START managedkafka_update_connect_cluster]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.longrunning.OperationTimedPollAlgorithm;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.retrying.TimedRetryAlgorithm;
import com.google.cloud.managedkafka.v1.CapacityConfig;
import com.google.cloud.managedkafka.v1.ConnectCluster;
import com.google.cloud.managedkafka.v1.ConnectClusterName;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectClient;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectSettings;
import com.google.cloud.managedkafka.v1.OperationMetadata;
import com.google.cloud.managedkafka.v1.UpdateConnectClusterRequest;
import com.google.protobuf.FieldMask;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

public class UpdateConnectCluster {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String clusterId = "my-connect-cluster";
    long memoryBytes = 12884901888L; // 12 GiB
    updateConnectCluster(projectId, region, clusterId, memoryBytes);
  }

  public static void updateConnectCluster(
      String projectId, String region, String clusterId, long memoryBytes) throws Exception {
    CapacityConfig capacityConfig = CapacityConfig.newBuilder().setMemoryBytes(memoryBytes).build();
    ConnectCluster connectCluster = ConnectCluster.newBuilder()
        .setName(ConnectClusterName.of(projectId, region, clusterId).toString())
        .setCapacityConfig(capacityConfig)
        .build();
    FieldMask updateMask = FieldMask.newBuilder().addPaths("capacity_config.memory_bytes").build();

    // Create the settings to configure the timeout for polling operations
    ManagedKafkaConnectSettings.Builder settingsBuilder = ManagedKafkaConnectSettings.newBuilder();
    TimedRetryAlgorithm timedRetryAlgorithm = OperationTimedPollAlgorithm.create(
        RetrySettings.newBuilder()
            .setTotalTimeoutDuration(Duration.ofHours(1L))
            .build());
    settingsBuilder.updateConnectClusterOperationSettings()
        .setPollingAlgorithm(timedRetryAlgorithm);

    try (ManagedKafkaConnectClient managedKafkaConnectClient = ManagedKafkaConnectClient.create(
        settingsBuilder.build())) {
      UpdateConnectClusterRequest request = UpdateConnectClusterRequest.newBuilder().setUpdateMask(updateMask)
          .setConnectCluster(connectCluster).build();
      OperationFuture<ConnectCluster, OperationMetadata> future = managedKafkaConnectClient
          .updateConnectClusterOperationCallable().futureCall(request);

      // Get the initial LRO and print details. CreateConnectCluster contains sample
      // code for polling logs.
      OperationSnapshot operation = future.getInitialFuture().get();
      System.out.printf("Connect cluster update started. Operation name: %s\nDone: %s\nMetadata: %s\n",
          operation.getName(),
          operation.isDone(),
          future.getMetadata().get().toString());

      ConnectCluster response = future.get();
      System.out.printf("Updated connect cluster: %s\n", response.getName());
    } catch (ExecutionException e) {
      System.err.printf("managedKafkaConnectClient.updateConnectCluster got err: %s", e.getMessage());
    }
  }
}

// [END managedkafka_update_connect_cluster]
