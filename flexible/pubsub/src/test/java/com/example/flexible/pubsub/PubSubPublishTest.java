/**
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.example.flexible.pubsub;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.google.cloud.pubsub.PubSub;
import com.google.cloud.pubsub.PubSubOptions;
import com.google.cloud.pubsub.ReceivedMessage;
import com.google.cloud.pubsub.SubscriptionInfo;
import com.google.cloud.pubsub.TopicInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class PubSubPublishTest {
  private static final String FAKE_URL = "fakeurl.google";
  private static final String topicName = System.getenv("PUBSUB_TOPIC");
  private static final String subscriptionName = System.getenv(
      "PUBSUB_SUBSCRIPTION");
  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  PubSub pubsub;
  private PubSubPublish servletUnderTest;

  @Before
  public void setUp() throws Exception {
    // Initialize Mockito
    MockitoAnnotations.initMocks(this);

    //  Set up some fake HTTP requests
    when(mockRequest.getRequestURI()).thenReturn(FAKE_URL);

    // Create an instance of the PubSubHome servlet
    servletUnderTest = new PubSubPublish();

    // Create a Topic with pull subscription
    pubsub = PubSubOptions.getDefaultInstance().getService();
    TopicInfo topicInfo = TopicInfo.newBuilder(topicName).build();
    pubsub.create(topicInfo);

    // Pull subscription
    SubscriptionInfo subscriptionInfo = SubscriptionInfo.newBuilder(
        topicName, subscriptionName).build();
    pubsub.create(subscriptionInfo);
  }

  @After
  public void tearDown() throws Exception {
    // Delete Topic and Subscription
    pubsub.deleteSubscription(subscriptionName);
    pubsub.deleteTopic(topicName);
  }

  @Test
  public void doPostIT() throws Exception {
    // Dummy payload
    final String dummyPayload = "MessageToPost";

    // Set payload data for request
    when(mockRequest.getParameter("payload")).thenReturn(dummyPayload);

    // Do POST
    servletUnderTest.doPost(mockRequest, mockResponse);

    // Pull using subscription to verify publish
    Iterator<ReceivedMessage> messages = pubsub.pull(subscriptionName, 1);

    // Check message payload is dummyPayload
    if (messages.hasNext()) {
      ReceivedMessage message = messages.next();
      assertThat(message.getPayloadAsString())
          .named("Publish Message")
          .contains(dummyPayload);
      message.ack();
    }
  }
}