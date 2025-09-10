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

import static com.google.cloud.managedkafka.v1.ManagedKafkaConnectClient.create;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.managedkafka.v1.Connector;
import com.google.cloud.managedkafka.v1.ConnectorName;
import com.google.cloud.managedkafka.v1.CreateConnectorRequest;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectClient;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class ConnectorsTest {

  protected static final String projectId = "test-project";
  protected static final String region = "us-central1";
  protected static final String connectClusterId = "test-connect-cluster";
  protected static final String mirrorMaker2ConnectorId = "test-mirrormaker2-source-connector";
  protected static final String pubsubSourceConnectorId = "test-pubsub-source-connector";
  protected static final String pubsubSinkConnectorId = "test-pubsub-sink-connector";
  protected static final String gcsConnectorId = "test-gcs-sink-connector";
  protected static final String bigqueryConnectorId = "test-bigquery-sink-connector";

  protected static final String mirrorMaker2SourceConnectorName =
      "projects/test-project/locations/us-central1/connectClusters/"
          + "test-connect-cluster/connectors/test-mirrormaker2-source-connector";
  protected static final String pubsubSourceConnectorName =
      "projects/test-project/locations/us-central1/connectClusters/"
          + "test-connect-cluster/connectors/test-pubsub-source-connector";
  protected static final String pubsubSinkConnectorName =
      "projects/test-project/locations/us-central1/connectClusters/"
          + "test-connect-cluster/connectors/test-pubsub-sink-connector";
  protected static final String gcsConnectorName =
      "projects/test-project/locations/us-central1/connectClusters/"
          + "test-connect-cluster/connectors/test-gcs-sink-connector";
  protected static final String bigqueryConnectorName =
      "projects/test-project/locations/us-central1/connectClusters/"
          + "test-connect-cluster/connectors/test-bigquery-sink-connector";

  private ByteArrayOutputStream bout;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @Test
  public void createMirrorMaker2SourceConnectorTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);
      Connector connector =
          Connector.newBuilder()
              .setName(
                  ConnectorName.of(projectId, region, connectClusterId, mirrorMaker2ConnectorId)
                      .toString())
              .build();
      when(managedKafkaConnectClient.createConnector(any(CreateConnectorRequest.class)))
          .thenReturn(connector);

      String sourceClusterBootstrapServers = "source-cluster:9092";
      String targetClusterBootstrapServers = "target-cluster:9092";
      String maxTasks = "3";
      String sourceClusterAlias = "source";
      String targetClusterAlias = "target";
      String connectorClass = "org.apache.kafka.connect.mirror.MirrorSourceConnector";
      String topics = ".*";
      String topicsExclude = "mm2.*.internal,.*.replica,__.*";

      CreateMirrorMaker2SourceConnector.createMirrorMaker2SourceConnector(
          projectId,
          region,
          maxTasks,
          connectClusterId,
          mirrorMaker2ConnectorId,
          sourceClusterBootstrapServers,
          targetClusterBootstrapServers,
          sourceClusterAlias,
          targetClusterAlias,
          connectorClass,
          topics,
          topicsExclude);

      String output = bout.toString();
      assertThat(output).contains("Created MirrorMaker2 Source connector");
      assertThat(output).contains(mirrorMaker2SourceConnectorName);
      verify(managedKafkaConnectClient, times(1))
          .createConnector(any(CreateConnectorRequest.class));
    }
  }

  @Test
  public void createPubSubSourceConnectorTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);
      Connector connector =
          Connector.newBuilder()
              .setName(
                  ConnectorName.of(projectId, region, connectClusterId, pubsubSourceConnectorId)
                      .toString())
              .build();
      when(managedKafkaConnectClient.createConnector(any(CreateConnectorRequest.class)))
          .thenReturn(connector);

      String pubsubProjectId = "test-pubsub-project";
      String subscriptionName = "test-subscription";
      String kafkaTopicName = "test-kafka-topic";
      String connectorClass = "com.google.pubsub.kafka.source.CloudPubSubSourceConnector";
      String maxTasks = "3";
      String valueConverter = "org.apache.kafka.connect.converters.ByteArrayConverter";
      String keyConverter = "org.apache.kafka.connect.storage.StringConverter";

      CreatePubSubSourceConnector.createPubSubSourceConnector(
          projectId,
          region,
          connectClusterId,
          pubsubSourceConnectorId,
          pubsubProjectId,
          subscriptionName,
          kafkaTopicName,
          connectorClass,
          maxTasks,
          valueConverter,
          keyConverter);

      String output = bout.toString();
      assertThat(output).contains("Created Pub/Sub Source connector");
      assertThat(output).contains(pubsubSourceConnectorName);
      verify(managedKafkaConnectClient, times(1))
          .createConnector(any(CreateConnectorRequest.class));
    }
  }

  @Test
  public void createPubSubSinkConnectorTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);
      Connector connector =
          Connector.newBuilder()
              .setName(
                  ConnectorName.of(projectId, region, connectClusterId, pubsubSinkConnectorId)
                      .toString())
              .build();
      when(managedKafkaConnectClient.createConnector(any(CreateConnectorRequest.class)))
          .thenReturn(connector);

      String pubsubProjectId = "test-pubsub-project";
      String pubsubTopicName = "test-pubsub-topic";
      String kafkaTopicName = "test-kafka-topic";
      String connectorClass = "com.google.pubsub.kafka.sink.CloudPubSubSinkConnector";
      String maxTasks = "3";
      String valueConverter = "org.apache.kafka.connect.storage.StringConverter";
      String keyConverter = "org.apache.kafka.connect.storage.StringConverter";

      CreatePubSubSinkConnector.createPubSubSinkConnector(
          projectId,
          region,
          connectClusterId,
          pubsubSinkConnectorId,
          pubsubProjectId,
          pubsubTopicName,
          kafkaTopicName,
          connectorClass,
          maxTasks,
          valueConverter,
          keyConverter);

      String output = bout.toString();
      assertThat(output).contains("Created Pub/Sub Sink connector");
      assertThat(output).contains(pubsubSinkConnectorName);
      verify(managedKafkaConnectClient, times(1))
          .createConnector(any(CreateConnectorRequest.class));
    }
  }

  @Test
  public void createCloudStorageSinkConnectorTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);
      Connector connector =
          Connector.newBuilder()
              .setName(
                  ConnectorName.of(projectId, region, connectClusterId, gcsConnectorId).toString())
              .build();
      when(managedKafkaConnectClient.createConnector(any(CreateConnectorRequest.class)))
          .thenReturn(connector);

      String bucketName = "test-gcs-bucket";
      String kafkaTopicName = "test-kafka-topic";
      String connectorClass = "io.aiven.kafka.connect.gcs.GcsSinkConnector";
      String maxTasks = "3";
      String gcsCredentialsDefault = "true";
      String formatOutputType = "json";
      String valueConverter = "org.apache.kafka.connect.json.JsonConverter";
      String valueSchemasEnable = "false";
      String keyConverter = "org.apache.kafka.connect.storage.StringConverter";

      CreateCloudStorageSinkConnector.createCloudStorageSinkConnector(
          projectId,
          region,
          connectClusterId,
          gcsConnectorId,
          bucketName,
          kafkaTopicName,
          connectorClass,
          maxTasks,
          gcsCredentialsDefault,
          formatOutputType,
          valueConverter,
          valueSchemasEnable,
          keyConverter);

      String output = bout.toString();
      assertThat(output).contains("Created Cloud Storage Sink connector");
      assertThat(output).contains(gcsConnectorName);
      verify(managedKafkaConnectClient, times(1))
          .createConnector(any(CreateConnectorRequest.class));
    }
  }

  @Test
  public void createBigQuerySinkConnectorTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);
      Connector connector =
          Connector.newBuilder()
              .setName(
                  ConnectorName.of(projectId, region, connectClusterId, bigqueryConnectorId)
                      .toString())
              .build();
      when(managedKafkaConnectClient.createConnector(any(CreateConnectorRequest.class)))
          .thenReturn(connector);

      String bigqueryProjectId = "test-bigquery-project";
      String datasetName = "test_dataset";
      String kafkaTopicName = "test-kafka-topic";
      String maxTasks = "3";
      String connectorClass = "com.wepay.kafka.connect.bigquery.BigQuerySinkConnector";
      String keyConverter = "org.apache.kafka.connect.storage.StringConverter";
      String valueConverter = "org.apache.kafka.connect.json.JsonConverter";
      String valueSchemasEnable = "false";

      CreateBigQuerySinkConnector.createBigQuerySinkConnector(
          projectId,
          region,
          connectClusterId,
          bigqueryConnectorId,
          bigqueryProjectId,
          datasetName,
          kafkaTopicName,
          maxTasks,
          connectorClass,
          keyConverter,
          valueConverter,
          valueSchemasEnable);

      String output = bout.toString();
      assertThat(output).contains("Created BigQuery Sink connector");
      assertThat(output).contains(bigqueryConnectorName);
      verify(managedKafkaConnectClient, times(1))
          .createConnector(any(CreateConnectorRequest.class));
    }
  }
}
