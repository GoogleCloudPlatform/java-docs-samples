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

// [START managedkafka_create_mirrormaker2_connector]

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.ConnectClusterName;
import com.google.cloud.managedkafka.v1.Connector;
import com.google.cloud.managedkafka.v1.ConnectorName;
import com.google.cloud.managedkafka.v1.CreateConnectorRequest;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateMirrorMaker2SourceConnector {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String maxTasks = "3";
    String connectClusterId = "my-connect-cluster";
    String connectorId = "my-mirrormaker2-connector";
    String sourceClusterBootstrapServers = "my-source-cluster:9092";
    String targetClusterBootstrapServers = "my-target-cluster:9092";
    String sourceClusterAlias = "source";
    String targetClusterAlias = "target"; // This is usually the primary cluster.
    String connectorClass = "org.apache.kafka.connect.mirror.MirrorSourceConnector";
    String topics = ".*";
    // You can define an exclusion policy for topics as follows:
    // To exclude internal MirrorMaker 2 topics, internal topics and replicated topics.
    String topicsExclude = "mm2.*.internal,.*.replica,__.*";
    createMirrorMaker2SourceConnector(
        projectId,
        region,
        maxTasks,
        connectClusterId,
        connectorId,
        sourceClusterBootstrapServers,
        targetClusterBootstrapServers,
        sourceClusterAlias,
        targetClusterAlias,
        connectorClass,
        topics,
        topicsExclude);
  }

  public static void createMirrorMaker2SourceConnector(
      String projectId,
      String region,
      String maxTasks,
      String connectClusterId,
      String connectorId,
      String sourceClusterBootstrapServers,
      String targetClusterBootstrapServers,
      String sourceClusterAlias,
      String targetClusterAlias,
      String connectorClass,
      String topics,
      String topicsExclude)
      throws Exception {

    // Build the connector configuration
    Map<String, String> configMap = new HashMap<>();
    configMap.put("tasks.max", maxTasks);
    configMap.put("connector.class", connectorClass);
    configMap.put("name", connectorId);
    configMap.put("source.cluster.alias", sourceClusterAlias);
    configMap.put("target.cluster.alias", targetClusterAlias);
    configMap.put("topics", topics);
    configMap.put("topics.exclude", topicsExclude);
    configMap.put("source.cluster.bootstrap.servers", sourceClusterBootstrapServers);
    configMap.put("target.cluster.bootstrap.servers", targetClusterBootstrapServers);

    Connector connector = Connector.newBuilder()
        .setName(
            ConnectorName.of(projectId, region, connectClusterId, connectorId).toString())
        .putAllConfigs(configMap)
        .build();

    try (ManagedKafkaConnectClient managedKafkaConnectClient = ManagedKafkaConnectClient.create()) {
      CreateConnectorRequest request = CreateConnectorRequest.newBuilder()
          .setParent(ConnectClusterName.of(projectId, region, connectClusterId).toString())
          .setConnectorId(connectorId)
          .setConnector(connector)
          .build();

      // This operation is being handled synchronously.
      Connector response = managedKafkaConnectClient.createConnector(request);
      System.out.printf("Created MirrorMaker2 Source connector: %s\n", response.getName());
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaConnectClient.createConnector got err: %s\n", e.getMessage());
    }
  }
}

// [END managedkafka_create_mirrormaker2_connector]