/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import com.google.cloud.pubsub.spi.v1.AckReplyConsumer;
import com.google.cloud.pubsub.spi.v1.MessageReceiver;
import com.google.cloud.pubsub.spi.v1.Subscriber;
import com.google.common.collect.ImmutableList;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.SubscriptionName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class SubscriberExample {

  // use the default project id
  private static final String PROJECT_ID = ServiceOptions.getDefaultProjectId();

  private static final BlockingQueue<PubsubMessage> messages = new LinkedBlockingDeque<>();

  private final List<String> receivedMessageIds = new ArrayList<>();

  static class MessageReceiverExample implements MessageReceiver {
    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
      messages.offer(message);
      consumer.ack();
    }
  }

  List<String> getReceivedMessages() {
    return ImmutableList.copyOf(receivedMessageIds);
  }

  public static void main(String... args) throws Exception {
    // set subscriber id, eg. my-subscriber-id
    String subscriptionId = args[0];
    SubscriptionName subscriptionName = SubscriptionName.create(PROJECT_ID, subscriptionId);
    Subscriber subscriber = null;
    try {
      // create a subscriber bound to the asynchronous message receiver
      subscriber = Subscriber.defaultBuilder(subscriptionName, new MessageReceiverExample()).build();
      subscriber.startAsync().awaitRunning();
      // Continue to listen to messages
      while (true) {
        PubsubMessage message = messages.take();
        System.out.println("Message Id: " + message.getMessageId());
        System.out.println("Data: " + message.getData().toStringUtf8());
      }
    } finally {
      if (subscriber != null) {
        subscriber.stopAsync();
      }
    }
  }
}
// [END pubsub_quickstart_subscriber]
