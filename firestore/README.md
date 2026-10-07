# Cloud Firestore

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=firestore/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Cloud Firestore](https://cloud.google.com/firestore/) is a flexible, scalable
NoSQL document database for mobile, web, and server development. These sample
Java applications demonstrate how to access the Firestore API using the Google
Java client libraries.

## Prerequisites

### Enable the API

You must [enable the Cloud Firestore API](https://console.cloud.google.com/flows/enableapi?apiid=firestore.googleapis.com)
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

Samples are organized in the [samples](samples/) subdirectory.

## Tests

Run all tests in a sample directory with Maven:

```
mvn clean verify
```

## Contributing

See the [contributor guide](../../CONTRIBUTING.md) for this repository.
