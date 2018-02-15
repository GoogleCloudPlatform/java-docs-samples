# Cloud IoT Core Java Samples

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=iot/api-client/manager/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This folder contains Java samples that demonstrate an overview of the
Google Cloud IoT Core platform.

## Quickstart

1. From the [Google Cloud IoT Core section](https://console.cloud.google.com/iot/)
   of the Google Cloud console, create a device registry.
2. Use the [`generate_keys.sh`](generate_keys.sh) script to generate your signing keys:

    ./generate_keys.sh

3. Add a device using the file `rsa_cert.pem`, specifying RS256_X509 and using the
  text copy of the public key starting with the ----START---- block of the certificate.

    cat rsa_cert.pem

4. Connect a device using the HTTP or MQTT device samples in the [manager](./manager) folder.

5. Programmattically control device configuration and using the device manager sample in the [manager](./manager) folder.
