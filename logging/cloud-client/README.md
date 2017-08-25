# Getting Started with Stackdriver Logging and the Google Cloud Client libraries

[Stackdriver Logging][logging]  allows you to store, search, analyze, monitor,
and alert on log data and events from Google Cloud Platform and Amazon Web
Services.
These sample Java applications demonstrate how to access the Stackdriver Logging API using
the [Google Cloud Client Library for Java][google-cloud-java].

[logging]: https://cloud.google.com/logging/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Setup

Install [Maven](http://maven.apache.org/).

Build your project with:

	mvn clean package -DskipTests
	
[Setup authentication](https://cloud.google.com/docs/authentication) using a service account.

### Writing a log entry (using the quickstart sample)

    mvn exec:java -Dexec.mainClass=com.example.logging.QuickstartSample \
        -Dexec.args="my-log"


### List log entries

    mvn exec:java -Dexec.mainClass=com.example.logging.ListLogs \
           -Dexec.args="my-log"
 

Logs can also viewed using the [Logs Viewer Console](https://pantheon.corp.google.com/logs/viewer).
