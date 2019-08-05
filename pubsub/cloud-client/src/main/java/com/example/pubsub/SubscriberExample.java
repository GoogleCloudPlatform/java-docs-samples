/*
 * Copyright 2017 Google Inc.
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

// [START pubsub_quickstart_subscriber]

import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class SubscriberExample {
  // use the default project id
  private static final String PROJECT_ID = ServiceOptions.getDefaultProjectId();

  // The amount of time to wait to offer a message to the queue.
  private static final long QUEUE_OFFER_TIMEOUT_SECONDS = 5;

  private static final BlockingQueue<MessageAndConsumer> queue = new LinkedBlockingDeque<>();

  // A POD object to hold a message and the consumer associated with it.
  static class MessageAndConsumer {
    PubsubMessage message;
    AckReplyConsumer consumer;

    MessageAndConsumer(PubsubMessage message, AckReplyConsumer consumer) {
      this.message = message;
      this.consumer = consumer;
    }
  }

  static class MessageReceiverExample implements MessageReceiver {

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
      // Attempt to add the message to the queue for processing. If we can't add it within
      // QUEUE_OFFER_TIMEOUT_SECONDS, then nack it and allow it to be redelivered (to this client
      // or to another one that might be running and have more capacity)
      try {
        if (!queue.offer(
            new MessageAndConsumer(message, consumer),
            QUEUE_OFFER_TIMEOUT_SECONDS,
            TimeUnit.SECONDS)) {
          consumer.nack();
        }
      } catch (InterruptedException e) {
        System.out.println("Could not add message to queue: " + e);
        consumer.nack();
      }
    }
  }

  /** Receive messages over a subscription. */
  public static void main(String... args) throws Exception {
    // set subscriber id, eg. my-sub
    String subscriptionId = args[0];
    ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(
        PROJECT_ID, subscriptionId);
    Subscriber subscriber = null;
    try {
      // create a subscriber bound to the asynchronous message receiver
      subscriber =
          Subscriber.newBuilder(subscriptionName, new MessageReceiverExample()).build();
      subscriber.startAsync().awaitRunning();
      // Continue to listen to queue
      while (true) {
        MessageAndConsumer messageAndConsumer = queue.take();
        System.out.println("Message Id: " + messageAndConsumer.message.getMessageId());
        System.out.println("Data: " + messageAndConsumer.message.getData().toStringUtf8());
        // Ack only once all work with the message is complete.
        messageAndConsumer.consumer.ack();
      }
    } finally {
      if (subscriber != null) {
        subscriber.stopAsync();
      }
    }
  }
}
// [END pubsub_quickstart_subscriber]
