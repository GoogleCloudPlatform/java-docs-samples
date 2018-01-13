# Getting Started with BigQuery with the REST API

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=bigquery/rest/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Google's BigQuery Service features a REST-based API that allows developers to
create applications to run ad-hoc queries on massive datasets. These sample
Java applications demonstrate how to access the BigQuery API directly using the
Google HTTP Client.

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

    mvn clean package -DskipTests

You can then run a given `ClassName` via:

    mvn exec:java -Dexec.mainClass=com.example.bigquery.ClassName \
      -Dexec.args="any arguments to the app"

### Labeling a dataset

[Label a dataset](https://cloud.google.com/bigquery/docs/labeling-datasets).

    mvn exec:java -Dexec.mainClass=com.example.bigquery.LabelsSample \
      -Dexec.args="project-id dataset-id label-key label-value"

## Testing

To run the tests for this sample, first set the `GOOGLE_CLOUD_PROJECT`
environment variable. The project should have a dataset named `test_dataset`
with a table named `test_table`.

    export GOOGLE_CLOUD_PROJECT=my-project

Then run the tests with Maven.

    mvn clean verify

## Products
- [Google BigQuery][2]

## Language
- [Java][3]

## Dependencies
- [Google HTTP Client Library for Java][4]

[2]: https://cloud.google.com/bigquery
[3]: https://java.com
[4]: https://github.com/google/google-http-java-client

