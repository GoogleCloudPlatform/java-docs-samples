/*
  Copyright 2016, Google, Inc.

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
import com.google.cloud.pubsub.PubSub;
import com.google.cloud.pubsub.PubSubOptions;
import com.google.cloud.pubsub.Topic;
import com.google.cloud.pubsub.TopicInfo;

public class QuickstartSample {
  public static void main(String... args) throws Exception {
    // Instantiates a client
    PubSub pubsub = PubSubOptions.defaultInstance().service();
    // The name for the new topic
    String topicName = "my-new-topic";
    // Creates the new topic
    Topic topic = pubsub.create(TopicInfo.of(topicName));
    System.out.printf("Topic %s created.%n", topic.name());
  }
}
// [END pubsub_quickstart]
