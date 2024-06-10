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

// [START managedkafka_create_topic]
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.ClusterName;
import com.google.cloud.managedkafka.v1.CreateTopicRequest;
import com.google.cloud.managedkafka.v1.ManagedKafkaClient;
import com.google.cloud.managedkafka.v1.Topic;
import com.google.cloud.managedkafka.v1.TopicName;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateTopic {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String clusterId = "my-cluster";
    String topicId = "my-topic";
    int partitionCount = 100;
    int replicationFactor = 3;
    Map<String, String> configs =
        new HashMap<String, String>() {
          {
            put("min.insync.replicas", "2");
          }
        };
    createTopic(projectId, region, clusterId, topicId, partitionCount, replicationFactor, configs);
  }

  public static void createTopic(
      String projectId,
      String region,
      String clusterId,
      String topicId,
      int partitionCount,
      int replicationFactor,
      Map<String, String> configs)
      throws Exception {
    Topic topic =
        Topic.newBuilder()
            .setName(TopicName.of(projectId, region, clusterId, topicId).toString())
            .setPartitionCount(partitionCount)
            .setReplicationFactor(replicationFactor)
            .putAllConfigs(configs)
            .build();
    try (ManagedKafkaClient managedKafkaClient = ManagedKafkaClient.create()) {
      CreateTopicRequest request =
          CreateTopicRequest.newBuilder()
              .setParent(ClusterName.of(projectId, region, clusterId).toString())
              .setTopicId(topicId)
              .setTopic(topic)
              .build();
      // This operation is being handled synchronously.
      Topic response = managedKafkaClient.createTopic(request);
      System.out.printf("Created topic: %s\n", response.getName());
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaClient.createTopic got err: %s", e.getMessage());
    }
  }
}

// [END managedkafka_create_topic]
