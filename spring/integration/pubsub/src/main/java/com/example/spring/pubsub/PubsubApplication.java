package com.example.spring.pubsub;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.Topic;
import com.google.pubsub.v1.TopicName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.pubsub.PubsubAdmin;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@SpringBootApplication
@RestController
public class PubsubApplication {

  private static final Log LOGGER = LogFactory.getLog(PubsubApplication.class);

  @Autowired
  private PubsubOutboundGateway messagingGateway;

  @Autowired
  private PubsubAdmin admin;

  public static void main(String[] args) throws IOException {
    SpringApplication.run(PubsubApplication.class, args);
  }

  @GetMapping("/listTopics")
  public List<String> listTopics() {
    return admin.listTopics().stream()
        .map(Topic::getNameAsTopicName)
        .map(TopicName::getTopic)
        .collect(Collectors.toList());
  }

  @GetMapping("/listSubscriptions")
  public List<String> listSubscriptions() {
    return admin.listSubscriptions().stream()
        .map(Subscription::getNameAsSubscriptionName)
        .map(SubscriptionName::getSubscription)
        .collect(Collectors.toList());
  }

  @PostMapping("/postMessage")
  public RedirectView addMessage(@RequestParam("message") String message) {
    messagingGateway.sendToPubsub(message);
    return new RedirectView("/");
  }

  @PostMapping("/newTopic")
  public RedirectView newTopic(@RequestParam("name") String topicName) {
    admin.createTopic(topicName);
    return new RedirectView("/");
  }

  @PostMapping("/newSubscription")
  public RedirectView newSubscription(@RequestParam("name") String subscriptionName,
      @RequestParam("topic") String topicName) {
    admin.createSubscription(subscriptionName, topicName);
    return new RedirectView("/");
  }

  @Bean
  public MessageChannel pubsubInputChannel() {
    return new PublishSubscribeChannel();
  }

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

  @Bean
  @ServiceActivator(inputChannel = "pubsubInputChannel")
  public MessageHandler messageReceiver1() {
    return message -> {
      LOGGER.info("Message arrived! Payload: "
          + ((ByteString) message.getPayload()).toStringUtf8());
      AckReplyConsumer consumer = (AckReplyConsumer) message.getHeaders().get(
          GcpHeaders.ACKNOWLEDGEMENT);
      consumer.ack();
    };
  }

  @Bean
  @ServiceActivator(inputChannel = "pubsubInputChannel")
  public MessageHandler messageReceiver2() {
    return message -> {
      LOGGER.info("Message also arrived here! Payload: "
          + ((ByteString) message.getPayload()).toStringUtf8());
      AckReplyConsumer consumer = (AckReplyConsumer) message.getHeaders().get(
          GcpHeaders.ACKNOWLEDGEMENT);
      consumer.ack();
    };
  }

  @Bean
  @ServiceActivator(inputChannel = "pubsubOutputChannel")
  public MessageHandler messageSender(PubsubTemplate pubsubTemplate) {
    PubsubMessageHandler outboundAdapter = new PubsubMessageHandler(pubsubTemplate);
    outboundAdapter.setTopic("test");
    return outboundAdapter;
  }

  @MessagingGateway(defaultRequestChannel = "pubsubOutputChannel")
  public interface PubsubOutboundGateway {

    void sendToPubsub(String text);
  }
}
