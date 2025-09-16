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

import com.google.api.core.ApiFuture;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.api.gax.rpc.OperationCallable;
import com.google.cloud.managedkafka.v1.ConnectCluster;
import com.google.cloud.managedkafka.v1.ConnectClusterName;
import com.google.cloud.managedkafka.v1.Connector;
import com.google.cloud.managedkafka.v1.ConnectorName;
import com.google.cloud.managedkafka.v1.CreateConnectClusterRequest;
import com.google.cloud.managedkafka.v1.DeleteConnectClusterRequest;
import com.google.cloud.managedkafka.v1.LocationName;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectClient;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectSettings;
import com.google.cloud.managedkafka.v1.OperationMetadata;
import com.google.cloud.managedkafka.v1.PauseConnectorRequest;
import com.google.cloud.managedkafka.v1.RestartConnectorRequest;
import com.google.cloud.managedkafka.v1.ResumeConnectorRequest;
import com.google.cloud.managedkafka.v1.StopConnectorRequest;
import com.google.cloud.managedkafka.v1.UpdateConnectClusterRequest;
import com.google.protobuf.Empty;
import com.google.protobuf.FieldMask;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class ConnectClustersTest {
  protected static final String projectId = "test-project";
  protected static final String region = "us-central1";
  protected static final String clusterId = "test-connect-cluster";
  protected static final String kafkaCluster = "test-kafka-cluster";
  protected static final String connectClusterName =
      "projects/test-project/locations/us-central1/connectClusters/test-connect-cluster";
  protected static final String connectorId = "test-connector";
  protected static final String connectorName =
      "projects/test-project/locations/us-central1/connectClusters/test-connect-cluster"
          + "/connectors/test-connector";
  private ByteArrayOutputStream bout;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @Test
  public void createConnectClusterTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    OperationCallable<CreateConnectClusterRequest, ConnectCluster, OperationMetadata>
        operationCallable = mock(OperationCallable.class);
    OperationFuture<ConnectCluster, OperationMetadata> operationFuture =
        mock(OperationFuture.class);

    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {

      // client creation
      mockedStatic
          .when(() -> create(any(ManagedKafkaConnectSettings.class)))
          .thenReturn(managedKafkaConnectClient);

      // operation callable
      when(managedKafkaConnectClient.createConnectClusterOperationCallable())
          .thenReturn(operationCallable);
      when(operationCallable.futureCall(any(CreateConnectClusterRequest.class)))
          .thenReturn(operationFuture);

      // initial future
      ApiFuture<OperationSnapshot> initialFuture = mock(ApiFuture.class);
      when(operationFuture.getInitialFuture()).thenReturn(initialFuture);

      // Metadata
      ApiFuture<OperationMetadata> metadataFuture = mock(ApiFuture.class);
      OperationMetadata metadata = mock(OperationMetadata.class);
      when(operationFuture.getMetadata()).thenReturn(metadataFuture);
      when(metadataFuture.get()).thenReturn(metadata);

      // operation snapshot
      OperationSnapshot operationSnapshot = mock(OperationSnapshot.class);
      when(operationFuture.getInitialFuture().get()).thenReturn(operationSnapshot);
      when(operationSnapshot.getName())
          .thenReturn("projects/test-project/locations/test-location/operations/test-operation");
      when(operationSnapshot.isDone()).thenReturn(false, false, true);

      // polling future
      RetryingFuture<OperationSnapshot> pollingFuture = mock(RetryingFuture.class);
      when(operationFuture.getPollingFuture()).thenReturn(pollingFuture);
      when(operationFuture.isDone()).thenReturn(false, false, true);
      ApiFuture<OperationSnapshot> attemptResult = mock(ApiFuture.class);
      when(pollingFuture.getAttemptResult()).thenReturn(attemptResult);
      when(attemptResult.get()).thenReturn(operationSnapshot);

      // Setup final result
      ConnectCluster resultCluster = mock(ConnectCluster.class);
      when(operationFuture.get()).thenReturn(resultCluster);
      when(resultCluster.getName()).thenReturn(connectClusterName);

      String subnet = "test-subnet";
      int vcpu = 12;
      long memory = 12884901888L; // 12 GiB
      CreateConnectCluster.createConnectCluster(
          projectId, region, clusterId, subnet, kafkaCluster, vcpu, memory);
      String output = bout.toString();
      assertThat(output).contains("Created connect cluster");
      assertThat(output).contains(connectClusterName);
      verify(managedKafkaConnectClient, times(1)).createConnectClusterOperationCallable();
      verify(operationCallable, times(1)).futureCall(any(CreateConnectClusterRequest.class));
      verify(operationFuture, times(2)).getPollingFuture(); // Verify 2 polling attempts
      verify(pollingFuture, times(2)).getAttemptResult(); // Verify 2 attempt results
      verify(operationSnapshot, times(3)).isDone(); // 2 polls + 1 initial check
    }
  }

  @Test
  public void getConnectClusterTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);
      ConnectCluster connectCluster =
          ConnectCluster.newBuilder()
              .setName(ConnectClusterName.of(projectId, region, clusterId).toString())
              .build();
      when(managedKafkaConnectClient.getConnectCluster(any(ConnectClusterName.class)))
          .thenReturn(connectCluster);
      GetConnectCluster.getConnectCluster(projectId, region, clusterId);
      String output = bout.toString();
      assertThat(output).contains(connectClusterName);
      verify(managedKafkaConnectClient, times(1)).getConnectCluster(any(ConnectClusterName.class));
    }
  }

  @Test
  public void listConnectClustersTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    ManagedKafkaConnectClient.ListConnectClustersPagedResponse response =
        mock(ManagedKafkaConnectClient.ListConnectClustersPagedResponse.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);
      Iterable<ConnectCluster> iterable =
          () -> {
            List<ConnectCluster> connectClusters = new ArrayList<>();
            connectClusters.add(
                ConnectCluster.newBuilder()
                    .setName(ConnectClusterName.of(projectId, region, clusterId).toString())
                    .build());
            return connectClusters.iterator();
          };
      when(response.iterateAll()).thenReturn(iterable);
      when(managedKafkaConnectClient.listConnectClusters(any(LocationName.class)))
          .thenReturn(response);
      ListConnectClusters.listConnectClusters(projectId, region);
      String output = bout.toString();
      assertThat(output).contains(connectClusterName);
      verify(managedKafkaConnectClient, times(1)).listConnectClusters(any(LocationName.class));
    }
  }

  @Test
  public void updateConnectClusterTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    OperationCallable<UpdateConnectClusterRequest, ConnectCluster, OperationMetadata>
        operationCallable = mock(OperationCallable.class);
    OperationFuture<ConnectCluster, OperationMetadata> operationFuture =
        mock(OperationFuture.class);

    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {

      // client creation
      mockedStatic
          .when(() -> create(any(ManagedKafkaConnectSettings.class)))
          .thenReturn(managedKafkaConnectClient);

      // operation callable
      when(managedKafkaConnectClient.updateConnectClusterOperationCallable())
          .thenReturn(operationCallable);
      when(operationCallable.futureCall(any(UpdateConnectClusterRequest.class)))
          .thenReturn(operationFuture);

      // initial future
      ApiFuture<OperationSnapshot> initialFuture = mock(ApiFuture.class);
      when(operationFuture.getInitialFuture()).thenReturn(initialFuture);

      // Metadata
      ApiFuture<OperationMetadata> metadataFuture = mock(ApiFuture.class);
      OperationMetadata metadata = mock(OperationMetadata.class);
      when(operationFuture.getMetadata()).thenReturn(metadataFuture);
      when(metadataFuture.get()).thenReturn(metadata);

      // operation snapshot
      OperationSnapshot operationSnapshot = mock(OperationSnapshot.class);
      when(operationFuture.getInitialFuture().get()).thenReturn(operationSnapshot);
      when(operationSnapshot.getName())
          .thenReturn("projects/test-project/locations/test-location/operations/test-operation");
      when(operationSnapshot.isDone()).thenReturn(true);

      // Setup final result
      ConnectCluster resultCluster = mock(ConnectCluster.class);
      when(operationFuture.get()).thenReturn(resultCluster);
      when(resultCluster.getName()).thenReturn(connectClusterName);

      long memory = 38654705664L; // 36 GiB
      UpdateConnectCluster.updateConnectCluster(projectId, region, clusterId, memory);
      String output = bout.toString();
      assertThat(output).contains("Updated connect cluster");
      assertThat(output).contains(connectClusterName);
      verify(managedKafkaConnectClient, times(1)).updateConnectClusterOperationCallable();
      verify(operationCallable, times(1)).futureCall(any(UpdateConnectClusterRequest.class));
    }
  }

  @Test
  public void deleteConnectClusterTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    OperationCallable<DeleteConnectClusterRequest, Empty, OperationMetadata> operationCallable =
        mock(OperationCallable.class);
    OperationFuture<Empty, OperationMetadata> operationFuture = mock(OperationFuture.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {

      // client creation
      mockedStatic
          .when(() -> create(any(ManagedKafkaConnectSettings.class)))
          .thenReturn(managedKafkaConnectClient);

      // operation callable
      when(managedKafkaConnectClient.deleteConnectClusterOperationCallable())
          .thenReturn(operationCallable);
      when(operationCallable.futureCall(any(DeleteConnectClusterRequest.class)))
          .thenReturn(operationFuture);

      // initial future
      ApiFuture<OperationSnapshot> initialFuture = mock(ApiFuture.class);
      when(operationFuture.getInitialFuture()).thenReturn(initialFuture);

      // Metadata
      ApiFuture<OperationMetadata> metadataFuture = mock(ApiFuture.class);
      OperationMetadata metadata = mock(OperationMetadata.class);
      when(operationFuture.getMetadata()).thenReturn(metadataFuture);
      when(metadataFuture.get()).thenReturn(metadata);

      // operation snapshot
      OperationSnapshot operationSnapshot = mock(OperationSnapshot.class);
      when(operationFuture.getInitialFuture().get()).thenReturn(operationSnapshot);
      when(operationSnapshot.getName())
          .thenReturn("projects/test-project/locations/test-location/operations/test-operation");
      when(operationSnapshot.isDone()).thenReturn(true);

      // Setup final result
      Empty resultEmpty = mock(Empty.class);
      when(operationFuture.get()).thenReturn(resultEmpty);

      DeleteConnectCluster.deleteConnectCluster(projectId, region, clusterId);
      String output = bout.toString();
      assertThat(output).contains("Deleted connect cluster");
      verify(managedKafkaConnectClient, times(1)).deleteConnectClusterOperationCallable();
      verify(operationCallable, times(1)).futureCall(any(DeleteConnectClusterRequest.class));
    }
  }

  @Test
  public void pauseConnectorTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);
      PauseConnector.pauseConnector(projectId, region, clusterId, connectorId);
      String output = bout.toString();
      assertThat(output).contains("Connector " + connectorId + " paused successfully.");
      verify(managedKafkaConnectClient, times(1)).pauseConnector(any(PauseConnectorRequest.class));
    }
  }

  @Test
  public void listConnectorsTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    ManagedKafkaConnectClient.ListConnectorsPagedResponse response =
        mock(ManagedKafkaConnectClient.ListConnectorsPagedResponse.class);

    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);

      List<Connector> connectors = new ArrayList<>();
      connectors.add(Connector.newBuilder().setName(connectorName).build());
      Iterable<Connector> iterable = () -> connectors.iterator();

      when(response.iterateAll()).thenReturn(iterable);
      when(managedKafkaConnectClient.listConnectors(any(ConnectClusterName.class)))
          .thenReturn(response);

      ListConnectors.listConnectors(projectId, region, clusterId);

      String output = bout.toString();
      assertThat(output).contains(connectorName);
      verify(managedKafkaConnectClient, times(1)).listConnectors(any(ConnectClusterName.class));
    }
  }

  @Test
  public void getConnectorTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);

      Connector connector = Connector.newBuilder().setName(connectorName).build();
      when(managedKafkaConnectClient.getConnector(any(ConnectorName.class))).thenReturn(connector);

      GetConnector.getConnector(projectId, region, clusterId, connectorId);
      String output = bout.toString();

      assertThat(output).contains(connectorName);
      verify(managedKafkaConnectClient, times(1)).getConnector(any(ConnectorName.class));
    }
  }

  @Test
  public void deleteConnectorTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);

      DeleteConnector.deleteConnector(projectId, region, clusterId, connectorId);

      String output = bout.toString();
      assertThat(output).contains("Deleted connector: " + connectorName);
      verify(managedKafkaConnectClient, times(1)).deleteConnector(any(ConnectorName.class));
    }
  }

  @Test
  public void updateConnectorTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);

      Connector updatedConnector =
          Connector.newBuilder().setName(connectorName).putConfigs("tasks.max", "5").build();

      when(managedKafkaConnectClient.updateConnector(any(Connector.class), any(FieldMask.class)))
          .thenReturn(updatedConnector);

      UpdateConnector.updateConnector(projectId, region, clusterId, connectorId, "5");

      String output = bout.toString();
      assertThat(output).contains("Updated connector: " + connectorName);
      assertThat(output).contains("tasks.max");
      assertThat(output).contains("5");
      verify(managedKafkaConnectClient, times(1))
          .updateConnector(any(Connector.class), any(FieldMask.class));
    }
  }

  @Test
  public void resumeConnectorTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);
      ResumeConnector.resumeConnector(projectId, region, clusterId, connectorId);
      String output = bout.toString();
      assertThat(output).contains("Connector " + connectorId + " resumed successfully.");
      verify(managedKafkaConnectClient, times(1))
          .resumeConnector(any(ResumeConnectorRequest.class));
    }
  }

  @Test
  public void restartConnectorTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);
      RestartConnector.restartConnector(projectId, region, clusterId, connectorId);
      String output = bout.toString();
      assertThat(output).contains("Connector " + connectorId + " restarted successfully.");
      verify(managedKafkaConnectClient, times(1))
          .restartConnector(any(RestartConnectorRequest.class));
    }
  }

  @Test
  public void stopConnectorTest() throws Exception {
    ManagedKafkaConnectClient managedKafkaConnectClient = mock(ManagedKafkaConnectClient.class);
    try (MockedStatic<ManagedKafkaConnectClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaConnectClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaConnectClient);
      StopConnector.stopConnector(projectId, region, clusterId, connectorId);
      String output = bout.toString();
      assertThat(output).contains("Connector " + connectorId + " stopped successfully.");
      verify(managedKafkaConnectClient, times(1)).stopConnector(any(StopConnectorRequest.class));
    }
  }
}
