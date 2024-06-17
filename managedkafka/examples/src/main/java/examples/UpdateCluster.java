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

// [START managedkafka_update_cluster]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.managedkafka.v1.CapacityConfig;
import com.google.cloud.managedkafka.v1.Cluster;
import com.google.cloud.managedkafka.v1.ClusterName;
import com.google.cloud.managedkafka.v1.ManagedKafkaClient;
import com.google.cloud.managedkafka.v1.OperationMetadata;
import com.google.cloud.managedkafka.v1.UpdateClusterRequest;
import com.google.protobuf.FieldMask;
import java.util.concurrent.ExecutionException;

public class UpdateCluster {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String clusterId = "my-cluster";
    long memoryBytes = 4221225472L; // 4 GiB
    updateCluster(projectId, region, clusterId, memoryBytes);
  }

  public static void updateCluster(
      String projectId, String region, String clusterId, long memoryBytes) throws Exception {
    CapacityConfig capacityConfig = CapacityConfig.newBuilder().setMemoryBytes(memoryBytes).build();
    Cluster cluster =
        Cluster.newBuilder()
            .setName(ClusterName.of(projectId, region, clusterId).toString())
            .setCapacityConfig(capacityConfig)
            .build();
    FieldMask updateMask = FieldMask.newBuilder().addPaths("capacity_config.memory_bytes").build();

    try (ManagedKafkaClient managedKafkaClient = ManagedKafkaClient.create()) {
      UpdateClusterRequest request =
          UpdateClusterRequest.newBuilder().setUpdateMask(updateMask).setCluster(cluster).build();
      OperationFuture<Cluster, OperationMetadata> future =
          managedKafkaClient.updateClusterOperationCallable().futureCall(request);
      Cluster response = future.get();
      System.out.printf("Updated cluster: %s\n", response.getName());
    } catch (ExecutionException e) {
      System.err.printf("managedKafkaClient.updateCluster got err: %s", e.getMessage());
    }
  }
}

// [END managedkafka_update_cluster]
