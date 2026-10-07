# Google Cloud Storage

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=storage/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Google [Cloud Storage](https://cloud.google.com/storage/) is unified object storage
for developers and enterprises, from live data serving to data analytics and archival.
These sample Java applications demonstrate how to access the Cloud Storage API using
the Google Cloud Java client libraries and related SDK samples.

## Prerequisites

### Enable the API

You must [enable the Cloud Storage API](https://console.cloud.google.com/flows/enableapi?apiid=storage.googleapis.com)
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

Samples are organized by client library and SDK usage:

- [Cloud Client samples](cloud-client/)
- [AWS S3 compatibility SDK samples](s3-sdk/)

## Tests

Run all tests in a sample directory with Maven:

```
mvn clean verify
```

## Contributing

See the [contributor guide](../../CONTRIBUTING.md) for this repository.
