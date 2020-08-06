package demo;

import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.PollableBean;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.PubSubHeaderMapper;
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

  public static void main(String[] args) {
    SpringApplication.run(PubSubApplication.class, args);
  }

  // [START pubsub_spring_inbound_channel_adapter]
  @Bean
  public MessageChannel messageChannelToForwardYourSubscription() {
    return new PublishSubscribeChannel();
  }

  @Bean
  public PubSubInboundChannelAdapter inboundChannelAdapter(
      @Qualifier("messageChannelToForwardYourSubscription") MessageChannel messageChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, "six");
    adapter.setOutputChannel(messageChannel);
    adapter.setAckMode(AckMode.MANUAL);
    adapter.setPayloadType(String.class);
    return adapter;
  }

  @ServiceActivator(inputChannel = "messageChannelToForwardYourSubscription")
  public void messageReceiver(String payload,
      @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
    LOGGER.info("Message arrived (via an input channel adapter from six)! Payload: " + payload);
    message.ack();
  }
  // [END pubsub_spring_inbound_channel_adapter]

  // [START pubsub_spring_outbound_channel_adapter]
  @Bean
  @ServiceActivator(inputChannel = "messageChannelToForwardYourSubscription")
  public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
    PubSubMessageHandler adapter =
        new PubSubMessageHandler(pubsubTemplate, "july");

    adapter.setPublishCallback(new ListenableFutureCallback<String>() {
      @Override
      public void onFailure(Throwable ex) {
        LOGGER.info("There was an error sending the message.");
      }

      @Override
      public void onSuccess(String result) {
        LOGGER.info("Message was sent via the output channel adapter to july successfully.");
      }
    });

    PubSubHeaderMapper headerMapper = new PubSubHeaderMapper();
    headerMapper.setOutboundHeaderPatterns("!foo");
    adapter.setHeaderMapper(headerMapper);

    return adapter;
  }
  // [END pubsub_spring_outbound_channel_adapter]

  // [START pubsub_spring_input_binder]
  @Bean
  public Consumer<Message<String>> receiveMessageFromPubSub() {
    return msg -> {
      LOGGER.info("Message arrived (via an input binder from july)! Payload: " + msg.getPayload());
    };
  }
  // [END pubsub_spring_input_binder]

  // [START pubsub_spring_output_binder]
  @Bean
  public Supplier<Message<String>> sendMessageToPubSub() {

    // Method A.
    // return () -> Flux.fromStream(Stream.generate(new Supplier<Message<String>>() {
    //   @Override
    //   public Message<String> get() {
    //     try {
    //       Thread.sleep(10000);
    //     } catch (InterruptedException e) {
    //       e.printStackTrace();
    //     }
    //     Random rand = new Random();
    //       Message<String> message = MessageBuilder
    //           .withPayload(Integer.toString(rand.nextInt(1000)))
    //           .setHeader("key", "imkey")
    //           .build();
    //       return message;
    //   }
    // })).subscribeOn(Schedulers.elastic()).share();

    // Method B.
    return () -> {
        Random rand = new Random();
          Message<String> message = MessageBuilder
              .withPayload(Integer.toString(rand.nextInt(1000)))
              .setHeader("key", "imkey")
              .build();
          return message;
    };
  }
  // [END pubsub_spring_output_binder]
}
