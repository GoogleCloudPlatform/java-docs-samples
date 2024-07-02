# Cloud Spanner OpenTelemetry Traces OTLP Sample

## Setup

This sample requires [Java](https://www.java.com/en/download/) and [Maven](http://maven.apache.org/) to run the integration test.

1.  **Follow the set-up instructions in [the documentation](https://cloud.google.com/java/docs/setup).**

2.  Enable APIs for your project.
    [Click here](https://console.cloud.google.com/flows/enableapi?apiid=spanner.googleapis.com&showconfirmation=true)
    to visit Cloud Platform Console and enable the Google Cloud Spanner API.

3.  Create a Cloud Spanner instance and database via the Cloud Plaform Console's
    [Cloud Spanner section](http://console.cloud.google.com/spanner).

4.  Enable application default credentials by running the command `gcloud auth application-default login`.

## Run the Example

1. Set up the following values to help the application locate your database:
    ````
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    ````

2. Set up the OTEL [Collector](https://opentelemetry.io/docs/collector/quick-start/) and update the OTLP endpoint
    ````
    .setEndpoint("http://localhost:4317") // Replace with your OTLP endpoint
    ````
    
3. Then run the application from command line, after switching to this directory:
    ````
    mvn exec:java -Dexec.mainClass="com.example.spanner.OpenTelemetryUsage"
    ````

You should start seeing traces in Collector .
