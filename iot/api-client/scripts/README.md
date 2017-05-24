# Getting Started with Cloud Pub/Sub and the Google Cloud Client libraries

[Google Cloud IoT Core](https://cloud.google.com/iot-core/)
is a fully-managed, globally distributed solution for managing devices and
sending / receiving messages from devices.

This script manages the [Google Cloud Pub/Sub][pubsub] project associated with
your Google Cloud IoT Core project to grant permissions to the protocol bridge.

Create your PubSub topic noting the project ID and topic ID, then build and run
the sample to configure your topic.

[pubsub]: https://cloud.google.com/pubsub/

#### Setup

* Install [Maven](http://maven.apache.org/)
* Build your project with:

    mvn clean compile assembly:single

#### Running the script

The following code will run the helper script:

    java -cp target/pubsub-policy-helper-1.0.0-jar-with-dependencies.jar \
        com.example.pubsub.AddCloudIotService <topicName> <projectId>

For example, the following example will configure the `device-events` topic
for the `my-iot-project` project.

    java -cp target/pubsub-policy-helper-1.0.0-jar-with-dependencies.jar \
        com.example.pubsub.AddCloudIotService device-events my-iot-project
