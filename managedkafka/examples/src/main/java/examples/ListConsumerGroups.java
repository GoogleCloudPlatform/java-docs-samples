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

// [START managedkafka_list_consumergroups]
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.ClusterName;
import com.google.cloud.managedkafka.v1.ConsumerGroup;
import com.google.cloud.managedkafka.v1.ManagedKafkaClient;
import java.io.IOException;

public class ListConsumerGroups {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "us-central1";
    String clusterId = "my-cluster";
    listConsumerGroups(projectId, region, clusterId);
  }

  public static void listConsumerGroups(String projectId, String region, String clusterId)
      throws Exception {
    try (ManagedKafkaClient managedKafkaClient = ManagedKafkaClient.create()) {
      ClusterName clusterName = ClusterName.of(projectId, region, clusterId);
      for (ConsumerGroup consumerGroup :
          managedKafkaClient.listConsumerGroups(clusterName).iterateAll()) {
        System.out.println(consumerGroup.getAllFields());
      }
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaClient.listConsumerGroups got err: %s", e.getMessage());
    }
  }
}

// [START managedkafka_list_consumergroups]