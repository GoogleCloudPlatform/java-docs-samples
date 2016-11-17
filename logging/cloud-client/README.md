# Getting Started with Stackdriver Logging and the Google Cloud Client libraries

[Stackdriver Logging][logging]  allows you to store, search, analyze, monitor,
and alert on log data and events from Google Cloud Platform and Amazon Web
Services.
These sample Java applications demonstrate how to access the Cloud Storage API using
the [Google Cloud Client Library for Java][google-cloud-java].

[logging]: https://cloud.google.com/logging/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

	mvn clean package -DskipTests

You can then run a given `ClassName` via:

	mvn exec:java -Dexec.mainClass=com.example.logging.ClassName \
	    -DpropertyName=propertyValue \
		-Dexec.args="any arguments to the app"

### Writing a log entry (using the quickstart sample)

    mvn exec:java -Dexec.mainClass=com.example.logging.QuickstartSample \
        -Dexec.args="my-log"
