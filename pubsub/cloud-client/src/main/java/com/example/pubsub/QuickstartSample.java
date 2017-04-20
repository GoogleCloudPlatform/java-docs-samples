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

import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.spi.v1.TopicAdminClient;
import com.google.pubsub.v1.TopicName;

public class QuickstartSample {

  public static void main(String... args) throws Exception {

    // Your Google Cloud Platform project ID
    String projectId = ServiceOptions.getDefaultProjectId();

    // Your topic ID
    String topicId = "my-new-topic";

    // Create a new topic
    TopicName topic = TopicName.create(projectId, topicId);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.createTopic(topic);
    }

    System.out.printf("Topic %s:%s created.\n", topic.getProject(), topic.getTopic());
  }
}
// [END pubsub_quickstart]
