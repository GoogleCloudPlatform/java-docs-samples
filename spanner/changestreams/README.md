# Cloud Spanner Change Stream Sample

## Setup

This sample requires [Java](https://www.java.com/en/download/) and [Maven](http://maven.apache.org/) to run the integration test.

1.  **Follow the set-up instructions in [the documentation](https://cloud.google.com/java/docs/setup).**

2.  Enable APIs for your project.
    [Click here](https://console.cloud.google.com/flows/enableapi?apiid=spanner.googleapis.com&showconfirmation=true)
    to visit Cloud Platform Console and enable the Google Cloud Spanner API.

3.  Create a Cloud Spanner instance and database via the Cloud Plaform Console's
    [Cloud Spanner section](http://console.cloud.google.com/spanner).

4.  Enable application default credentials by running the command `gcloud auth application-default login`.

## Run integration test

Run the following Maven command to run integration test:

```
mvn test -Dspanner.test.instance=my-instance -Dspanner.test.database=my-db -Dtest=com.example.spanner.changestreams.ChangeStreamSampleIT
```