/*
 * Copyright 2019 Google LLC
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

package com.example.containeranalysis;

// [START containeranalysis_pubsub]
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import io.grpc.StatusRuntimeException;
import java.io.IOException;
import java.lang.InterruptedException;
import java.util.concurrent.TimeUnit;

public class Subscriptions {
  // Handle incoming Occurrences using a Cloud Pub/Sub subscription
  public static int pubSub(String subId, long timeoutSeconds, String projectId)
      throws InterruptedException {
    // String subId = "my-occurrence-subscription";
    // long timeoutSeconds = 20;
    // String projectId = "my-project-id";
    Subscriber subscriber = null;
    MessageReceiverExample receiver = new MessageReceiverExample();

    try {
      // Subscribe to the requested Pub/Sub channel
      ProjectSubscriptionName subName = ProjectSubscriptionName.of(projectId, subId);
      subscriber = Subscriber.newBuilder(subName, receiver).build();
      subscriber.startAsync().awaitRunning();
      // Sleep to listen for messages
      TimeUnit.SECONDS.sleep(timeoutSeconds);
    } finally {
      // Stop listening to the channel
      if (subscriber != null) {
        subscriber.stopAsync();
      }
    }
    // Print and return the number of Pub/Sub messages received
    System.out.println(receiver.messageCount);
    return receiver.messageCount;
  }

  // Custom class to handle incoming Pub/Sub messages
  // In this case, the class will simply log and count each message as it comes in
  static class MessageReceiverExample implements MessageReceiver {
    public int messageCount = 0;

    @Override
    public synchronized void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
      // Every time a Pub/Sub message comes in, print it and count it
      System.out.println("Message " + messageCount + ": " + message.getData().toStringUtf8());
      messageCount += 1;
      // Acknowledge the message
      consumer.ack();
    }
  }

  // Creates and returns a Pub/Sub subscription object listening to the Occurrence topic
  public static Subscription createOccurrenceSubscription(String subId, String projectId) 
      throws IOException, StatusRuntimeException, InterruptedException {
    // This topic id will automatically receive messages when Occurrences are added or modified
    String topicId = "container-analysis-occurrences-v1";
    ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
    ProjectSubscriptionName subName = ProjectSubscriptionName.of(projectId, subId);

    SubscriptionAdminClient client = SubscriptionAdminClient.create();
    PushConfig config = PushConfig.getDefaultInstance();
    Subscription sub = client.createSubscription(subName, topicName, config, 0);
    return sub;
  }
}
// [END containeranalysis_pubsub]
