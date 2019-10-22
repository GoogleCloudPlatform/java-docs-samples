# Google Cloud IoT Core Java Samples

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=iot/api-client/manager/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This directory contains samples for Google Cloud IoT Core. [Google Cloud IoT Core](https://cloud.google.com/iot/docs/ "Google Cloud IoT Core") allows developers to easily integrate Publish and Subscribe functionality with devices and programmatically manage device authorization.

## Prerequisites

### Java

We recommend using [Java 8 JDK](https://java.com/en/download) for this example.

### Download Maven

These samples use the [Apache Maven][maven] build system. Before getting
started, be sure to [download][maven-download] and [install][maven-install] it.
When you use Maven as described here, it will automatically download the needed
client libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

### Create a Project in the Google Cloud Platform Console

If you haven't already created a project, create one now. Projects enable you to
manage all Google Cloud Platform resources for your app, including deployment,
access control, billing, and services.

1. Open the [Cloud Platform Console][cloud-console].
2. In the drop-down menu at the top, select **Create a project**.
3. Give your project a name.
4. Make a note of the project ID, which might be different from the project
   name. The project ID is used in commands and in configurations.

[cloud-console]: https://console.cloud.google.com/

## Setup

Note that before you can run the sample, you must configure a Google Cloud
PubSub topic for Cloud IoT as described in [the parent README](../README.md).

Before running the samples, you can set the `GOOGLE_CLOUD_PROJECT` and
`GOOGLE_APPLICATION_CREDENTIALS` environment variables to avoid passing them to
the sample every time you run it.

For example, on most *nix systems you can do this as:

    export GOOGLE_CLOUD_PROJECT=your-project-id
    export GOOGLE_APPLICATION_CREDENTIALS="/home/user/Downloads/[FILE_NAME].json"


### Authentication

This sample requires you to have authentication setup. Refer to the [Authentication Getting Started Guide](https://cloud.google.com/docs/authentication/getting-started "Google Cloud IoT Core") for instructions on setting up credentials for applications.

Run the following command to install the libraries and build the sample with Maven:

    mvn clean compile assembly:single


Before you begin, you will need to create the Google Cloud PubSub message queue, create a subscription to it, create a device registry, and add a device to the registry.

1. Create the PubSub topic as:

    gcloud pubsub topics create java-e2e

2. Create the subscription:

    gcloud pubsub subscriptions create java-e2e-sub --topic=java-e2e

3. Create the device registry:

    gcloud iot registries create java-ed2e-registry --event-notification-config=topic=java-e2e --region=us-central1

4. Run the `generate_keys.sh` shell script:

    ../generate_keys.sh

5. Add a device to the registry using the keys you generated:

    gcloud iot devices create device-id --registry=java-ed2e-sub --region=us-central1 --public-key=path=./rsa_cert.pem,type=RS256


## Samples

### Server

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=iot/api-client/manager/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

To run this sample:

    mvn exec:java \
        -Dexec.mainClass="com.example.cloud.iot.endtoend.CloudiotPubsubExampleServer" \
        -Dexec.args="-project_id=<your-iot-project> \
                    -pubsub_subscription=<your-pubsub-subscription>"

### Device

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=iot/api-client/manager/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

To run this sample:

    mvn exec:java \
      -Dexec.mainClass="com.example.cloud.iot.endtoend.CloudiotPubsubExampleMqttDevice" \
      -Dexec.args="-project_id=<your-iot-project> \
                   -registry_id=<your-registry-id> \
                   -device_id=<device-id> \
                   -private_key_file=<path-to-keyfile> \
                   -algorithm=<RS256|ES256>"

For example, if your project ID is `blue-jet-123`, your device registry id is
`my-registry`, your device id is `my-device` and you have generated your
credentials using the [`generate_keys.sh`](../generate_keys.sh) script
provided in the parent folder, you can run the sample as:

    mvn exec:java \
        -Dexec.mainClass="com.example.cloud.iot.endtoend.CloudiotPubsubExampleMqttDevice" \
        -Dexec.args="-project_id=blue-jet-123 \
                     -registry_id=my-registry \
                     -device_id=my-device \
                     -private_key_file=../rsa_private_pkcs8 \
                     -algorithm=RS256"
