# Google Cloud Pub/Sub

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=pubsub/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Google [Cloud Pub/Sub](https://cloud.google.com/pubsub/) is a messaging and
event-streaming service for asynchronous communication between independent
applications. These sample Java applications demonstrate how to access the
Pub/Sub API using the Google Java client libraries and related integration patterns.

## Prerequisites

### Enable the API

You must [enable the Cloud Pub/Sub API](https://console.cloud.google.com/flows/enableapi?apiid=pubsub.googleapis.com)
for your project in order to use these samples.

### Authentication

See the [authentication documentation](https://cloud.google.com/docs/authentication/production)
for more information about authenticating for Google Cloud APIs.

## Samples

Samples are organized by client library and integration pattern:

- [Cloud Client samples](cloud-client/)
- [Spring integration samples](spring/)
- [Streaming analytics samples](streaming-analytics/)

## Tests

Run all tests in a sample directory with Maven:

```
mvn clean verify
```

## Contributing

See the [contributor guide](../../CONTRIBUTING.md) for this repository.
