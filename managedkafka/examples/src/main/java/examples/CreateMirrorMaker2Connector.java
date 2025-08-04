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

public class CreateMirrorMaker2Connector {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String connectClusterId = "my-connect-cluster";
    String connectorId = "my-mirrormaker2-connector";
    String sourceClusterBootstrapServers = "my-source-cluster:9092";
    String targetClusterBootstrapServers = "my-target-cluster:9092";
    String sourceClusterAlias = "source";
    String targetClusterAlias = "target";
    String connectorClass = "org.apache.kafka.connect.mirror.MirrorSourceConnector";
    String topics = ".*";
    String offsetSyncsReplicationFactor = "1";
    String sourceSecurityProtocol = "SASL_SSL";
    String sourceSaslMechanism = "OAUTHBEARER";
    String sourceLoginCallbackHandler = 
        "com.google.cloud.hosted.kafka.auth.GcpLoginCallbackHandler";
    String sourceJaasConfig = 
        "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;";
    String targetSecurityProtocol = "SASL_SSL";
    String targetSaslMechanism = "OAUTHBEARER";
    String targetLoginCallbackHandler = 
        "com.google.cloud.hosted.kafka.auth.GcpLoginCallbackHandler";
    String targetJaasConfig = 
        "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;";
    createMirrorMaker2Connector(
        projectId,
        region,
        connectClusterId,
        connectorId,
        sourceClusterBootstrapServers,
        targetClusterBootstrapServers,
        sourceClusterAlias,
        targetClusterAlias,
        connectorClass,
        topics,
        offsetSyncsReplicationFactor,
        sourceSecurityProtocol,
        sourceSaslMechanism,
        sourceLoginCallbackHandler,
        sourceJaasConfig,
        targetSecurityProtocol,
        targetSaslMechanism,
        targetLoginCallbackHandler,
        targetJaasConfig);
  }

  public static void createMirrorMaker2Connector(
      String projectId,
      String region,
      String connectClusterId,
      String connectorId,
      String sourceClusterBootstrapServers,
      String targetClusterBootstrapServers,
      String sourceClusterAlias,
      String targetClusterAlias,
      String connectorClass,
      String topics,
      String offsetSyncsReplicationFactor,
      String sourceSecurityProtocol,
      String sourceSaslMechanism,
      String sourceLoginCallbackHandler,
      String sourceJaasConfig,
      String targetSecurityProtocol,
      String targetSaslMechanism,
      String targetLoginCallbackHandler,
      String targetJaasConfig)
      throws Exception {

    // Build the connector configuration
    Map<String, String> configMap = new HashMap<>();
    configMap.put("connector.class", connectorClass);
    configMap.put("name", connectorId);
    configMap.put("source.cluster.alias", sourceClusterAlias);
    configMap.put("target.cluster.alias", targetClusterAlias);
    configMap.put("topics", topics);
    configMap.put("source.cluster.bootstrap.servers", sourceClusterBootstrapServers);
    configMap.put("target.cluster.bootstrap.servers", targetClusterBootstrapServers);
    configMap.put("offset-syncs.topic.replication.factor", offsetSyncsReplicationFactor);
    configMap.put("source.cluster.security.protocol", sourceSecurityProtocol);
    configMap.put("source.cluster.sasl.mechanism", sourceSaslMechanism);
    configMap.put("source.cluster.sasl.login.callback.handler.class", sourceLoginCallbackHandler);
    configMap.put("source.cluster.sasl.jaas.config", sourceJaasConfig);
    configMap.put("target.cluster.security.protocol", targetSecurityProtocol);
    configMap.put("target.cluster.sasl.mechanism", targetSaslMechanism);
    configMap.put("target.cluster.sasl.login.callback.handler.class", targetLoginCallbackHandler);
    configMap.put("target.cluster.sasl.jaas.config", targetJaasConfig);

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
      System.out.printf("Created MirrorMaker2 connector: %s\n", response.getName());
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaConnectClient.createConnector got err: %s", e.getMessage());
    }
  }
}

// [END managedkafka_create_mirrormaker2_connector]
