/*
 *  Copyright 2017 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.spring.pubsub;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.protobuf.ByteString;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.pubsub.core.PubsubTemplate;
import org.springframework.cloud.gcp.pubsub.support.GcpHeaders;
import org.springframework.cloud.gcp.pubsub.support.SubscriberFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.gcp.AckMode;
import org.springframework.integration.gcp.inbound.PubsubInboundChannelAdapter;
import org.springframework.integration.gcp.outbound.PubsubMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@SpringBootApplication
public class PubsubApplication {

  private static final Log LOGGER = LogFactory.getLog(PubsubApplication.class);

  public static void main(String[] args) throws IOException {
    SpringApplication.run(PubsubApplication.class, args);
  }

  // Inbound channel adapter.

  /**
   * Spring channel for incoming messages from Google Cloud Pub/Sub.
   *
   * <p>We use a {@link PublishSubscribeChannel} which broadcasts messages to every subscriber. In
   * this case, every service activator.
   */
  @Bean
  public MessageChannel pubsubInputChannel() {
    return new PublishSubscribeChannel();
  }

  /**
   * Inbound channel adapter that gets activated whenever a new message arrives at a Google Cloud
   * Pub/Sub subscription.
   *
   * <p>Messages get posted to the specified input channel, which activates the service activators
   * below.
   *
   * @param inputChannel Spring channel that receives messages and triggers attached service
   * activators
   * @param subscriberFactory creates the subscriber that listens to messages from Google Cloud
   * Pub/Sub
   * @return the inbound channel adapter for a Google Cloud Pub/Sub subscription
   */
  @Bean
  public PubsubInboundChannelAdapter messageChannelAdapter(
      @Qualifier("pubsubInputChannel") MessageChannel inputChannel,
      SubscriberFactory subscriberFactory) {
    PubsubInboundChannelAdapter adapter =
        new PubsubInboundChannelAdapter(subscriberFactory, "messages");
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);

    return adapter;
  }

  /**
   * Message handler that gets triggered whenever a new message arrives at the attached Spring
   * channel.
   *
   * <p>Just logs the received message. Message acknowledgement mode set to manual above, so the
   * consumer that allows us to (n)ack is extracted from the message headers and used to ack.
   */
  @Bean
  @ServiceActivator(inputChannel = "pubsubInputChannel")
  public MessageHandler messageReceiver1() {
    return message -> {
      LOGGER.info("Message arrived! Payload: "
          + ((ByteString) message.getPayload()).toStringUtf8());
      AckReplyConsumer consumer =
          (AckReplyConsumer) message.getHeaders().get(GcpHeaders.ACKNOWLEDGEMENT);
      consumer.ack();
    };
  }

  /**
   * Second message handler that also gets messages from the same subscription as above.
   */
  @Bean
  @ServiceActivator(inputChannel = "pubsubInputChannel")
  public MessageHandler messageReceiver2() {
    return message -> {
      LOGGER.info("Message also arrived here! Payload: "
          + ((ByteString) message.getPayload()).toStringUtf8());
      AckReplyConsumer consumer =
          (AckReplyConsumer) message.getHeaders().get(GcpHeaders.ACKNOWLEDGEMENT);
      consumer.ack();
    };
  }

  // Outbound channel adapter

  /**
   * The outbound channel adapter to write messages from a Spring channel to a Google Cloud Pub/Sub
   * topic.
   *
   * @param pubsubTemplate Spring abstraction to send messages to Google Cloud Pub/Sub topics
   */
  @Bean
  @ServiceActivator(inputChannel = "pubsubOutputChannel")
  public MessageHandler messageSender(PubsubTemplate pubsubTemplate) {
    PubsubMessageHandler outboundAdapter = new PubsubMessageHandler(pubsubTemplate);
    outboundAdapter.setTopic("test");
    return outboundAdapter;
  }

  /**
   * A Spring mechanism to write messages to a channel.
   */
  @MessagingGateway(defaultRequestChannel = "pubsubOutputChannel")
  public interface PubsubOutboundGateway {

    void sendToPubsub(String text);
  }
}
