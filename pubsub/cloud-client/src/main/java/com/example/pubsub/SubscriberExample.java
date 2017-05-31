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

public class SubscriberExample implements Runnable {
  // use the default project id
  private static final String PROJECT_ID = ServiceOptions.getDefaultProjectId();

  private final BlockingQueue<PubsubMessage> messages = new LinkedBlockingDeque<>();

  private final List<String> receivedMessageIds = new ArrayList<>();

  private final String subscriptionId;

  private volatile boolean listen = true;

  public SubscriberExample(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  @Override
  public void run() {
    MessageReceiver receiver =
        new MessageReceiver() {
          @Override
          public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
            messages.offer(message);
            consumer.ack();
          }
        };
    SubscriptionName subscriptionName = SubscriptionName.create(PROJECT_ID, subscriptionId);
    Subscriber subscriber = null;
    try {
      // create a subscriber bound to the asynchronous message receiver
      subscriber = Subscriber.defaultBuilder(subscriptionName, receiver).build();
      subscriber.startAsync().awaitRunning();
      // continue to wait on received messages, Ctrl-C to exit
      while (listen) {
        // block on receiving a message
        PubsubMessage message = messages.take();
        System.out.println("Message Id: " + message.getMessageId());
        System.out.println("Data: " + message.getData().toStringUtf8());
        receivedMessageIds.add(message.getMessageId());
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      if (subscriber != null) {
        subscriber.stopAsync();
      }
    }
  }

  void stopSubscriber() {
    listen = false;
  }

  List<String> getReceivedMessages() {
    return ImmutableList.copyOf(receivedMessageIds);
  }

  public static void main(String... args) throws Exception {
    // set subscriber id, eg. my-subscriber-id
    String subscriberId = args[0];
    SubscriberExample subscriber = new SubscriberExample(subscriberId);
    Thread t = new Thread(subscriber);
    t.start();
    // Stop subscriber after 5 minutes of listening
    Thread.sleep(5 * 60000);
    subscriber.stopSubscriber();
    t.join();
  }
}
// [END pubsub_quickstart_subscriber]
