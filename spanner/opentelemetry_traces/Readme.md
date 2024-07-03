# Cloud Spanner OpenTelemetry Traces

## Setup

This sample requires [Java](https://www.java.com/en/download/) and [Maven](http://maven.apache.org/).

1.  **Follow the set-up instructions in [the documentation](https://cloud.google.com/java/docs/setup).**

2.  Enable APIs for your project.
    
    a. [Click here](https://console.cloud.google.com/flows/enableapi?apiid=spanner.googleapis.com&showconfirmation=true)
    to visit Cloud Platform Console and enable the Google Cloud Spanner API.
    
    b. [Click here](https://console.cloud.google.com/flows/enableapi?apiid=cloudtrace.googleapis.com&showconfirmation=true)
    to visit Cloud Platform Console and enable the Cloud Trace API.

3.  Create a Cloud Spanner instance and database via the Cloud Plaform Console's
    [Cloud Spanner section](http://console.cloud.google.com/spanner).

4.  Enable application default credentials by running the command `gcloud auth application-default login`.

## Run the Example

1. Set up database configuration in the `OpenTelemetryUsage.java` class:
    ````
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    ````

2. Configure trace data export. You can use either the OpenTelemetry [Collector](https://opentelemetry.io/docs/collector/quick-start/ with the OTLP Exporter or the Cloud Trace Exporter. By default, the Cloud Trace Exporter is used.

- To use OTLP Exporter, Set up the OpenTelemetry [Collector](https://opentelemetry.io/docs/collector/quick-start/) and update the OTLP endpoint in `OpenTelemetryUsage.java` class
    ````
    boolean useCloudTraceExporter = true; // Replace to false for OTLP
    String otlpEndpoint = "http://localhost:4317"; // Replace with your OTLP endpoint
    ````

3. You can also enable API Tracing and SQL Statement Tracing by setting below options. Refer [Traces](https://github.com/googleapis/java-spanner?tab=readme-ov-file#traces) for more details. 
    ````
    SpannerOptions options = SpannerOptions.newBuilder()
    .setOpenTelemetry(openTelemetry)
    .setEnableExtendedTracing(true)
    .setEnableApiTracing(true)
    .build();
    ````

4. Then run the application from command line, after switching to this directory:
    ````
    mvn exec:java -Dexec.mainClass="com.example.spanner.OpenTelemetryUsage"
    ````

You should start seeing traces in Cloud Trace .
