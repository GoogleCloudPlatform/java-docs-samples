/*
 * Copyright 2023 Google LLC
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

package com.example.dataflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PubSubWriteIT {
  private static final String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");

  private ByteArrayOutputStream bout;
  private PrintStream out;
  private String topicId;
  private String subscriptionId;
  TopicAdminClient topicAdminClient;
  SubscriptionAdminClient subscriptionAdminClient;

  @Before
  public void setUp() throws IOException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    topicId = "test_topic_" + UUID.randomUUID().toString().substring(0, 8);
    subscriptionId = topicId + "-sub";

    TopicName topicName = TopicName.of(projectId, topicId);
    topicAdminClient = TopicAdminClient.create();
    topicAdminClient.createTopic(topicName);

    SubscriptionName subscriptionName = SubscriptionName.of(projectId, subscriptionId);
    subscriptionAdminClient = SubscriptionAdminClient.create();
    subscriptionAdminClient.createSubscription(subscriptionName, topicName,
        PushConfig.getDefaultInstance(), 120);
  }

  @After
  public void tearDown() {
    subscriptionAdminClient.deleteSubscription(SubscriptionName.of(projectId, subscriptionId));
    topicAdminClient.deleteTopic(TopicName.of(projectId, topicId));
    System.setOut(null);
  }

  @Test
  public void testPubSubWriteWithAttributes() throws Exception {

    final AtomicInteger count = new AtomicInteger(0);

    PubSubWriteWithAttributes.main(
        new String[] {
            "--runner=DirectRunner",
            "--topic=" + String.format("projects/%s/topics/%s", projectId, topicId)
        });

    // Verify that the pipeline wrote messages to Pub/Sub
    MessageReceiver receiver =
        (PubsubMessage message, AckReplyConsumer consumer) -> {
          count.incrementAndGet();
          consumer.ack();
        };

    Subscriber subscriber = null;
    try {
      ProjectSubscriptionName subscriptionName =
          ProjectSubscriptionName.of(projectId, subscriptionId);

      subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
      subscriber.startAsync().awaitRunning();
      subscriber.awaitTerminated(30, TimeUnit.SECONDS);
    } catch (TimeoutException timeoutException) {
      subscriber.stopAsync();
    }
    assertEquals(4, count.get());
  }
}
