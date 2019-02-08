# Cloud IoT Core Java Gateway Command Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=iot/api-client/manager/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample app demonstrates sending telemetry data on behalf of a device using
a Gateway.

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

The following command will run the sample


    mvn exec:java -Dexec.mainClass="com.example.cloud.iot.examples.DeviceGatewayExample"

The following description summarizes the sample usage:

    usage: DeviceGatewayExample [--cloud_region <arg>] --command <arg>
           [--device_id <arg>] [--project_id <arg>] [--registry_name <arg>]
           [--telemetry_data <arg>]
    Cloud IoT Core Commandline Example (Device Gateways):

        --cloud_region <arg>     GCP cloud region.
        --command <arg>          Command to run:
                                 send-delegate-telemetry
        --device_id <arg>        Name for the delegate device.
        --project_id <arg>       GCP cloud project name.
        --registry_name <arg>    Name for your Device Registry.
        --telemetry_data <arg>   The telemetry data (string or JSON) to send
                                 on behalf of the delegated device.

    https://cloud.google.com/iot-core

The following example shows using the sample to send the telemetry data `hello`
on behalf of the device `some-device-id`:

    mvn exec:java -Dexec.mainClass="com.example.cloud.iot.examples.DeviceGatewayExample" \
    -Dexec.args="-cloud_region=us-central1 \
        -project_id=blue-jet-123 \
        -command=send-delegate-telemetry \
        -device_id=your-device-id \
        -telemetry_data=hello \
        -device_id=some-device-id \
        -registry_name=my-registry"

## Troubleshooting
Make sure to bind your device before trying to attach it. If your device is not
bound to the gateway, the call to attach the device will not return.
