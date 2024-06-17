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

import com.google.cloud.managedkafka.v1.ClusterName;
import com.google.cloud.managedkafka.v1.ConsumerGroup;
import com.google.cloud.managedkafka.v1.ConsumerGroupName;
import com.google.cloud.managedkafka.v1.ManagedKafkaClient;
import com.google.cloud.managedkafka.v1.UpdateConsumerGroupRequest;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class ConsumerGroupsTest {
  protected static final String consumerGroupId = "test-consumer-group";
  protected static final String consumerGroupName =
      "projects/test-project/locations/us-central1/clusters/"
          + "test-cluster/consumerGroups/test-consumer-group";
  private ByteArrayOutputStream bout;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @Test
  public void getConsumerGroupTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaClient);
      ConsumerGroup consumerGroup =
          ConsumerGroup.newBuilder()
              .setName(
                  ConsumerGroupName.of(
                          ClustersTest.projectId,
                          ClustersTest.region,
                          ClustersTest.clusterId,
                          consumerGroupId)
                      .toString())
              .build();
      when(managedKafkaClient.getConsumerGroup(any(ConsumerGroupName.class)))
          .thenReturn(consumerGroup);
      GetConsumerGroup.getConsumerGroup(
          ClustersTest.projectId, ClustersTest.region, ClustersTest.clusterId, consumerGroupId);
      String output = bout.toString();
      assertThat(output).contains(consumerGroupName);
      verify(managedKafkaClient, times(1)).getConsumerGroup(any(ConsumerGroupName.class));
    }
  }

  @Test
  public void listConsumerGroupsTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    ManagedKafkaClient.ListConsumerGroupsPagedResponse response =
        mock(ManagedKafkaClient.ListConsumerGroupsPagedResponse.class);
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaClient);
      Iterable<ConsumerGroup> iterable =
          () -> {
            ConsumerGroup consumerGroup =
                ConsumerGroup.newBuilder()
                    .setName(
                        ConsumerGroupName.of(
                                ClustersTest.projectId,
                                ClustersTest.region,
                                ClustersTest.clusterId,
                                consumerGroupId)
                            .toString())
                    .build();
            List<ConsumerGroup> list =
                new ArrayList<ConsumerGroup>(Collections.singletonList(consumerGroup));
            return list.iterator();
          };
      when(response.iterateAll()).thenReturn(iterable);
      when(managedKafkaClient.listConsumerGroups(any(ClusterName.class))).thenReturn(response);
      ListConsumerGroups.listConsumerGroups(
          ClustersTest.projectId, ClustersTest.region, ClustersTest.clusterId);
      String output = bout.toString();
      assertThat(output).contains(consumerGroupName);
      verify(response, times(1)).iterateAll();
      verify(managedKafkaClient, times(1)).listConsumerGroups(any(ClusterName.class));
    }
  }

  @Test
  public void updateConsumerGroupTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaClient);
      ConsumerGroup consumerGroup =
          ConsumerGroup.newBuilder()
              .setName(
                  ConsumerGroupName.of(
                          ClustersTest.projectId,
                          ClustersTest.region,
                          ClustersTest.clusterId,
                          consumerGroupId)
                      .toString())
              .build();
      when(managedKafkaClient.updateConsumerGroup(any(UpdateConsumerGroupRequest.class)))
          .thenReturn(consumerGroup);
      Map<Integer, Integer> partitionOffsets =
          new HashMap<Integer, Integer>() {
            {
              put(1, 10);
              put(2, 20);
              put(3, 30);
            }
          };
      UpdateConsumerGroup.updateConsumerGroup(
          ClustersTest.projectId,
          ClustersTest.region,
          ClustersTest.clusterId,
          TopicsTest.topicId,
          consumerGroupId,
          partitionOffsets);
      String output = bout.toString();
      assertThat(output).contains("Updated consumer group");
      assertThat(output).contains(consumerGroupName);
      verify(managedKafkaClient, times(1))
          .updateConsumerGroup(any(UpdateConsumerGroupRequest.class));
    }
  }

  @Test
  public void deleteConsumerGroupTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaClient);
      DeleteConsumerGroup.deleteConsumerGroup(
          ClustersTest.projectId, ClustersTest.region, ClustersTest.clusterId, consumerGroupId);
      String output = bout.toString();
      assertThat(output).contains("Deleted consumer group");
    }
  }
}
