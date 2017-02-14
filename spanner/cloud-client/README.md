# Getting Started with Cloud Spanner and the Google Cloud Client libraries

[Cloud Spanner][Spanner] is a horizontally-scalable database-as-a-service
with transactions and SQL support.
These sample Java applications demonstrate how to access the Spanner API using
the [Google Cloud Client Library for Java][google-cloud-java].

[Spanner]: https://cloud.google.com/spanner/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

    mvn clean package -DskipTests

You can then run a given `ClassName` via:

    mvn exec:java -Dexec.mainClass=com.example.spanner.ClassName \
        -DpropertyName=propertyValue \
        -Dexec.args="any arguments to the app"

### Running a simple query (using the quickstart sample)

    mvn exec:java -Dexec.mainClass=com.example.spanner.QuickstartSample -Dexec.args="my-instance my-database"

## Tutorial

### Running the tutorial
    mvn exec:java -Dexec.mainClass=com.example.spanner.SpannerSample -Dexec.args="<command> my-instance my-database"

## Test
    mvn verify -Dspanner.test.instance=<instance id> -Dspanner.sample.database=<new database id>  -Dspanner.quickstart.database=<existing database id>
