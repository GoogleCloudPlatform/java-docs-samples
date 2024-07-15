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

// [START managedkafka_list_clusters]
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.Cluster;
import com.google.cloud.managedkafka.v1.LocationName;
import com.google.cloud.managedkafka.v1.ManagedKafkaClient;
import java.io.IOException;

public class ListClusters {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    listClusters(projectId, region);
  }

  public static void listClusters(String projectId, String region) throws Exception {
    try (ManagedKafkaClient managedKafkaClient = ManagedKafkaClient.create()) {
      LocationName locationName = LocationName.of(projectId, region);
      // This operation is being handled synchronously.
      for (Cluster cluster : managedKafkaClient.listClusters(locationName).iterateAll()) {
        System.out.println(cluster.getAllFields());
      }
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaClient.listClusters got err: %s", e.getMessage());
    }
  }
}

// [END managedkafka_list_clusters]
