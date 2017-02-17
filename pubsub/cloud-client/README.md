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

## Testing

To run the tests for this sample, first set the `GOOGLE_CLOUD_PROJECT`
environment variable. The project should have a dataset named `test_dataset`
with a table named `test_table`.

    export GOOGLE_CLOUD_PROJECT=my-project

Then run the tests with Maven.

    mvn clean verify

### Creating a new topic (using the quickstart sample)

    mvn exec:java -Dexec.mainClass=com.example.pubsub.QuickstartSample
