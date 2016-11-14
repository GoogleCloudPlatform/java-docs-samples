# Getting Started with Cloud Pub/Sub and the Google Cloud Client libraries

[Google Cloud Pub/Sub][pubsub] is a fully-managed real-time messaging service that allows you to
send and receive messages between independent applications.
These sample Java applications demonstrate how to access the Pub/Sub API using
the [Google Cloud Client Library for Java][google-cloud-java].

[pubsub]: https://cloud.google.com/pubsub/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

	mvn clean package -DskipTests
	
You can then run a given `ClassName` via:

	mvn exec:java -Dexec.mainClass=com.example.pubsub.ClassName \
	    -DpropertyName=propertyValue \
		-Dexec.args="any arguments to the app"

### Creating a new topic (using the quickstart sample)

    mvn exec:java -Dexec.mainClass=com.example.pubsub.QuickstartSample
