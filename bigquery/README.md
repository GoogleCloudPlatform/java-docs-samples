# Google BigQuery

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=bigquery/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Google [BigQuery](https://cloud.google.com/bigquery/) is a serverless data warehouse
for analytics over massive datasets. These sample Java applications demonstrate how to
access the BigQuery API using the Google Java client libraries and REST-based samples.

## Prerequisites

### Enable the API

You must [enable the BigQuery API](https://console.cloud.google.com/flows/enableapi?apiid=bigquery.googleapis.com)
for your project in order to use these samples.

### Set Environment Variables

You must set your project ID in order to run the samples.

```text
$ export GOOGLE_CLOUD_PROJECT=<your-project-id-here>
```

### Authentication

See the [authentication documentation](https://cloud.google.com/docs/authentication/production)
for more information about authenticating for Google Cloud APIs.

## Samples

Samples are organized by client library and usage pattern:

- [Cloud Client samples](cloud-client/)
- [REST samples](rest/)
- [BigQuery Storage samples](bigquerystorage/)

Additional BigQuery-related samples may be located in nested subdirectories.

## Tests

Run all tests in a sample directory with Maven:

```
mvn clean verify
```

## Contributing

See the [contributor guide](../../CONTRIBUTING.md) for this repository.
