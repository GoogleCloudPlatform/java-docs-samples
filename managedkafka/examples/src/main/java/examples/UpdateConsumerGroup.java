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

// [START managedkafka_update_consumergroup]
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.ConsumerGroup;
import com.google.cloud.managedkafka.v1.ConsumerGroupName;
import com.google.cloud.managedkafka.v1.ConsumerPartitionMetadata;
import com.google.cloud.managedkafka.v1.ConsumerTopicMetadata;
import com.google.cloud.managedkafka.v1.ManagedKafkaClient;
import com.google.cloud.managedkafka.v1.TopicName;
import com.google.cloud.managedkafka.v1.UpdateConsumerGroupRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateConsumerGroup {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String clusterId = "my-cluster";
    String topicId = "my-topic";
    String consumerGroupId = "my-consumer-group";
    Map<Integer, Integer> partitionOffsets =
        new HashMap<Integer, Integer>() {
          {
            put(1, 10);
            put(2, 20);
            put(3, 30);
          }
        };
    updateConsumerGroup(projectId, region, clusterId, topicId, consumerGroupId, partitionOffsets);
  }

  public static void updateConsumerGroup(
      String projectId,
      String region,
      String clusterId,
      String topicId,
      String consumerGroupId,
      Map<Integer, Integer> partitionOffsets)
      throws Exception {
    TopicName topicName = TopicName.of(projectId, region, clusterId, topicId);
    ConsumerGroupName consumerGroupName =
        ConsumerGroupName.of(projectId, region, clusterId, consumerGroupId);

    Map<Integer, ConsumerPartitionMetadata> partitions =
        new HashMap<Integer, ConsumerPartitionMetadata>() {
          {
            for (Entry<Integer, Integer> partitionOffset : partitionOffsets.entrySet()) {
              ConsumerPartitionMetadata partitionMetadata =
                  ConsumerPartitionMetadata.newBuilder()
                      .setOffset(partitionOffset.getValue())
                      .build();
              put(partitionOffset.getKey(), partitionMetadata);
            }
          }
        };
    ConsumerTopicMetadata topicMetadata =
        ConsumerTopicMetadata.newBuilder().putAllPartitions(partitions).build();
    ConsumerGroup consumerGroup =
        ConsumerGroup.newBuilder()
            .setName(consumerGroupName.toString())
            .putTopics(topicName.toString(), topicMetadata)
            .build();
    FieldMask updateMask = FieldMask.newBuilder().addPaths("topics").build();

    try (ManagedKafkaClient managedKafkaClient = ManagedKafkaClient.create()) {
      UpdateConsumerGroupRequest request =
          UpdateConsumerGroupRequest.newBuilder()
              .setUpdateMask(updateMask)
              .setConsumerGroup(consumerGroup)
              .build();
      // This operation is being handled synchronously.
      ConsumerGroup response = managedKafkaClient.updateConsumerGroup(request);
      System.out.printf("Updated consumer group: %s\n", response.getName());
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaClient.updateConsumerGroup got err: %s", e.getMessage());
    }
  }
}

// [END managedkafka_update_consumergroup]
