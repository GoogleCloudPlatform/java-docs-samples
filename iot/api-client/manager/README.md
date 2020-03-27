# Cloud IoT Core Java Device Management example

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

    usage: DeviceRegistryExample [--cloud_region <arg>] --command <arg>
       [--ec_public_key_file <arg>] --project_id <arg> --pubsub_topic
       <arg> --registry_name <arg> [--rsa_certificate_file <arg>]

    Cloud IoT Core Commandline Example (Device / Registry management):

    --cloud_region <arg>           GCP cloud region (default us-central1).
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

We recommend using the Maven **exec** plugin for invoking the sample.

For example, if your project ID is `blue-jet-123`, your service account
credentials are stored in your home folder in creds.json and you have generated
your credentials using the shell script provided in the parent folder, you can
run the sample as:


    mvn exec:exec -Dmanager \
                  -Dcr=--cloud_region=us-central1 \
                  -Dproj=--project_id=blue-jet-123 \
                  -Drname=--registry_name=my-registry \
                  -Dcmd=list-devices

The full set of parameters passable to the exec wrapper are as follows:

    mvn exec:exec -Dmanager \
                  -Dpst=--pubsub_topic=<your-topic> \
                  -Decf=--ec_public_key_file=<your-ec-file> \
                  -Drsaf=--rsa_certificate_file=<your-rsa-certificate-file> \
                  -Dcr=--cloud_region=<your region, e.g. us-central1> \
                  -Dproj=--project_id=<your-cloud-project-id> \
                  -Drname=--registry_name=<your-registry-name> \
                  -Ddid=--device_id=<your-device-id> \
                  -Dgid=--gateway_id=<your-gateway-id> \
                  -Ddata=--data=<your-command-data> \
                  -Dconf=--configuration=<your-configuration-data> \
                  -Dv=--version=<your-configuration-version> \
                  -Dm=--member=<your-member-data> \
                  -Dr=--role=<your-role-data> \
                  -Dcmd=--command=<your-command-to-run>

## Usage Examples

Create a PubSub topic, `hello-java`, for the project, `blue-jet-123`:

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-123 \
                  -Dcmd=create-iot-topic \
                  -Dpst=--pubsub_topic=hello-java

Create an ES device:

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-123 \
                  -Dpst=--pubsub_topic=hello-java \
                  -Dcr=--cloud_region=us-central1 \
                  -Drname=--registry_name=hello-java \
                  -Decf=--ec_public_key_file ../ec_public.pem \
                  -Ddid=--device_id=java-device-0 \
                  -Dcmd=create-es

Create an RSA device:

    mvn exec:exec -Dmanager \
                  -Dproj=-project_id=blue-jet-123 \
                  -Dpst=-pubsub_topic=hello-java \
                  -Drname=-registry_name=hello-java \
                  -Drsaf=-rsa_certificate_file=../rsa_cert.pem \
                  -Ddid=-device_id=java-device-1 \
                  -Dcmd=create-rsa

Create a device without authorization:

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-123 \
                  -Dpst=--pubsub_topic=hello-java \
                  -Drname=--registry_name=hello-java \
                  -Ddid=--device_id=java-device-3 \
                  -Dcmd=create-unauth

Create a device registry:

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-123 \
                  -Dpst=--pubsub_topic=hello-java \
                  -Drname=--registry_name=hello-java \
                  -Dcmd=create-registry

Delete a device registry:

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-123 \
                  -Dpst=--pubsub_topic=hello-java \
                  -Drname=--registry_name=hello-java \
                  -Dcmd=delete-registry

Get a device registry:

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-343 \
                  -Dcmd=get-registry \
                  -Drname=--registry_name="hello-java"

List devices:

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-123 \
                  -Dpst=--pubsub_topic=hello-java \
                  -Drname=--registry_name=hello-java \
                  -Dcmd=list-devices

List device registries:

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-123 \
                  -Dpst=--pubsub_topic=hello-java \
                  -Drname=--registry_name=hello-java \
                  -Dcmd=list-registries

Patch a device with ES:

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-123 \
                  -Dpst=--pubsub_topic=hello-java \
                  -Drname=--registry_name=hello-java \
                  -Decf=--ec_public_key_file=../ec_public.pem \
                  -Ddid=--device_id=java-device-1 \
                  -Dcmd=patch-device-es

Patch a device with RSA:

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-123 \
                  -Dpst=--pubsub_topic=hello-java \
                  -Drname=--registry_name=hello-java \
                  -Drsaf=--rsa_certificate_file=../rsa_cert.pem \
                  -Ddid=--device_id=java-device-0 \
                  -Dcmd=patch-device-rsa

Create a gateway with RSA credentials:

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-123 \
                  -Drname=--registry_name=your-registry \
                  -Drsaf=--rsa_certificate_file=../rsa_cert.pem \
                  -Dgid=--gateway_id=java-gateway-0 \
                  -Dcmd=create-gateway

Create a gateway with EC credentials:

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-123 \
                  -Drname=--registry_name=your-registry-name \
                  -Decf=--ec_public_key_file=resources/ec_public.pem \
                  -Dgid=--gateway_id=java-gateway-1 \
                  -Dcmd=create-gateway


Bind a device to a gateway:

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-123 \
                  -Drname=--registry_name=your-registry \
                  -Dgid=--gateway_id=java-gateway-0 \
                  -Ddid=--device_id=java-device-0 \
                  -Dcmd=bind-device-to-gateway

Unbind a device to a gateway:

    mvn exec:exec -Dmanager \
                  -Dproj=-project_id=blue-jet-123 \
                  -Drname=-registry_name=your-registry \
                  -Dgid=-gateway_id=java-gateway-0 \
                  -Ddid=-device_id=java-device-0 \
                  -Dcmd=unbind-device-from-gateway

List gateways in a registry.

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-123 \
                  -Drname=--registry_name=your-registry \
                  -Dcmd=list-gateways

List devices bound to a gateway.

    mvn exec:exec -Dmanager \
                  -Dproj=--project_id=blue-jet-123 \
                  -Drname=--registry_name=your-registry \
                  -Dgid=--gateway_id=your-gateway-id  \
                  -Dcmd=list-devices-for-gateway


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
    mvn exec:exec -Dhttp \
                  -Dproject_id=YOUR-PROJECT-ID \
                  -Dregistry_id=YOUR-REGISTRY-ID \
                  -Ddevice_id=YOUR-DEVICE-ID \
                  -Dalgorithm=RS256|ES256 \
                  -Dprivate_key_file="../path/to/your_private_pkcs8" \
                  -Dcr=-cloud_region=us-central1 | asia-east1 | europe-west1 \
                  -Dhba=http_bridge_address=https://cloudiotdevice.googleapis.com \
                  -Dapiv=-api_version=v1 \
                  -Dexp=-token_exp_minutes=60 \
                  -Dmt=-message_type=state | event
```

For example, if your project ID is `blue-jet-123`, the Cloud region associated
with your device registry is europe-west1, and you have generated your
credentials using the [`generate_keys.sh`](../generate_keys.sh) script
provided in the parent folder, you can run the sample as:

```
    mvn exec:exec -Dhttp \
                  -Dproject_id=blue-jet-123 \
                  -Dregistry_id=my-registry \
                  -Ddevice_id=my-java-device \
                  -Dalgorithm=RS256 \
                  -Dprivate_key_file=../rsa_private_pkcs8 \
                  -Dcr=-cloud_region=asia-east1
```

To publish state messages, run the sample as follows:

```
    mvn exec:exec -Dhttp \
                  -Dproject_id=blue-jet-123 \
                  -Dregistry_id=my-registry \
                  -Ddevice_id=my-java-device \
                  -Dalgorithm=RS256 \
                  -Dprivate_key_file=../rsa_private_pkcs8 \
                  -Dcr=-cloud_region=us-central1 \
                  -Dmt=message_type=state
```


## Reading the messages written by the sample client

1. Create a subscription to your topic.

```
    gcloud pubsub subscriptions create \
        projects/your-project-id/subscriptions/my-subscription \
        --topic device-events
```

2. Read messages published to the topic

```
    gcloud pubsub subscriptions pull --auto-ack \
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

The following example shows you how to invoke the sample using the `mvn exec`:

    mvn exec:exec -Dmqtt \
                  -Dproject_id=YOUR-PROJECT-ID \
                  -Dregistry_id=YOUR-REGISTRY-ID \
                  -Ddevice_id=YOUR-DEVICE-ID \
                  -Dalgorithm=RS256|ES256 \
                  -Dprivate_key_file=../path/to/your_private_pkcs8
                  -Dcmd=-command=listen-for-config-messages
                  -Dgid=-gateway_id=YOUR-GATEWAY-ID
                  -Dcr=-cloud_region=us-central1 | asia-east1 | europe-west1
                  -Dexp=-token_exp_minutes=60
                  -Dmhn=mqtt_bridge_hostname=mqtt.googleapis.com
                  -Dmp=-mqtt_bridge_port=443 | 8883
                  -Dmt=-message_type=state | event
                  -Dtd=-telemetry_data=YOUR-CUSTOM-DATA
                  -Dwt=-wait_time=3600

For example, if your project ID is `blue-jet-123`, your device registry is
located in the `asia-east1` region, and you have generated your
credentials using the [`generate_keys.sh`](../generate_keys.sh) script
provided in the parent folder, you can run the sample as:

Run mqtt example:

    mvn exec:exec -Dmqtt \
                  -Dproject_id=blue-jet-123 \
                  -Dcloud_region=asia-east1 \
                  -Dregistry_id=my-registry \
                  -Ddevice_id=my-test-device \
                  -Dalgorithm=RS256 \
                  -Dprivate_key_file="../path/to/your_private_pkcs8"

Listen for configuration messages:

    mvn exec:exec -Dmqtt \
                  -Dproject_id=blue-jet-123 \
                  -Dregistry_id=my-registry \
                  -Ddevice_id=my-test-device \
                  -Dalgorithm=RS256 \
                  -Dprivate_key_file="../path/to/your_private_pkcs8"
                  -Dgid=-gateway_id=YOUR-GATEWAY-ID \
                  -Dmhn-mqtt_bridge_hostname=mqtt.googleapis.com \
                  -Dmp=-mqtt_bridge_port=443 \
                  -Dcmd=-command=listen-for-config-messages

Send data on behalf of device:

    mvn exec:exec -Dmqtt \
                  -Dcr=-cloud_region=us-central1 \
                  -Ddevice_id=java-device-0 \
                  -Dregistry_id=my-registry  \
                  -Dgid=-gateway_id=test-gateway \
                  -Dprivate_key_file=../your_private_pkcs8 \
                  -Dalgorithm=RS256 \
                  -Dtd=-telemetry_data="your telemetry msg" \
                  -Dmhn=-mqtt_bridge_hostname=mqtt.googleapis.com \
                  -Dmt=-message_type='event' \
                  -Dmp=mqtt_bridge_port=443 \
                  -Dcmd=-command=send-data-from-bound-device


## Reading the messages written by the sample client

1. Create a subscription to your topic.

    gcloud pubsub subscriptions create \
        projects/your-project-id/subscriptions/my-subscription \
        --topic device-events

2. Read messages published to the topic

    gcloud pubsub subscriptions pull --auto-ack \
        projects/my-iot-project/subscriptions/my-subscription
