# Cloud Spanner OpenCensus Sample

An example that demonstrates round-trip and query stats latency of [Google Cloud Spanner](https://cloud.google.com/spanner/) operations.

This sample requires [Java](https://www.java.com/en/download/) and [Maven](http://maven.apache.org/) for building the application.

This sample includes 2 classes that demonstrate how to record Cloud Spanner's latencies.

* `CaptureGrpcMetric` - capture client round-trip latency.
* `CaptureGfeMetric` - capture Google Front End (GFE) latency.
* `CaptureQueryStatsMetric` - capture query stats latency.

## Build and Run

1.  **Follow the set-up instructions in [the documentation](https://cloud.google.com/java/docs/setup).**

2.  Enable APIs for your project.
    [Click here](https://console.cloud.google.com/flows/enableapi?apiid=spanner.googleapis.com&showconfirmation=true)
    to visit Cloud Platform Console and enable the Google Cloud Spanner API.

3.  Create a Cloud Spanner instance and database via the Cloud Plaform Console's
    [Cloud Spanner section](http://console.cloud.google.com/spanner).

4.  Enable application default credentials by running the command `gcloud auth application-default login`.
