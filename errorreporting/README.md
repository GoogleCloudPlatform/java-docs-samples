# Stackdriver Error Reporting sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=errorreporting/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Stackdriver Error Reporting][error-reporting] Stackdriver Error Reporting counts, analyzes and aggregates the crashes in your running cloud services.
A [centralized error management interface](https://console.cloud.google.com/errors) displays the results with sorting and filtering capabilities.

This sample Java application demonstrates how to send custom error events using the [Error Reporting API][api-ref-docs].
Note: Runtime exceptions and stack traces are automatically sent to Error reporting in applications running in [App Engine Flex environment][ae-flex].

[ae-flex]: https://cloud.google.com/appengine/docs/flexible/java
[error-reporting]: https://cloud.google.com/error-reporting/
[api-ref-docs]: https://googlecloudplatform.github.io/google-cloud-java/latest/apidocs/index.html?com/google/cloud/errorreporting/v1beta1/package-summary.html

## Setup

1. Install [Maven](http://maven.apache.org/).
1. [Enable](https://console.cloud.google.com/apis/api/clouderrorreporting.googleapis.com/overview) Stack Driver Error Reporting API.

## Build

Build your project with:
```
	mvn clean package
```

## Local testing

1. [Create a service account](https://cloud.google.com/docs/authentication/getting-started#creating_the_service_account)
and set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable.
2. Run
```
   mvn clean verify
```
Check the [error reporting console](https://console.cloud.google.com/errors).

Confirm that you see the custom errors reported using the Error Reporting API.
