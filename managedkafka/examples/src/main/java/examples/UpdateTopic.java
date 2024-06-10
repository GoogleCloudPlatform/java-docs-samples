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

// [START managedkafka_update_topic]
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.ManagedKafkaClient;
import com.google.cloud.managedkafka.v1.Topic;
import com.google.cloud.managedkafka.v1.TopicName;
import com.google.cloud.managedkafka.v1.UpdateTopicRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UpdateTopic {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String clusterId = "my-cluster";
    String topicId = "my-topic";
    int partitionCount = 200;
    Map<String, String> configs =
        new HashMap<String, String>() {
          {
            put("min.insync.replicas", "1");
          }
        };
    updateTopic(projectId, region, clusterId, topicId, partitionCount, configs);
  }

  public static void updateTopic(
      String projectId,
      String region,
      String clusterId,
      String topicId,
      int partitionCount,
      Map<String, String> configs)
      throws Exception {
    Topic topic =
        Topic.newBuilder()
            .setName(TopicName.of(projectId, region, clusterId, topicId).toString())
            .setPartitionCount(partitionCount)
            .putAllConfigs(configs)
            .build();
    String[] paths = {"partition_count", "configs"};
    FieldMask updateMask = FieldMask.newBuilder().addAllPaths(Arrays.asList(paths)).build();
    try (ManagedKafkaClient managedKafkaClient = ManagedKafkaClient.create()) {
      UpdateTopicRequest request =
          UpdateTopicRequest.newBuilder().setUpdateMask(updateMask).setTopic(topic).build();
      // This operation is being handled synchronously.
      Topic response = managedKafkaClient.updateTopic(request);
      System.out.printf("Updated topic: %s\n", response.getName());
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaClient.updateCluster got err: %s", e.getMessage());
    }
  }
}

// [END managedkafka_update_topic]
