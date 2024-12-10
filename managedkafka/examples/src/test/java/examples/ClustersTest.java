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

import static com.google.cloud.managedkafka.v1.ManagedKafkaClient.create;
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
import com.google.cloud.managedkafka.v1.Cluster;
import com.google.cloud.managedkafka.v1.ClusterName;
import com.google.cloud.managedkafka.v1.CreateClusterRequest;
import com.google.cloud.managedkafka.v1.DeleteClusterRequest;
import com.google.cloud.managedkafka.v1.LocationName;
import com.google.cloud.managedkafka.v1.ManagedKafkaClient;
import com.google.cloud.managedkafka.v1.ManagedKafkaSettings;
import com.google.cloud.managedkafka.v1.OperationMetadata;
import com.google.cloud.managedkafka.v1.UpdateClusterRequest;
import com.google.protobuf.Empty;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class ClustersTest {
  protected static final String projectId = "test-project";
  protected static final String region = "us-central1";
  protected static final String clusterId = "test-cluster";
  protected static final String clusterName =
      "projects/test-project/locations/us-central1/clusters/test-cluster";
  private ByteArrayOutputStream bout;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @Test
  public void createClusterTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    OperationCallable<CreateClusterRequest, Cluster, OperationMetadata> operationCallable =
        mock(OperationCallable.class);
    OperationFuture<Cluster, OperationMetadata> operationFuture =
        mock(OperationFuture.class);

    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {

      // client creation
      mockedStatic.when(() -> create(any(ManagedKafkaSettings.class)))
          .thenReturn(managedKafkaClient);

      // operation callable
      when(managedKafkaClient.createClusterOperationCallable())
          .thenReturn(operationCallable);
      when(operationCallable.futureCall(any(CreateClusterRequest.class)))
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
      Cluster resultCluster = mock(Cluster.class);
      when(operationFuture.get()).thenReturn(resultCluster);
      when(resultCluster.getName()).thenReturn(clusterName);

      String subnet = "test-subnet";
      int cpu = 3;
      long memory = 3221225472L;
      CreateCluster.createCluster(projectId, region, clusterId, subnet, cpu, memory);
      String output = bout.toString();
      assertThat(output).contains("Created cluster");
      assertThat(output).contains(clusterName);
      verify(managedKafkaClient, times(1)).createClusterOperationCallable();
      verify(operationCallable, times(1)).futureCall(any(CreateClusterRequest.class));
      verify(operationFuture, times(2)).getPollingFuture();  // Verify 2 polling attempts
      verify(pollingFuture, times(2)).getAttemptResult();    // Verify 2 attempt results
      verify(operationSnapshot, times(3)).isDone();          // 2 polls + 1 initial check
    }
  }

  @Test
  public void getClusterTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaClient);
      Cluster cluster =
          Cluster.newBuilder()
              .setName(ClusterName.of(projectId, region, clusterId).toString())
              .build();
      when(managedKafkaClient.getCluster(any(ClusterName.class))).thenReturn(cluster);
      GetCluster.getCluster(projectId, region, clusterId);
      String output = bout.toString();
      assertThat(output).contains(clusterName);
      verify(managedKafkaClient, times(1)).getCluster(any(ClusterName.class));
    }
  }

  @Test
  public void listClustersTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    ManagedKafkaClient.ListClustersPagedResponse response =
        mock(ManagedKafkaClient.ListClustersPagedResponse.class);
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaClient);
      Iterable<Cluster> iterable =
          () -> {
            Cluster cluster =
                Cluster.newBuilder()
                    .setName(ClusterName.of(projectId, region, clusterId).toString())
                    .build();
            List<Cluster> list = new ArrayList<Cluster>(Collections.singletonList(cluster));
            return list.iterator();
          };
      when(response.iterateAll()).thenReturn(iterable);
      when(managedKafkaClient.listClusters(any(LocationName.class))).thenReturn(response);
      ListClusters.listClusters(projectId, region);
      String output = bout.toString();
      assertThat(output).contains(clusterName);
      verify(response, times(1)).iterateAll();
      verify(managedKafkaClient, times(1)).listClusters(any(LocationName.class));
    }
  }

  @Test
  public void updateClusterTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    OperationCallable<UpdateClusterRequest, Cluster, OperationMetadata> operationCallable =
        mock(OperationCallable.class);
    OperationFuture<Cluster, OperationMetadata> operationFuture =
        mock(OperationFuture.class);

    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {

      // client creation
      mockedStatic.when(() -> create(any(ManagedKafkaSettings.class)))
          .thenReturn(managedKafkaClient);

      // operation callable
      when(managedKafkaClient.updateClusterOperationCallable())
          .thenReturn(operationCallable);
      when(operationCallable.futureCall(any(UpdateClusterRequest.class)))
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

      // Setup final result
      Cluster resultCluster = mock(Cluster.class);
      when(operationFuture.get()).thenReturn(resultCluster);
      when(resultCluster.getName()).thenReturn(clusterName);

      long updatedMemory = 4221225472L;
      UpdateCluster.updateCluster(projectId, region, projectId, updatedMemory);
      String output = bout.toString();
      assertThat(output).contains("Updated cluster");
      assertThat(output).contains(clusterName);
      verify(managedKafkaClient, times(1)).updateClusterOperationCallable();
      verify(operationCallable, times(1)).futureCall(any(UpdateClusterRequest.class));
    }
  }

  @Test
  public void deleteClusterTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    OperationCallable<DeleteClusterRequest, Empty, OperationMetadata> operationCallable =
        mock(OperationCallable.class);
    OperationFuture<Empty, OperationMetadata> operationFuture =
        mock(OperationFuture.class);
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {

      // client creation
      mockedStatic.when(() -> create(any(ManagedKafkaSettings.class)))
          .thenReturn(managedKafkaClient);

      // operation callable
      when(managedKafkaClient.deleteClusterOperationCallable())
          .thenReturn(operationCallable);
      when(operationCallable.futureCall(any(DeleteClusterRequest.class)))
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

      // Setup final result
      Cluster resultCluster = mock(Cluster.class);
      when(operationFuture.get()).thenReturn(Empty.getDefaultInstance());
      when(resultCluster.getName()).thenReturn(clusterName);

      DeleteCluster.deleteCluster(projectId, region, clusterId);
      String output = bout.toString();
      assertThat(output).contains("Deleted cluster");

      verify(managedKafkaClient, times(1)).deleteClusterOperationCallable();
      verify(operationCallable, times(1)).futureCall(any(DeleteClusterRequest.class));
    }
  }
}
