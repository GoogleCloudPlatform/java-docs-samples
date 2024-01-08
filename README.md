# Google Cloud Platform Java Samples

[![Build Status][java-11-badge]][java-11-link] [![Build
Status][java-17-badge]][java-17-link]

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This repository holds sample code written in Java that demonstrates the
[Google Cloud Platform](https://cloud.google.com/docs/).

Some samples have accompanying guides on <cloud.google.com>. See respective
README files for details.

## Google Cloud Samples

To browse ready to use code samples check [Google Cloud Samples](https://cloud.google.com/docs/samples?l=java).

## Set Up

1. [Set up your Java Development Environment](https://cloud.google.com/java/docs/setup)

1. Clone this repository:

        git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git

1. Obtain authentication credentials.

    Create local credentials by running the following command and following the
    oauth2 flow (read more about the command [here][auth_command]):

        gcloud auth application-default login

    Or manually set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable
    to point to a service account key JSON file path.

    Learn more at [Setting Up Authentication for Server to Server Production Applications][ADC].

    *Note:* Application Default Credentials is able to implicitly find the credentials as long as the application is running on Compute Engine, Kubernetes Engine, App Engine, or Cloud Functions.

## Contributing

* See the [Contributing Guide](CONTRIBUTING.md)

## Licensing

* See [LICENSE](LICENSE)

## Supported Java runtimes

Every submitted change has to pass all checks that run on the testing environments with Java 11 and Java 17 runtimes before merging the change to the main branch.
We run periodic checks on the environments with Java 8 and Java 21 runtimes but we don't enforce passing these tests at the moment.
Because Java 8 is a [supported Java runtime][supported_runtimes] in Google Cloud, please configure to build your code sample with Java 8.
In exceptional cases, configure to build your code sample using Java 11.

[supported_runtimes]: https://cloud.google.com/java/docs/supported-java-versions

## Source Code Headers

Every file containing source code must include copyright and license
information. This includes any JS/CSS files that you might be serving out to
browsers. (This is to help well-intentioned people avoid accidental copying that
doesn't comply with the license.)

Apache header:

    Copyright 2022 Google LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[ADC]: https://developers.google.com/identity/protocols/application-default-credentials
[auth_command]: https://cloud.google.com/sdk/gcloud/reference/beta/auth/application-default/login
[java-8-badge]:
https://storage.googleapis.com/cloud-devrel-kokoro-resources/java/badges/java-docs-samples-8.svg
[java-8-link]:
https://storage.googleapis.com/cloud-devrel-kokoro-resources/java/badges/java-docs-samples-8.html
[java-11-badge]:
https://storage.googleapis.com/cloud-devrel-kokoro-resources/java/badges/java-docs-samples-11.svg
[java-11-link]:
https://storage.googleapis.com/cloud-devrel-kokoro-resources/java/badges/java-docs-samples-11.html
[java-17-badge]:
https://storage.googleapis.com/cloud-devrel-kokoro-resources/java/badges/java-docs-samples-17.svg
[java-17-link]:
https://storage.googleapis.com/cloud-devrel-kokoro-resources/java/badges/java-docs-samples-17.html

Java is a registered trademark of Oracle and/or its affiliates.
