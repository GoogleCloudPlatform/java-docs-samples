/**
 * Copyright 2017 Google Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.flexible.pubsub;

import com.google.api.gax.grpc.ApiException;
import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.spi.v1.Publisher;
import com.google.cloud.pubsub.spi.v1.SubscriptionAdminClient;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.*;
import io.grpc.Status;
import java.util.logging.Logger;

public class PubSubService {

  private Publisher publisher;
  private Logger logger = Logger.getLogger(this.getClass().getName());

  /** Create a lazy initialized singleton of PubSubService */
  private static class LazyInit {
    private static final PubSubService INSTANCE;

    static {
      System.out.println("instance");
      INSTANCE = new PubSubService();
    }
  }

  public static PubSubService getInstance() {
    return LazyInit.INSTANCE;
  }

  /**
   * Construct the push endpoint URL on appspot.com with verification token
   *
   * @return push endpoint
   */
  private static String getPushEndPoint() {
    return "https://"
        + getProjectId()
        + ".appspot.com/pubsub/push?token="
        + getPushVerificationToken();
  }

  private static String getPushVerificationToken() {
    return System.getenv("PUBSUB_VERIFICATION_TOKEN");
  }

  /**
   * Initialize the pubsub service : - create a publisher on a topic and add a subscriber with push
   * endpoint for the topic
   */
  private PubSubService() {
    try {
      String topicId = System.getenv("PUBSUB_TOPIC");
      String endpoint = getPushEndPoint();
      String subscriptionId = System.getenv("PUBSUB_SUBSCRIPTION_ID");

      this.publisher = Publisher.newBuilder(getTopicName(topicId)).build();
      addPushEndPoint(topicId, subscriptionId, endpoint);
    } catch (Exception e) {
      logger.severe(e.toString());
    }
  }

  /**
   * Publish a message on configured topic
   *
   * @param message String
   * @throws Exception
   */
  public void publish(String message) throws Exception {
    PubsubMessage pubsubMessage =
        PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(message)).build();
    publisher.publish(pubsubMessage);
  }

  private TopicName getTopicName(String topicId) {
    return TopicName.create(getProjectId(), topicId);
  }

  private SubscriptionName getSubscriptionName(String subscriptionId) {
    return SubscriptionName.create(getProjectId(), subscriptionId);
  }

  private static String getProjectId() {
    return ServiceOptions.getDefaultProjectId();
  }

  /**
   * Creates/modifies subscription with push configuration on topic
   *
   * @param topicId topic-id
   * @param subscriptionId subscription-id
   * @param endpoint push endpoint URL
   * @throws Exception
   */
  private void addPushEndPoint(String topicId, String subscriptionId, String endpoint)
      throws Exception {
    PushConfig pushConfig = PushConfig.newBuilder().setPushEndpoint(endpoint).build();
    SubscriptionName subscriptionName = getSubscriptionName(subscriptionId);
    try (SubscriptionAdminClient subscriberAdminClient = SubscriptionAdminClient.create()) {
      try {
        subscriberAdminClient.createSubscription(
            subscriptionName, getTopicName(topicId), pushConfig, 3);
      } catch (ApiException e) {
        // modify push config for existing subscription
        if (e.getStatusCode().toStatus().equals(Status.ALREADY_EXISTS)) {
          subscriberAdminClient.modifyPushConfig(subscriptionName, pushConfig);
          return;
        }
        // otherwise, re-throw
        throw e;
      }
    }
  }
}
