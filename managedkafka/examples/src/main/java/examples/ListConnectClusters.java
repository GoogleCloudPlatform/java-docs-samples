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

// [START managedkafka_list_connect_clusters]
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.ConnectCluster;
import com.google.cloud.managedkafka.v1.LocationName;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectClient;
import java.io.IOException;

public class ListConnectClusters {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    listConnectClusters(projectId, region);
  }

  public static void listConnectClusters(String projectId, String region) throws Exception {
    try (ManagedKafkaConnectClient managedKafkaConnectClient = ManagedKafkaConnectClient.create()) {
      LocationName locationName = LocationName.of(projectId, region);
      // This operation is being handled synchronously.
      for (ConnectCluster connectCluster : managedKafkaConnectClient.listConnectClusters(locationName).iterateAll()) {
        System.out.println(connectCluster.getAllFields());
      }
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaConnectClient.listConnectClusters got err: %s", e.getMessage());
    }
  }
}

// [END managedkafka_list_connect_clusters]
