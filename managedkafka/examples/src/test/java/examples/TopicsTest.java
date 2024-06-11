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
import com.google.cloud.managedkafka.v1.CreateTopicRequest;
import com.google.cloud.managedkafka.v1.ManagedKafkaClient;
import com.google.cloud.managedkafka.v1.Topic;
import com.google.cloud.managedkafka.v1.TopicName;
import com.google.cloud.managedkafka.v1.UpdateTopicRequest;
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
public class TopicsTest {
  protected static final String topicId = "test-topic";
  protected static final String topicName =
      "projects/test-project/locations/us-central1/clusters/test-cluster/topics/test-topic";
  private ByteArrayOutputStream bout;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @Test
  public void createTopicTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaClient);
      Topic topic =
          Topic.newBuilder()
              .setName(
                  TopicName.of(
                          ClustersTest.projectId,
                          ClustersTest.region,
                          ClustersTest.clusterId,
                          topicId)
                      .toString())
              .build();
      when(managedKafkaClient.createTopic(any(CreateTopicRequest.class))).thenReturn(topic);
      int partitionCount = 10;
      int replicationFactor = 3;
      Map<String, String> configs =
          new HashMap<String, String>() {
            {
              put("min.insync.replicas", "2");
            }
          };
      CreateTopic.createTopic(
          ClustersTest.projectId,
          ClustersTest.region,
          ClustersTest.clusterId,
          topicId,
          partitionCount,
          replicationFactor,
          configs);
      String output = bout.toString();
      assertThat(output).contains("Created topic");
      assertThat(output).contains(topicName);
      verify(managedKafkaClient, times(1)).createTopic(any(CreateTopicRequest.class));
    }
  }

  @Test
  public void getTopicTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaClient);
      Topic topic =
          Topic.newBuilder()
              .setName(
                  TopicName.of(
                          ClustersTest.projectId,
                          ClustersTest.region,
                          ClustersTest.clusterId,
                          topicId)
                      .toString())
              .build();
      when(managedKafkaClient.getTopic(any(TopicName.class))).thenReturn(topic);
      GetTopic.getTopic(
          ClustersTest.projectId, ClustersTest.region, ClustersTest.clusterId, topicId);
      String output = bout.toString();
      assertThat(output).contains(topicName);
      verify(managedKafkaClient, times(1)).getTopic(any(TopicName.class));
    }
  }

  @Test
  public void listTopicsTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    ManagedKafkaClient.ListTopicsPagedResponse response =
        mock(ManagedKafkaClient.ListTopicsPagedResponse.class);
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaClient);
      Iterable<Topic> iterable =
          () -> {
            Topic topic =
                Topic.newBuilder()
                    .setName(
                        TopicName.of(
                                ClustersTest.projectId,
                                ClustersTest.region,
                                ClustersTest.clusterId,
                                topicId)
                            .toString())
                    .build();
            List<Topic> list = new ArrayList<Topic>(Collections.singletonList(topic));
            return list.iterator();
          };
      when(response.iterateAll()).thenReturn(iterable);
      when(managedKafkaClient.listTopics(any(ClusterName.class))).thenReturn(response);
      ListTopics.listTopics(ClustersTest.projectId, ClustersTest.region, ClustersTest.clusterId);
      String output = bout.toString();
      assertThat(output).contains(topicName);
      verify(response, times(1)).iterateAll();
      verify(managedKafkaClient, times(1)).listTopics(any(ClusterName.class));
    }
  }

  @Test
  public void updateTopicTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaClient);
      Topic topic =
          Topic.newBuilder()
              .setName(
                  TopicName.of(
                          ClustersTest.projectId,
                          ClustersTest.region,
                          ClustersTest.clusterId,
                          topicId)
                      .toString())
              .build();
      when(managedKafkaClient.updateTopic(any(UpdateTopicRequest.class))).thenReturn(topic);
      int partitionCount = 20;
      Map<String, String> configs =
          new HashMap<String, String>() {
            {
              put("min.insync.replicas", "1");
            }
          };
      UpdateTopic.updateTopic(
          ClustersTest.projectId,
          ClustersTest.region,
          ClustersTest.clusterId,
          topicId,
          partitionCount,
          configs);
      String output = bout.toString();
      assertThat(output).contains("Updated topic");
      assertThat(output).contains(topicName);
      verify(managedKafkaClient, times(1)).updateTopic(any(UpdateTopicRequest.class));
    }
  }

  @Test
  public void deleteTopicTest() throws Exception {
    ManagedKafkaClient managedKafkaClient = mock(ManagedKafkaClient.class);
    try (MockedStatic<ManagedKafkaClient> mockedStatic =
        Mockito.mockStatic(ManagedKafkaClient.class)) {
      mockedStatic.when(() -> create()).thenReturn(managedKafkaClient);
      DeleteTopic.deleteTopic(
          ClustersTest.projectId, ClustersTest.region, ClustersTest.clusterId, topicId);
      String output = bout.toString();
      assertThat(output).contains("Deleted topic");
    }
  }
}
