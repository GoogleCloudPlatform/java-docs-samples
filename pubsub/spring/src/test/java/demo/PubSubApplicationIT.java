/*
 * Copyright 2021 Google LLC
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

package demo;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.TopicName;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class PubSubApplicationIT {
  private ByteArrayOutputStream bout;
  private PrintStream out;

  private static final String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String topicOneId = "topic-one";
  private static final String topicTwoId = "topic-two";
  private static final String subscriptionOneId = "sub-one";
  private static final String subscriptionTwoId = "sub-two";

  private static void requireEnvVar(String varName) {
    assertNotNull(
        "Environment variable " + varName + " is required to perform these tests.",
        System.getenv(varName));
  }

  @Rule public Timeout globalTimeout = Timeout.seconds(600); // 10 minute timeout

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void setUp() throws Exception {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      try {
        topicAdminClient.createTopic(TopicName.of(projectId, topicOneId));
        topicAdminClient.createTopic(TopicName.of(projectId, topicTwoId));
      } catch (AlreadyExistsException ignore) {
        System.out.println("Using existing topics.");
      }
    }

    try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
      Subscription subscriptionOne =
          Subscription.newBuilder()
              .setName(String.valueOf(ProjectSubscriptionName.of(projectId, subscriptionOneId)))
              .setTopic(String.valueOf(TopicName.of(projectId, topicOneId)))
              .build();
      Subscription subscriptionTwo =
          Subscription.newBuilder()
              .setName(String.valueOf(ProjectSubscriptionName.of(projectId, subscriptionTwoId)))
              .setTopic(String.valueOf(TopicName.of(projectId, topicTwoId)))
              .build();

      try {
        subscriptionAdminClient.createSubscription(subscriptionOne);
        subscriptionAdminClient.createSubscription(subscriptionTwo);
      } catch (AlreadyExistsException ignore) {
        System.out.println("Using existing subscriptions");
      }
    }
  }

  @After
  public void tearDown() {
    // No need to clean up these pairs of topics and subscriptions.
    System.setOut(null);
  }

  @Test
  public void testPubSubApplication() throws Exception {
    bout.reset();

    demo.PubSubApplication.main(new String[] {});

    assertThat(bout.toString()).contains("Started PubSubApplication");
  }
}
