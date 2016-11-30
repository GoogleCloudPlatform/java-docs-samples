package com.example.appengine.pubsub;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.google.cloud.pubsub.PubSub;
import com.google.cloud.pubsub.PubSubOptions;
import com.google.cloud.pubsub.ReceivedMessage;
import com.google.cloud.pubsub.SubscriptionInfo;
import com.google.cloud.pubsub.Topic;
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
    MockitoAnnotations.initMocks(this);

    //  Set up some fake HTTP requests
    when(mockRequest.getRequestURI()).thenReturn(FAKE_URL);

    // Create an instance of the PubSubHome servlet
    servletUnderTest = new PubSubPublish();

    // Create a Topic with pull subscription
    pubsub = PubSubOptions.getDefaultInstance().getService();
    TopicInfo topicInfo = TopicInfo.newBuilder(topicName).build();
    Topic topic = pubsub.create(topicInfo);
    SubscriptionInfo subscriptionInfo = SubscriptionInfo.newBuilder(
        topicName, subscriptionName).build();
    pubsub.create(subscriptionInfo);
  }

  @After
  public void tearDown() throws Exception {
    // Delete Topic
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
    SubscriptionInfo subscrptionInfo = SubscriptionInfo.of(topicName,
        subscriptionName);
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