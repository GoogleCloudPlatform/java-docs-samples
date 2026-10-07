# Google Cloud Bigtable

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=bigtable/README.md">
<img alt="Open in Cloud Shell" src="https://gstatic.com/cloudssh/images/open-btn.png"></a>

Google [Bigtable](https://cloud.google.com/bigtable/) is a scalable NoSQL
wide-column database for large analytical and operational workloads. These sample
Java applications demonstrate how to access the Bigtable API using the Google Java
client libraries and related integration patterns.

## Prerequisites

### Enable the API

You must [enable the Cloud Bigtable API](https://console.cloud.google.com/flows/enableapi?apiid=bigtable.googleapis.com)
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

Samples are organized by client library, framework, and usage pattern:

- [Apache Beam samples](beam/)
- [Spark samples](spark/)
- [HBase migration samples](hbase/)
- [Bigtable Proxy samples](bigtable-proxy/)
- [Scheduled backup samples](scheduled-backups/)
- [Memorystore samples](memorystore/)
- [Use case samples](use-cases/)

Additional samples may be located in nested subdirectories.

## Tests

Run all tests in a sample directory with Maven:

```
mvn clean verify
```

## Contributing

See the [contributor guide](../CONTRIBUTING.md) for this repository.
