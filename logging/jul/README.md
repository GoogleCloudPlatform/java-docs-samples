# Getting Started with Stackdriver Logging using `java.util.logging`

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=logging/jul/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Stackdriver Logging][logging]  allows you to store, search, analyze, monitor,
and alert on log data and events from Google Cloud Platform and Amazon Web
Services.
These sample Java applications demonstrate how to write logs to Stackdriver using
the default Java Logging API (`java.util.logging`) handler for 
[Google Cloud Client Library for Java][google-cloud-java].

[logging]: https://cloud.google.com/logging/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Setup

Install [Maven](http://maven.apache.org/).

Build your project with:

	mvn clean package -DskipTests
	
[Setup authentication](https://cloud.google.com/docs/authentication) using a service account.

## Configuration

Update [logging.properties](src/main/resources/logging.properties) to configure the handler.

## Enhancers

[ExampleEnhancer.java](src/main/java/com/example/logging/jul/enhancers/ExampleEnhancer.java)
provides an example of enhancing log entries with additional labels.


## Writing log entries
    mvn exec:java -Dexec.mainClass=com.example.logging.jul.Quickstart \
        -Dexec.args="-Djava.util.logging.file=src/main/resources/logging.properties"           

Logs can be viewed using the [Logs Viewer Console](https://console.cloud.google.com/logs/viewer).
