# Google Cloud Storage (GCS) and the Google Java API Client library

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=storage/cloud-client/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Google Cloud Storage Service features a REST-based API that allows developers to store and access arbitrarily-large objects. These sample Java applications demonstrate how to access the Google Cloud Storage JSON API using the Google Java API Client Libraries. For more information, read the [Google Cloud Storage JSON API Overview][1].

## Quickstart

1. Install the [Google Cloud SDK](https://cloud.google.com/sdk/), including the [gcloud tool](https://cloud.google.com/sdk/gcloud/).

1. Setup the gcloud tool.

   ```
   gcloud init
   ```

1. Clone this repo.

   ```
   git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
   ```

1. Install [Maven](http://maven.apache.org/).

1. Build this project from this directory:

   ```
   mvn package
   ```

1. Run one of the sample apps by specifying its class name and a bucket name:

   ```
   mvn exec:java -Dexec.mainClass=StorageSample \
           -Dexec.args="ABucketName"
   ```

Note that if it's been a while, you may need to login with gcloud.

   ```
   gcloud auth application-default login
   ```

## Products
- [Google Cloud Storage][2]

## Language
- [Java][3]

## Dependencies
- [Google APIs Client Library for Java][4]

[1]: https://cloud.google.com/storage/docs/json_api
[2]: https://cloud.google.com/storage
[3]: https://java.com
[4]: http://code.google.com/p/google-api-java-client/

