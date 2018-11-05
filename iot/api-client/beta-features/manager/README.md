# Cloud IoT Core Java Command Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=iot/api-client/manager/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample app demonstrates sending a command to a Cloud IoT Core device.

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

    usage: DeviceRegistryExample [--cloud_region <arg>] --command <arg>
           [--data <arg>] [--device_id <arg>] [--project_id <arg>]
           [--registry_name <arg>]
    Cloud IoT Core Commandline Example (Device / Registry management):

        --cloud_region <arg>    GCP cloud region.
        --command <arg>         Command to run:
                                send-command
        --data <arg>            The command data (string or JSON) to send to
                                the specified device.
        --device_id <arg>       Name for your Device.
        --project_id <arg>      GCP cloud project name.
        --registry_name <arg>   Name for your Device Registry.

For example, if your project ID is `blue-jet-123`, your service account
credentials are stored in your home folder in creds.json and you have generated
your credentials using the shell script provided in the parent folder, you can
run the sample as:

    mvn exec:java \
      -Dexec.mainClass="com.example.cloud.iot.examples.DeviceRegistryExample" \
      -Dexec.args="-project_id=blue-jet-123 \
                   -registry_name=your-registry-id \
                   -device_id=your-device-id \
                   -command=send-command \
                   -data=hello"

# Cloud IoT Core Java MQTT Command example

This sample app receives commands sent by the command app.

Note that before you can run the sample, you must configure a Google Cloud
PubSub topic for Cloud IoT Core and register a device as described in the
[parent README](../README.md).

## Setup

Run the following command to install the dependencies using Maven:

    mvn clean compile

## Running the sample

The following command summarizes the sample usage:

    usage: DeviceRegistryExample [--cloud_region <arg>] --command <arg>
           [--command_data <arg>] [--device_id <arg>] [--project_id <arg>]
           [--registry_name <arg>]
    Cloud IoT Core Commandline Example (Device / Registry management):

        --cloud_region <arg>    GCP cloud region.
        --command <arg>         Command to run:
                                send-command
        --command_data <arg>    The command data (string or JSON) to send to
                                the specified device.
        --device_id <arg>       Name for your Device.
        --project_id <arg>      GCP cloud project name.
        --registry_name <arg>   Name for your Device Registry.

For example, if your project ID is `blue-jet-123`, your device registry is
located in the `asia-east1` region, and you have generated your
credentials using the [`generate_keys.sh`](../generate_keys.sh) script
provided in the parent folder, you can run the sample as:

    mvn exec:java \
        -Dexec.mainClass="com.example.cloud.iot.examples.MqttExample" \
        -Dexec.args="-project_id=blue-jet-123 \
                     -registry_id=my-registry \
                     -cloud_region=asia-east1 \
                     -device_id=my-device \
                     -private_key_file=../rsa_private_pkcs8 \
                     -algorithm=RS256"
