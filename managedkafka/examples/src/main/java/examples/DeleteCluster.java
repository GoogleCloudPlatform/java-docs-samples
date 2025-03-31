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

// [START managedkafka_delete_cluster]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.longrunning.OperationTimedPollAlgorithm;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.retrying.TimedRetryAlgorithm;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.ClusterName;
import com.google.cloud.managedkafka.v1.DeleteClusterRequest;
import com.google.cloud.managedkafka.v1.ManagedKafkaClient;
import com.google.cloud.managedkafka.v1.ManagedKafkaSettings;
import com.google.cloud.managedkafka.v1.OperationMetadata;
import com.google.protobuf.Empty;
import java.io.IOException;
import java.time.Duration;

public class DeleteCluster {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String clusterId = "my-cluster";
    deleteCluster(projectId, region, clusterId);
  }

  public static void deleteCluster(String projectId, String region, String clusterId)
      throws Exception {

    // Create the settings to configure the timeout for polling operations
    ManagedKafkaSettings.Builder settingsBuilder = ManagedKafkaSettings.newBuilder();
    TimedRetryAlgorithm timedRetryAlgorithm = OperationTimedPollAlgorithm.create(
        RetrySettings.newBuilder()
            .setTotalTimeoutDuration(Duration.ofHours(1L))
            .build());
    settingsBuilder.deleteClusterOperationSettings()
        .setPollingAlgorithm(timedRetryAlgorithm);

    try (ManagedKafkaClient managedKafkaClient = ManagedKafkaClient.create(
        settingsBuilder.build())) {
      DeleteClusterRequest request =
          DeleteClusterRequest.newBuilder()
              .setName(ClusterName.of(projectId, region, clusterId).toString())
              .build();
      OperationFuture<Empty, OperationMetadata> future =
          managedKafkaClient.deleteClusterOperationCallable().futureCall(request);

      // Get the initial LRO and print details. CreateCluster contains sample code for polling logs.
      OperationSnapshot operation = future.getInitialFuture().get();
      System.out.printf("Cluster deletion started. Operation name: %s\nDone: %s\nMetadata: %s\n",
          operation.getName(),
          operation.isDone(),
          future.getMetadata().get().toString());

      future.get();
      System.out.println("Deleted cluster");
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaClient.deleteCluster got err: %s", e.getMessage());
    }
  }
}

// [END managedkafka_delete_cluster]
