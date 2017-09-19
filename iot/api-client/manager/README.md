# Cloud IoT Core Java Device Management example

This sample app demonstrates device management for Google Cloud IoT Core.

Note that before you can run the sample, you must configure a Google Cloud
PubSub topic for Cloud IoT as described in [the parent README](../README.md).

## Setup

Manually install [the provided client library](https://cloud.google.com/iot/resources/java/cloud-iot-core-library.jar)
for Cloud IoT Core to Maven:

     mvn install:install-file -Dfile=cloud-iot-core-library.jar -DgroupId=com.example.apis \
    -DartifactId=google-api-services-cloudiot -Dversion=v1beta1-rev20170418-1.22.0-SNAPSHOT \
    -Dpackaging=jar

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

    java -cp target/cloudiot-manager-demo-1.0-jar-with-dependencies.jar \
        com.example.cloud.iot.examples.DeviceRegistryExample \
        -project_id=blue-jet-123 -pubsub_topic=hello-java
        -command=create-iot-topic

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
        -registry_name=hello-java -command=create-registry \

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
