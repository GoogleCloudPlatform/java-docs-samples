# Getting started with Spring Integration channel adapters for Google Cloud Pub/Sub

This is a sample application that uses Spring Integration and Spring Boot to read and write messages
to Google Cloud Pub/Sub.

PubsubApplication is a typical Spring Boot application. We declare all the necessary beans for the
application to work in the `PubsubApplication` class. The most important ones are the inbound and
outbound channel adapters.

## Channel adapters

On Spring Integration, channel adapters are adapters that send or receive messages from external
systems, convert them to/from an internal Spring message representation and read/write them to a
Spring channel, which can then have other components attached to it, such as service activators.

### Inbound channel adapter

PubsubInboundChannelAdapter is Spring Cloud GCP Pub/Sub inbound channel adapter class. It's declared
in the user app as follows:

```
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
```

In the example, we instantiate the `PubsubInboundChannelAdapter` object with a SubscriberFactory and
a Google Cloud Pub/Sub subscription name, from where the adapter listens to messages, and then set
its output channel and ack mode.
 
In apps which use the Spring Cloud GCP Pubsub Boot starter, a SubscriberFactory is automatically
provided. The subscription name (e.g., `"messages"`) is the name of a Google Cloud Pub/Sub
subscription that must already exist when the channel adapter is created.

The input channel is a channel in which messages get into Spring from an external system.
In this example, we use a PublishSubscribeChannel, which broadcasts incoming messages to all its
subscribers, including service activators.

```
@Bean
public MessageChannel pubsubInputChannel() {
  return new PublishSubscribeChannel();
}
```

Setting the acknowledgement mode on the inbound channel adapter is optional. It is set to automatic
by default. If set to manual, messages must be explicitly acknowledged through the
`AckReplyConsumer` object from the Spring message header `GcpHeader.ACKNOWLEDGEMENT`.

```
AckReplyConsumer consumer =
    (AckReplyConsumer) message.getHeaders().get(GcpHeaders.ACKNOWLEDGEMENT);
consumer.ack();
```

A service activator is typically attached to a channel in order to process incoming messages. Here
is an example of a service activator that logs and acknowledges the received message.

```
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
```

### Outbound channel adapter

PubSubMessageHandler is Spring Cloud GCP's Pub/Sub outbound channel adapter. It converts Spring
messages in a channel to an external representation and sends them to a Google Cloud Pub/Sub topic.

```
@Bean
@ServiceActivator(inputChannel = "pubsubOutputChannel")
public MessageHandler messageSender(PubsubTemplate pubsubTemplate) {
  PubsubMessageHandler outboundAdapter = new PubsubMessageHandler(pubsubTemplate);
  outboundAdapter.setTopic("test");
  return outboundAdapter;
}
```

`PubsubTemplate` is Spring Cloud GCP's abstraction to send messages to Google Cloud Pub/Sub. It
contains the logic to create a Google Cloud Pub/Sub `Publisher`, convert Spring messages to Google
Cloud Pub/Sub `PubsubMessage` and publish them to a topic.

`PubsubMessageHandler` requires a `PubsubTemplate` to be instantiated. The Spring Cloud GCP Boot
Pubsub starter provides a pre-configured `PubsubTemplate`, ready to use. `PubsubMessageHandler`
also requires the name of a Google Cloud Pub/Sub topic, which must exist before any messages are
sent.

We use a messaging gateway to write to a Spring channel.

```
@MessagingGateway(defaultRequestChannel = "pubsubOutputChannel")
public interface PubsubOutboundGateway {

  void sendToPubsub(String text);
}
```

Spring auto-generates the output channel, as well as the gateway code and injects it to the local
variable in `WebAppController`.

```
@Autowired
private PubsubOutboundGateway messagingGateway;
```

## Administration

The Spring Cloud GCP Pubsub package provides a Google Cloud Pub/Sub administration utility,
`PubsubAdmin`, to simplify the creation, listing and deletion of Google Cloud Pub/Sub topics and
subscriptions. The Spring Cloud GCP Pubsub starter provides a pre-configured `PubsubAdmin`, based on
an application's properties.

```
@Autowired
private PubsubAdmin admin;
```

## Sample application

This sample application uses Spring Boot and Spring Web to declare a REST controller. The front-end
uses client-side scripting with Angular.

It is exemplified how to:
* Send messages to a Google Cloud Pub/Sub topic through an outbound channel adapter;
* Receive and process messages from a Google Cloud Pub/Sub subscription through an inbound channel
adapter;
* Create new Google Cloud Pub/Sub topics and subscriptions through the Pub/Sub admin utility.
