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

import com.google.cloud.managedkafka.v1.Cluster;
import com.google.cloud.managedkafka.v1.ClusterName;
import com.google.cloud.managedkafka.v1.LocationName;
import com.google.cloud.managedkafka.v1.ManagedKafkaClient;
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
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaClient);
      when(managedKafkaClient.createClusterOperationCallable())
          .thenReturn(MockOperationFuture.getOperableCallable());
      String subnet = "test-subnet";
      int cpu = 3;
      long memory = 3221225472L;
      CreateCluster.createCluster(projectId, region, clusterId, subnet, cpu, memory);
      String output = bout.toString();
      assertThat(output).contains("Created cluster");
      assertThat(output).contains(clusterName);
      verify(managedKafkaClient, times(1)).createClusterOperationCallable();
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
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaClient);
      when(managedKafkaClient.updateClusterOperationCallable())
          .thenReturn(MockOperationFuture.getOperableCallable());
      long updatedMemory = 4221225472L;
      UpdateCluster.updateCluster(projectId, region, projectId, updatedMemory);
      String output = bout.toString();
      assertThat(output).contains("Updated cluster");
      assertThat(output).contains(clusterName);
      verify(managedKafkaClient, times(1)).updateClusterOperationCallable();
    }
  }

  @Test
  public void deleteClusterTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaClient);
      when(managedKafkaClient.deleteClusterOperationCallable())
          .thenReturn(MockDeleteOperationFuture.getOperableCallable());
      DeleteCluster.deleteCluster(projectId, region, clusterId);
      String output = bout.toString();
      assertThat(output).contains("Deleted cluster");
      verify(managedKafkaClient, times(1)).deleteClusterOperationCallable();
    }
  }
}
