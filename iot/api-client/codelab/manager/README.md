# Cloud IoT Core Commands Java Codelab

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=iot/api-client/manager/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample app demonstrates device management for Google Cloud IoT Core.

Note that before you can run the sample, you must configure a Google Cloud
PubSub topic for Cloud IoT as described in [the parent README](../README.md).

Before running the samples, you can set the `GOOGLE_CLOUD_PROJECT` and
`GOOGLE_APPLICATION_CREDENTIALS` environment variables to avoid passing them to
the sample every time you run it.

## Setup
Run the following command to install the libraries and build the sample with
Maven:

    mvn clean compile assembly:single

## Running the sample

The following description summarizes the sample usage:

    usage: MqttCommandsDemo [--cloud_region <arg>] --project_id <arg>
    --registry_id <arg> --device_id <arg>

    Cloud IoT Core Commandline Example (MQTT Device / Commands codelab):

    --cloud_region <arg>           GCP cloud region (default us-central1).
    --private_key_file <arg>       Path to RS256 private key file.
    --algorithm <arg>              Encryption algorithm to use to generate the JWT.
    --project_id <arg>             GCP cloud project name.
    --registry_id <arg>            Name for your Device Registry.
    --device_id <arg>              ID for your Device.

https://cloud.google.com/iot-core

For example, if your project ID is `blue-jet-123`, your service account
credentials are stored in your home folder in creds.json and you have generated
your credentials using the shell script provided in the parent folder, you can
run the sample as:

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
        -Dexec.mainClass="com.example.cloud.iot.examples.MqttCommandsDemo" \
        -Dexec.args="-project_id=my-iot-project \
                     -registry_id=my-registry \
                     -cloud_region=us-central1 \
                     -device_id=my-device \
                     -private_key_file=rsa_private_pkcs8 \
                     -algorithm=RS256"

Run mqtt example:

    mvn exec:java \
        -Dexec.mainClass="com.example.cloud.iot.examples.MqttCommandsDemo" \
        -Dexec.args="-project_id=blue-jet-123 \
                     -registry_id=my-registry \
                     -cloud_region=asia-east1 \
                     -device_id=my-device \
                     -private_key_file=../rsa_private_pkcs8 \
                     -algorithm=RS256"

