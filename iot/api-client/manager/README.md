# Cloud IoT Core Java Device Management example

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=iot/api-client/manager/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample app demonstrates device management for Google Cloud IoT Core.

Note that before you can run the sample, you must configure a Google Cloud
PubSub topic for Cloud IoT as described in [the parent README](../README.md).

## Setup
Run the following command to install the libraries and build the sample with
Maven:

mvn clean compile assembly:single

## Running the sample

The following description summarizes the sample usage:

    usage: DeviceRegistryExample [--cloud_region <arg>] --command <arg>
       [--ec_public_key_file <arg>] --project_id <arg> --pubsub_topic
       <arg> --registry_name <arg> [--rsa_certificate_file <arg>]

    Cloud IoT Core Commandline Example (Device / Registry management):

    --cloud_region <arg>           GCP cloud region.
    --command <arg>                Command to run:
                                   create-iot-topic
                                   create-rsa
                                   create-es
                                   create-unauth
                                   create-registry
                                   delete-device
                                   delete-registry
                                   get-device
                                   get-registry
                                   list-devices
                                   list-registries
                                   patch-device-es
                                   patch-device-rsa
    --ec_public_key_file <arg>     Path to ES256 public key file.
    --project_id <arg>             GCP cloud project name.
    --pubsub_topic <arg>           Pub/Sub topic to create registry in.
    --registry_name <arg>          Name for your Device Registry.
    --rsa_certificate_file <arg>   Path to RS256 certificate file.

https://cloud.google.com/iot-core

For example, if your project ID is `blue-jet-123`, your service account
credentials are stored in your home folder in creds.json and you have generated
your credentials using the shell script provided in the parent folder, you can
run the sample as:


## Usage Examples

Create a PubSub topic, `hello-java`, for the project, `blue-jet-123`:

    mvn exec:java \
        -Dexec.mainClass="com.example.cloud.iot.examples.DeviceRegistryExample" \
        -Dexec.args="-project_id=blue-jet-123 \
                     -command=create-iot-topic \
                     -pubsub_topic=hello-java "

Create an ES device:

    java -cp target/cloudiot-manager-demo-1.0-jar-with-dependencies.jar \
        com.example.cloud.iot.examples.DeviceRegistryExample \
        -project_id=blue-jet-123 -pubsub_topic=hello-java \
        -registry_name=hello-java -ec_public_key_file ../ec_public.pem \
        -device_id="java-device-0" -command=create-es

Create an RSA device:

    java -cp target/cloudiot-manager-demo-1.0-jar-with-dependencies.jar \
        com.example.cloud.iot.examples.DeviceRegistryExample \
        -project_id=blue-jet-123 -pubsub_topic=hello-java \
        -registry_name=hello-java -rsa_certificate_file ../rsa_cert.pem \
        -device_id="java-device-1" -command=create-rsa

Create a device without authorization:

    java -cp target/cloudiot-manager-demo-1.0-jar-with-dependencies.jar \
        com.example.cloud.iot.examples.DeviceRegistryExample \
        -project_id=blue-jet-123 -pubsub_topic=hello-java \
        -registry_name=hello-java -device_id="java-device-3" \
        -command=create-unauth

Create a device registry:

    java -cp target/cloudiot-manager-demo-1.0-jar-with-dependencies.jar \
        com.example.cloud.iot.examples.DeviceRegistryExample \
        -project_id=blue-jet-123 -pubsub_topic=hello-java \
        -registry_name=hello-java -command=create-registry

Delete a device registry:

    java -cp target/cloudiot-manager-demo-1.0-jar-with-dependencies.jar \
        com.example.cloud.iot.examples.DeviceRegistryExample \
        -project_id=blue-jet-123 -pubsub_topic=hello-java \
        -registry_name=hello-java -command=delete-registry

Get a device registry:

    java -cp target/cloudiot-manager-demo-1.0-jar-with-dependencies.jar \
        com.example.cloud.iot.examples.DeviceRegistryExample \
        -project_id=blue-jet-123 -pubsub_topic=hello-java \
        -registry_name=hello-java -command=get-registry

List devices:

    java -cp target/cloudiot-manager-demo-1.0-jar-with-dependencies.jar \
        com.example.cloud.iot.examples.DeviceRegistryExample \
        -project_id=blue-jet-123 -pubsub_topic=hello-java \
        -registry_name=hello-java -command=list-devices

List device registries:

    java -cp target/cloudiot-manager-demo-1.0-jar-with-dependencies.jar \
        com.example.cloud.iot.examples.DeviceRegistryExample \
        -project_id=blue-jet-123 -pubsub_topic=hello-java \
        -registry_name=hello-java -command=list-registries

Patch a device with ES:

    java -cp target/cloudiot-manager-demo-1.0-jar-with-dependencies.jar \
        com.example.cloud.iot.examples.DeviceRegistryExample \
        -project_id=blue-jet-123 -pubsub_topic=hello-java \
        -registry_name=hello-java -ec_public_key_file ../ec_public.pem \
        -device_id="java-device-1" -command=patch-device-es

Patch a device with RSA:

    java -cp target/cloudiot-manager-demo-1.0-jar-with-dependencies.jar \
        com.example.cloud.iot.examples.DeviceRegistryExample \
        -project_id=blue-jet-123 -pubsub_topic=hello-java \
        -registry_name=hello-java -rsa_certificate_file ../rsa_cert.pem \
        -device_id="java-device-0" -command=patch-device-rsa


# Cloud IoT Core Java HTTP example

This sample app publishes data to Cloud Pub/Sub using the HTTP bridge provided
as part of Google Cloud IoT Core.

Note that before you can run the sample, you must configure a Google Cloud
PubSub topic for Cloud IoT Core and register a device as described in the
[parent README](../README.md).

## Setup

Run the following command to install the dependencies using Maven:

    mvn clean compile

## Running the sample

The following command summarizes the sample usage:

```
    mvn exec:java \
        -Dexec.mainClass="com.example.cloud.iot.examples.HttpExample" \
        -Dexec.args="-project_id=<your-iot-project> \
                     -registry_id=<your-registry-id> \
                     -device_id=<device-id> \
                     -private_key_file=<path-to-keyfile> \
                     -message_type=<event|state> \
                     -algorithm=<RS256|ES256>"
```

For example, if your project ID is `blue-jet-123`, your service account
credentials are stored in your home folder in creds.json and you have generated
your credentials using the [`generate_keys.sh`](../generate_keys.sh) script
provided in the parent folder, you can run the sample as:

```
    mvn exec:java \
        -Dexec.mainClass="com.example.cloud.iot.examples.HttpExample" \
        -Dexec.args="-project_id=blue-jet-123 \
                     -registry_id=my-registry \
                     -device_id=my-java-device \
                     -private_key_file=../rsa_private_pkcs8 \
                     -algorithm=RS256"
```

To publish state messages, run the sample as follows:

```
    mvn exec:java \
        -Dexec.mainClass="com.example.cloud.iot.examples.HttpExample" \
        -Dexec.args="-project_id=blue-jet-123 \
                     -registry_id=my-registry \
                     -device_id=my-java-device \
                     -private_key_file=../rsa_private_pkcs8 \
                     -message_type=state \
                     -algorithm=RS256"
```


## Reading the messages written by the sample client

1. Create a subscription to your topic.

```
    gcloud beta pubsub subscriptions create \
        projects/your-project-id/subscriptions/my-subscription \
        --topic device-events
```

2. Read messages published to the topic

```
    gcloud beta pubsub subscriptions pull --auto-ack \
        projects/my-iot-project/subscriptions/my-subscription
```

# Cloud IoT Core Java MQTT example

This sample app publishes data to Cloud Pub/Sub using the MQTT bridge provided
as part of Google Cloud IoT Core.

Note that before you can run the sample, you must configure a Google Cloud
PubSub topic for Cloud IoT Core and register a device as described in the
[parent README](../README.md).

## Setup

Run the following command to install the dependencies using Maven:

    mvn clean compile

## Running the sample

The following command summarizes the sample usage:

    mvn exec:java \
        -Dexec.mainClass="com.example.cloud.iot.examples.MqttExample" \
        -Dexec.args="-project_id=my-iot-project \
                     -registry_id=my-registry \
                     -device_id=my-device \
                     -private_key_file=rsa_private_pkcs8 \
                     -algorithm=RS256"

For example, if your project ID is `blue-jet-123`, your service account
credentials are stored in your home folder in creds.json and you have generated
your credentials using the [`generate_keys.sh`](../generate_keys.sh) script
provided in the parent folder, you can run the sample as:

    mvn exec:java \
        -Dexec.mainClass="com.example.cloud.iot.examples.MqttExample" \
        -Dexec.args="-project_id=blue-jet-123 \
                     -registry_id=my-registry \
                     -device_id=my-device \
                     -private_key_file=../rsa_private_pkcs8 \
                     -algorithm=RS256"

## Reading the messages written by the sample client

1. Create a subscription to your topic.

    gcloud beta pubsub subscriptions create \
        projects/your-project-id/subscriptions/my-subscription \
        --topic device-events

2. Read messages published to the topic

    gcloud beta pubsub subscriptions pull --auto-ack \
        projects/my-iot-project/subscriptions/my-subscription
