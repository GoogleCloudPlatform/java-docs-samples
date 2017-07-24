# Cloud IoT Core Java Device Management example

This sample app demonstrates device management for Google Cloud IoT Core.

Note that before you can run the sample, you must configure a Google Cloud
PubSub topic for Cloud IoT as described in [the parent README](../README.md).

## Setup

Manually install [the provided client library](https://cloud.google.com/iot/resources/java/cloud-iot-core-library.jar)
for Cloud IoT Core to Maven:

     mvn install:install-file -Dfile=cloud-iot-core-library.jar -DgroupId=com.google.apis \
    -DartifactId=google-api-services-cloudiot -Dversion=v1beta1-rev20170418-1.22.0-SNAPSHOT \
    -Dpackaging=jar

Run the following command to install the libraries and build the sample with
Maven:

mvn clean compile assembly:single

## Running the sample

The following command summarizes the sample usage:

    mvn exec:java \
        -Dexec.mainClass="com.google.cloud.iot.examples.DeviceRegistryExample" \
        -Dexec.args="-project_id=my-project-id \
            -pubsub_topic=projects/my-project-id/topics/my-topic-id \
            -ec_public_key_file=/path/to/ec_public.pem \
            -rsa_certificate_file=/path/to/rsa_cert.pem"

For example, if your project ID is `blue-jet-123`, your service account
credentials are stored in your home folder in creds.json and you have generated
your credentials using the shell script provided in the parent folder, you can
run the sample as:

    mvn exec:java \
        -Dexec.mainClass="com.google.cloud.iot.examples.DeviceRegistryExample" \
        -Dexec.args="-project_id=blue-jet-123 \
            -pubsub_topic=projects/blue-jet-123/topics/device-events \
            -ec_public_key_file=../ec_public.pem \
            -rsa_certificate_file=../rsa_cert.pem"
