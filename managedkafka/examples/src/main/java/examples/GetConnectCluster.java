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

// [START managedkafka_get_connect_cluster]
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.ConnectCluster;
import com.google.cloud.managedkafka.v1.ConnectClusterName;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectClient;
import java.io.IOException;

public class GetConnectCluster {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String clusterId = "my-connect-cluster";
    getConnectCluster(projectId, region, clusterId);
  }

  public static void getConnectCluster(String projectId, String region, String clusterId)
      throws Exception {
    try (ManagedKafkaConnectClient managedKafkaConnectClient = ManagedKafkaConnectClient.create()) {
      // This operation is being handled synchronously.
      ConnectCluster connectCluster = managedKafkaConnectClient
          .getConnectCluster(ConnectClusterName.of(projectId, region, clusterId));
      System.out.println(connectCluster.getAllFields());
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaConnectClient.getConnectCluster got err: %s", e.getMessage());
    }
  }
}

// [END managedkafka_get_connect_cluster]
