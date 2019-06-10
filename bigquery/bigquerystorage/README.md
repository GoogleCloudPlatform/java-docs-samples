# Getting Started with the BigQuery Storage API

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=bigquery/bigquerystorage/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Google's BigQuery Service features a Storage API for performing reads of BigQuery-managed data at
scale.  This sample demonstrates using the API to read a sample table from the BigQuery public
datasets, projecting a subset of columns, and filtering that data on the server side.

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

    mvn clean package -DskipTests

You can then run a given `ClassName` via:

    mvn exec:java -Dexec.mainClass=com.example.bigquery.ClassName \
      -Dexec.args="any arguments to the app"

### Reading a Table with the BigQuery Storage API

    mvn exec:java -Dexec.mainClass=com.example.bigquerystorage.BigQueryStorage \
      -Dexec.args="project-id"

## Testing

To run the tests for this sample, first set the `GOOGLE_CLOUD_PROJECT`
environment variable. 

    export GOOGLE_CLOUD_PROJECT=my-project

Then run the tests with Maven.

    mvn clean verify
