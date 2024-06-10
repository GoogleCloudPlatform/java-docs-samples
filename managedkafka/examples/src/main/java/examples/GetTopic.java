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

// [START managedkafka_get_topic]
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.ManagedKafkaClient;
import com.google.cloud.managedkafka.v1.Topic;
import com.google.cloud.managedkafka.v1.TopicName;
import java.io.IOException;

public class GetTopic {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String clusterId = "my-cluster";
    String topicId = "my-topic";
    getTopic(projectId, region, clusterId, topicId);
  }

  public static void getTopic(String projectId, String region, String clusterId, String topicId)
      throws Exception {
    try (ManagedKafkaClient managedKafkaClient = ManagedKafkaClient.create()) {
      // This operation is being handled synchronously.
      Topic topic =
          managedKafkaClient.getTopic(TopicName.of(projectId, region, clusterId, topicId));
      System.out.println(topic.getAllFields());
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaClient.getTopic got err: %s", e.getMessage());
    }
  }
}

// [END managedkafka_get_topic]
