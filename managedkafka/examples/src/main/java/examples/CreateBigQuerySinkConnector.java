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

// [START managedkafka_create_bigquery_sink_connector]

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.ConnectClusterName;
import com.google.cloud.managedkafka.v1.Connector;
import com.google.cloud.managedkafka.v1.ConnectorName;
import com.google.cloud.managedkafka.v1.CreateConnectorRequest;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateBigQuerySinkConnector {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String connectClusterId = "my-connect-cluster";
    String connectorId = "my-bigquery-sink-connector";
    String bigqueryProjectId = "my-bigquery-project-id";
    String datasetName = "my_dataset";
    String tableName = "kafka_sink_table";
    String kafkaTopicName = "kafka-topic";
    String maxTasks = "3";
    String connectorClass = "com.wepay.kafka.connect.bigquery.BigQuerySinkConnector";
    String keyConverter = "org.apache.kafka.connect.storage.StringConverter";
    String valueConverter = "org.apache.kafka.connect.json.JsonConverter";
    String valueSchemasEnable = "false";
    createBigQuerySinkConnector(
        projectId,
        region,
        connectClusterId,
        connectorId,
        bigqueryProjectId,
        datasetName,
        tableName,
        kafkaTopicName,
        maxTasks,
        connectorClass,
        keyConverter,
        valueConverter,
        valueSchemasEnable);
  }

  public static void createBigQuerySinkConnector(
      String projectId,
      String region,
      String connectClusterId,
      String connectorId,
      String bigqueryProjectId,
      String datasetName,
      String tableName,
      String kafkaTopicName,
      String maxTasks,
      String connectorClass,
      String keyConverter,
      String valueConverter,
      String valueSchemasEnable)
      throws Exception {

    // Build the connector configuration
    Map<String, String> configMap = new HashMap<>();
    configMap.put("name", connectorId);
    configMap.put("project", bigqueryProjectId);
    configMap.put("topics", kafkaTopicName);
    configMap.put("tasks.max", maxTasks);
    configMap.put("connector.class", connectorClass);
    configMap.put("key.converter", keyConverter);
    configMap.put("value.converter", valueConverter);
    configMap.put("value.converter.schemas.enable", valueSchemasEnable);
    configMap.put("defaultDataset", datasetName);

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
      System.out.printf("Created BigQuery Sink connector: %s\n", response.getName());
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaConnectClient.createConnector got err: %s", e.getMessage());
    }
  }
}

// [END managedkafka_create_bigquery_sink_connector]
