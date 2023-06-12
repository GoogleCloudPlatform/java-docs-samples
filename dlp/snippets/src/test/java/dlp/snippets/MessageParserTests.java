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

import static com.google.common.truth.Truth.assertThat;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.common.collect.ImmutableList;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MessageParserTests extends TestBase {
  private static final UUID testRunUuid = UUID.randomUUID();
  private static final TopicName topicName =
      TopicName.of(PROJECT_ID, String.format("%s-%s", TOPIC_ID, testRunUuid));
  private static final SubscriptionName subscriptionName =
      SubscriptionName.of(PROJECT_ID, String.format("%s-%s", SUBSCRIPTION_ID, testRunUuid));

  @Override
  protected ImmutableList<String> requiredEnvVars() {
    return ImmutableList.of(
        "GOOGLE_APPLICATION_CREDENTIALS",
        "GOOGLE_CLOUD_PROJECT",
        "PUB_SUB_TOPIC",
        "PUB_SUB_SUBSCRIPTION");
  }

  @BeforeClass
  public static void before() throws Exception {
    // Create a new topic
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.createTopic(topicName);
    }

    // Create a new subscription
    try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
      subscriptionAdminClient.createSubscription(
          subscriptionName, topicName, PushConfig.getDefaultInstance(), 0);
    }
  }

  @AfterClass
  public static void after() throws Exception {
    // Delete the test topic
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topicName);
    } catch (ApiException e) {
      System.err.println(String.format("Error deleting topic %s: %s", topicName.getTopic(), e));
      // Keep trying to clean up
    }

    // Delete the test subscription
    try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
      subscriptionAdminClient.deleteSubscription(subscriptionName);
    } catch (ApiException e) {
      System.err.println(
          String.format(
              "Error deleting subscription %s: %s", subscriptionName.getSubscription(), e));
      // Keep trying to clean up
    }
  }

  @Test
  public void testDataProfilePubSubMessageParser() throws TimeoutException {
    DataProfilePubSubMessageParser.parseMessage(PROJECT_ID, subscriptionName.getSubscription());
    String output = bout.toString();
    ProjectSubscriptionName subscriptionName =
        ProjectSubscriptionName.of(PROJECT_ID, SUBSCRIPTION_ID);
    assertThat(output).contains("Listening for messages on " + subscriptionName);
  }
}
