# Getting Started with Cloud Pub/Sub and the Google Cloud Client libraries

[Google Cloud Pub/Sub][pubsub] is a fully-managed real-time messaging service that allows you to
send and receive messages between independent applications.
This sample Java application demonstrates how to access the Pub/Sub API using
the [Google Cloud Client Library for Java][google-cloud-java].

[pubsub]: https://cloud.google.com/pubsub/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

For more samples, see the samples in
[google-cloud-java](https://github.com/GoogleCloudPlatform/google-cloud-java/tree/master/google-cloud-examples/src/main/java/com/google/cloud/examples/pubsub).

## Quickstart

#### Setup
- Install [Maven](http://maven.apache.org/) <p>
- Install the [Google Cloud SDK](https://cloud.google.com/sdk/) and run :


      gcloud config set project [YOUR PROJECT ID]


- Build your project with:


	  mvn clean package -DskipTests

#### Testing

Run the tests with Maven.

    mvn clean verify

#### Creating a new topic (using the quickstart sample)

    mvn exec:java -Dexec.mainClass=com.example.pubsub.QuickstartSample
