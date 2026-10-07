# Dialogflow CX

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=dialogflow-cx/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Dialogflow CX](https://cloud.google.com/dialogflow/cx/docs) is a conversational
AI platform for building advanced virtual agents with flows and pages. These sample
Java applications demonstrate how to access the Dialogflow CX API using the Google
Java client libraries.

## Prerequisites

### Enable the API

You must [enable the Dialogflow CX API](https://console.cloud.google.com/flows/enableapi?apiid=dialogflow.googleapis.com)
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

Samples in this directory use the Dialogflow CX Java client library.

## Tests

Run all tests in a sample directory with Maven:

```
mvn clean verify
```

## Contributing

See the [contributor guide](../../CONTRIBUTING.md) for this repository.
