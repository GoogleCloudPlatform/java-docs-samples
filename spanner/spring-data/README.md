# Spring Data Cloud Spanner Sample

An example that demonstrate read, write, and administrative operations
using [Spring Data Cloud Spanner](https://github.com/spring-cloud/spring-cloud-gcp/blob/master/docs/src/main/asciidoc/spanner.adoc).

This sample requires [Java](https://www.java.com/en/download/) and [Maven](http://maven.apache.org/) for building the application.

This sample includes 3 classes that demonstrate how to use Spring Data Cloud Spanner to perform
read, write, and database admin operations.

* `SpannerSchemaToolsSample` - create and drop interleaved tables based on a root Java entity class.
* `SpannerTemplateSample` - perform read and write operations with Java entities.
* `SpannerRepositorySample` - perform operations and queries by defining their operations in an interface. 


## Build and Run

1.  **Follow the set-up instructions in [the documentation](https://cloud.google.com/java/docs/setup).**

2.  Enable APIs for your project.
    [Click here](https://console.cloud.google.com/flows/enableapi?apiid=spanner.googleapis.com&showconfirmation=true)
    to visit Cloud Platform Console and enable the Google Cloud Spanner API.

3.  Create a Cloud Spanner instance and database via the Cloud Plaform Console's
    [Cloud Spanner section](http://console.cloud.google.com/spanner).

4.  Supply your instance and database name in `resources/application.properties`

5.  Run the following Maven command to run `QuickStartSample`, which runs table creation, write, and read operations:
    ```
    mvn exec:java -Dexec.mainClass="com.example.spanner.QuickStartSample"
    ```
