# Error reporting sample for Google App Engine Flexible

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=flexible/errorreporting/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Stackdriver Error Reporting][error-reporting] Stackdriver Error Reporting counts, analyzes and aggregates the crashes in your running cloud services.
A [centralized error management interface](https://console.cloud.google.com/errors) displays the results with sorting and filtering capabilities.

This sample Java application demonstrates how errors are automatically sent to Error reporting in applications running in [App Engine Flex environment][ae-flex].
It also demonstrates how to send custom error events using the Error Reporting API.

[error-reporting]: https://cloud.google.com/error-reporting/
[ae-flex]: https://cloud.google.com/appengine/docs/flexible/java
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Setup

1. Install [Maven](http://maven.apache.org/).
1. Install and initialize [GCloud SDK](https://cloud.google.com/sdk/downloads).
1. [Enable](https://console.cloud.google.com/apis/api/clouderrorreporting.googleapis.com/overview) Stack Driver Error Reporting API.
(Note : only required for logging custom events using the Error Reporting API)

## Build
Build your project with:
```
	mvn clean package
```

## Local testing
[Create a service account](https://cloud.google.com/docs/authentication/getting-started#creating_the_service_account)
and set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable.

For local testing, we will be using the [Jetty Maven plugin](http://www.eclipse.org/jetty/documentation/9.4.x/jetty-maven-plugin.html).
Run:
```
   mvn jetty:run
```
Access [http://localhost:8080/error](http://localhost:8080/error) endpoint.

After accessing the `/error` endpoint, check the [error reporting console](https://console.cloud.google.com/errors).
Confirm that you see the custom error reported using the error reporting API.

## Deploy
Run:
```
   mvn appengine:deploy
```
Access [https://YOUR_PROJECT_ID.appspot.com/error] endpoint.

After accessing the `/error` endpoint, check the [error reporting console](https://console.cloud.google.com/errors).
Confirm that you see:
1. IllegalArgumentException logged via the standard logging framework.
1. Custom error reported using the error reporting API.
1. Runtime exception.
