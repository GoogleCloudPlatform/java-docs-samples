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

// [START managedkafka_delete_connect_cluster]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.longrunning.OperationTimedPollAlgorithm;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.retrying.TimedRetryAlgorithm;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.ConnectClusterName;
import com.google.cloud.managedkafka.v1.DeleteConnectClusterRequest;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectClient;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectSettings;
import com.google.cloud.managedkafka.v1.OperationMetadata;
import com.google.protobuf.Empty;
import java.io.IOException;
import java.time.Duration;

public class DeleteConnectCluster {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String clusterId = "my-connect-cluster";
    deleteConnectCluster(projectId, region, clusterId);
  }

  public static void deleteConnectCluster(String projectId, String region, String clusterId)
      throws Exception {

    // Create the settings to configure the timeout for polling operations
    ManagedKafkaConnectSettings.Builder settingsBuilder = ManagedKafkaConnectSettings.newBuilder();
    TimedRetryAlgorithm timedRetryAlgorithm = OperationTimedPollAlgorithm.create(
        RetrySettings.newBuilder()
            .setTotalTimeoutDuration(Duration.ofHours(1L))
            .build());
    settingsBuilder.deleteConnectClusterOperationSettings()
        .setPollingAlgorithm(timedRetryAlgorithm);

    try (ManagedKafkaConnectClient managedKafkaConnectClient = ManagedKafkaConnectClient.create(
        settingsBuilder.build())) {
      DeleteConnectClusterRequest request = DeleteConnectClusterRequest.newBuilder()
          .setName(ConnectClusterName.of(projectId, region, clusterId).toString())
          .build();
      OperationFuture<Empty, OperationMetadata> future = managedKafkaConnectClient
          .deleteConnectClusterOperationCallable().futureCall(request);

      // Get the initial LRO and print details. CreateConnectCluster contains sample
      // code for polling logs.
      OperationSnapshot operation = future.getInitialFuture().get();
      System.out.printf(
          "Connect cluster deletion started. Operation name: %s\nDone: %s\nMetadata: %s\n",
          operation.getName(),
          operation.isDone(),
          future.getMetadata().get().toString());

      future.get();
      System.out.println("Deleted connect cluster");
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaConnectClient.deleteConnectCluster got err: %s", 
          e.getMessage());
    }
  }
}

// [END managedkafka_delete_connect_cluster]
