/*
 * Copyright 2016 Google Inc.
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

package com.example.pubsub;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for quickstart sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class QuickStartIT {

  private ByteArrayOutputStream bout;

  private String projectId = ServiceOptions.getDefaultProjectId();
  private String topicId = formatForTest("my-topic");
  private String subscriptionId = formatForTest("my-sub");
  private int messageCount = 5;

  class SubscriberRunnable implements Runnable {

    private String subscriptionId;

    SubscriberRunnable(String subscriptionId) {
      this.subscriptionId = subscriptionId;
    }

    @Override
    public void run() {
      try {
        SubscriberExample.main(subscriptionId);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Rule public Timeout globalTimeout = Timeout.seconds(300); // 5 minute timeout

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
    try {
      deleteTestSubscription();
      deleteTestTopic();
    } catch (Exception e) {
      // topic, subscription may not yet exist
    }
  }

  @After
  public void tearDown() throws Exception {
    System.setOut(null);
    deleteTestSubscription();
    deleteTestTopic();
  }

  @Test
  public void testQuickstart() throws Exception {
    // create a topic
    CreateTopicExample.main(topicId);
    String got = bout.toString();
    assertThat(got).contains(topicId + " created.");

    // create a subscriber
    CreatePullSubscriptionExample.main(topicId, subscriptionId);
    got = bout.toString();
    assertThat(got).contains(subscriptionId + " created.");

    bout.reset();
    // publish messages
    PublisherExample.main(topicId, String.valueOf(messageCount));
    String[] messageIds = bout.toString().split("\n");
    assertThat(messageIds).hasLength(messageCount);

    bout.reset();
    // receive messages
    Thread subscriberThread = new Thread(new SubscriberRunnable(subscriptionId));
    subscriberThread.start();
    Set<String> expectedMessageIds = new HashSet<>();
    List<String> receivedMessageIds = new ArrayList<>();
    expectedMessageIds.addAll(Arrays.asList(messageIds));
    while (!expectedMessageIds.isEmpty()) {
      for (String expectedId : expectedMessageIds) {
        if (bout.toString().contains(expectedId)) {
          receivedMessageIds.add(expectedId);
        }
      }
      expectedMessageIds.removeAll(receivedMessageIds);
    }
    assertThat(expectedMessageIds).isEmpty();
  }

  private String formatForTest(String name) {
    return name + "-" + java.util.UUID.randomUUID().toString();
  }

  private void deleteTestTopic() throws Exception {
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(projectId, topicId));
    } catch (IOException e) {
      System.err.println("Error deleting topic " + e.getMessage());
    }
  }

  private void deleteTestSubscription() throws Exception {
    try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
      subscriptionAdminClient.deleteSubscription(
          ProjectSubscriptionName.of(projectId, subscriptionId));
    } catch (IOException e) {
      System.err.println("Error deleting subscription " + e.getMessage());
    }
  }
}
