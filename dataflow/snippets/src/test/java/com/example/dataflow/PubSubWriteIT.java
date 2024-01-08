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

import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertEquals;

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
import java.io.PrintStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PubSubWriteIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

  private ByteArrayOutputStream bout;
  private PrintStream out;
  private String topicId;
  private String subscriptionId;
  TopicAdminClient topicAdminClient;
  SubscriptionAdminClient subscriptionAdminClient;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @Before
  public void setUp() throws Exception {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    topicId = "test_topic_" + UUID.randomUUID().toString().substring(0, 8);
    subscriptionId = topicId + "-sub";

    TopicName topicName = TopicName.of(PROJECT_ID, topicId);
    topicAdminClient = TopicAdminClient.create();
    topicAdminClient.createTopic(topicName);

    SubscriptionName subscriptionName = SubscriptionName.of(PROJECT_ID, subscriptionId);
    subscriptionAdminClient = SubscriptionAdminClient.create();
    subscriptionAdminClient.createSubscription(subscriptionName, topicName,
        PushConfig.getDefaultInstance(), 120);
  }

  @After
  public void tearDown() {
    subscriptionAdminClient.deleteSubscription(SubscriptionName.of(PROJECT_ID, subscriptionId));
    topicAdminClient.deleteTopic(TopicName.of(PROJECT_ID, topicId));
    System.setOut(null);
  }

  @Test
  public void testPubSubWriteWithAttributes() throws Exception {

    Map<String, PubsubMessage> messages = new ConcurrentHashMap<>();

    PubSubWriteWithAttributes.main(
        new String[] {
            "--runner=DirectRunner",
            "--topic=" + String.format("projects/%s/topics/%s", PROJECT_ID, topicId)
        });

    MessageReceiver receiver =
        (PubsubMessage message, AckReplyConsumer consumer) -> {
          // Store in a map by message ID, which are guaranteed to be unique within a topic.
          messages.put(message.getMessageId(), message);
          consumer.ack();
        };

    // Verify that the pipeline wrote messages to Pub/Sub
    Subscriber subscriber = null;
    try {
      ProjectSubscriptionName subscriptionName =
          ProjectSubscriptionName.of(PROJECT_ID, subscriptionId);

      subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
      subscriber.startAsync().awaitRunning();
      subscriber.awaitTerminated(30, TimeUnit.SECONDS);
    } catch (TimeoutException timeoutException) {
      subscriber.stopAsync();
    }
    assertEquals(4, messages.size());
    for (Map.Entry<String, PubsubMessage> item : messages.entrySet()) {
      assertEquals(2, item.getValue().getAttributesCount());
    }
  }
}
