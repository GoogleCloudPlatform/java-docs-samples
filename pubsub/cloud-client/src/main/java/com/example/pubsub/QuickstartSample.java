/*
  Copyright 2017, Google, Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package com.example.pubsub;

// [START pubsub_quickstart]
// Imports the Google Cloud client library

import com.google.api.gax.core.RpcFuture;
import com.google.cloud.pubsub.spi.v1.Publisher;
import com.google.cloud.pubsub.spi.v1.PublisherClient;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

public class QuickstartSample {

  public static void main(String... args) throws Exception {

    // Create a new topic
    String projectId = args[0];
    TopicName topic = TopicName.create(projectId, "my-new-topic");
    try (PublisherClient publisherClient = PublisherClient.create()) {
      publisherClient.createTopic(topic);
    }
    System.out.printf("Topic %s:%s created.\n", topic.getProject(), topic.getTopic());

    // Creates a publisher
    Publisher publisher = null;
    try {
      publisher = Publisher.newBuilder(topic).build();

      //Publish a message asynchronously
      String message = "my-message";
      ByteString data = ByteString.copyFromUtf8(message);
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
      RpcFuture<String> messageIdFuture = publisher.publish(pubsubMessage);

      //Print message id of published message
      System.out.println("published with message ID: " + messageIdFuture.get());
    } finally {
      if (publisher != null) {
        publisher.shutdown();
      }
    }
  }
}
// [END pubsub_quickstart]
