/*
 * Copyright 2020 Google LLC
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

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.util.concurrent.ListenableFutureCallback;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
public class PubSubApplication {

  private static final Log LOGGER = LogFactory.getLog(PubSubApplication.class);
  private static final Random rand = new Random(2020);

  public static void main(String[] args) {
    SpringApplication.run(PubSubApplication.class, args);
  }

  // [START pubsub_spring_inbound_channel_adapter]
  // Create a message channel for messages arriving from the subscription `sub-one`.
  @Bean
  public MessageChannel inputMessageChannel() {
    return new PublishSubscribeChannel();
  }

  // Create an inbound channel adapter to listen to the subscription `sub-one` and send
  // messages to the input message channel.
  @Bean
  public PubSubInboundChannelAdapter inboundChannelAdapter(
      @Qualifier("inputMessageChannel") MessageChannel messageChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, "sub-one");
    adapter.setOutputChannel(messageChannel);
    adapter.setAckMode(AckMode.MANUAL);
    adapter.setPayloadType(String.class);
    return adapter;
  }

  // Define what happens to the messages arriving in the message channel.
  @ServiceActivator(inputChannel = "inputMessageChannel")
  public void messageReceiver(
      String payload,
      @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
    LOGGER.info("Message arrived via an inbound channel adapter from sub-one! Payload: " + payload);
    message.ack();
  }
  // [END pubsub_spring_inbound_channel_adapter]

  // [START pubsub_spring_outbound_channel_adapter]
  // Create an outbound channel adapter to send messages from the input message channel to the
  // topic `topic-two`.
  @Bean
  @ServiceActivator(inputChannel = "inputMessageChannel")
  public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
    PubSubMessageHandler adapter = new PubSubMessageHandler(pubsubTemplate, "topic-two");

    adapter.setPublishCallback(
        new ListenableFutureCallback<String>() {
          @Override
          public void onFailure(Throwable throwable) {
            LOGGER.info("There was an error sending the message.");
          }

          @Override
          public void onSuccess(String result) {
            LOGGER.info("Message was sent via the outbound channel adapter to topic-two!");
          }
        });
    return adapter;
  }
  // [END pubsub_spring_outbound_channel_adapter]

  // [START pubsub_spring_cloud_stream_input_binder]
  // Create an input binder to receive messages from `topic-two` using a Consumer bean.
  @Bean
  public Consumer<Message<String>> receiveMessageFromTopicTwo() {
    return message -> {
      LOGGER.info(
          "Message arrived via an input binder from topic-two! Payload: " + message.getPayload());
    };
  }
  // [END pubsub_spring_cloud_stream_input_binder]

  // [START pubsub_spring_cloud_stream_output_binder]
  // Create an output binder to send messages to `topic-one` using a Supplier bean.
  @Bean
  public Supplier<Flux<Message<String>>> sendMessageToTopicOne() {
    return () ->
        Flux.<Message<String>>generate(
            sink -> {
              try {
                Thread.sleep(10000);
              } catch (InterruptedException e) {
                // stop sleep earlier.
              }

              Message<String> message =
                  MessageBuilder.withPayload("message-" + rand.nextInt(1000)).build();
              LOGGER.info(
                  "Sending a message via the output binder to topic-one! Payload: "
                      + message.getPayload());
              sink.next(message);
            })
            .subscribeOn(Schedulers.elastic());
  }
  // [END pubsub_spring_cloud_stream_output_binder]
}
