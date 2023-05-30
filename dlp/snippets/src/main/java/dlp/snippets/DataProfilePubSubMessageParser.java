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

package dlp.snippets;

// [START dlp_data_profile_pub_sub_message_parser]

import com.google.api.core.ApiService;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.privacy.dlp.v2.DataProfilePubSubMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DataProfilePubSubMessageParser {

  public static void main(String... args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    String subscriptionId = "your-pubsub-subscription-id";
    parseMessage(projectId, subscriptionId);
  }

  // Parses messages received from a Google Cloud Pub/Sub subscription into a
  // DataProfilePubSubMessage object.
  public static void parseMessage(String projectId, String subscriptionId)throws TimeoutException {
    int timeoutSeconds = 5;

    // The `ProjectSubscriptionName.of` method creates a fully qualified identifier
    // in the form `projects/{projectId}/subscriptions/{subscriptionId}`.
    ProjectSubscriptionName subscriptionName =
        ProjectSubscriptionName.of(projectId, subscriptionId);

    MessageReceiver receiver =
        (PubsubMessage pubsubMessage, AckReplyConsumer consumer) -> {
          try {
            DataProfilePubSubMessage message =
                DataProfilePubSubMessage.parseFrom(pubsubMessage.getData());
            System.out.println(
                "PubsubMessage with ID: "
                    + pubsubMessage.getMessageId()
                    + "; message size: "
                    + pubsubMessage.getData().size()
                    + "; event: "
                    + message.getEvent()
                    + "; profile name: "
                    + message.getProfile().getName()
                    + "; full resource: "
                    + message.getProfile().getFullResource());
            consumer.ack();
          } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
          }
        };

    // Create subscriber client.
    Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
    try {
      ApiService apiService = subscriber.startAsync();
      apiService.awaitRunning();
      System.out.printf(
          "Listening for messages on %s for %d seconds.%n", subscriptionName, timeoutSeconds);
      subscriber.awaitTerminated(timeoutSeconds, TimeUnit.SECONDS);
    }  finally {
      subscriber.stopAsync();
    }
  }
}

// [END dlp_data_profile_pub_sub_message_parser]
