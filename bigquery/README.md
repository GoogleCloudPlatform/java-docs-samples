# Getting Started with BigQuery

Google's BigQuery Service features a REST-based API that allows developers to
create applications to run ad-hoc queries on massive datasets. These sample
Java applications demonstrate how to access the BigQuery API.

## API Libraries

We provide samples for multiple methods of accessing the APIs in case you need
lower-level access, but the `cloud-client` samples are idiomatic and show the
recommended way to access the API.

- cloud-client (Preferred Option)
  - This uses [Google Cloud Client
    Libraries](http://googlecloudplatform.github.io/google-cloud-java/), and
    the idiomatic and
    [recommended](https://cloud.google.com/bigquery/docs/reference/libraries)
    way to interact with BigQuery.
- rest
  - This uses BigQuery's RESTful API directly. Not recommended.

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

	mvn clean package -DskipTests

You can then run a given `ClassName` via:

	mvn exec:java -Dexec.mainClass=com.google.cloud.bigquery.samples.ClassName \
		-Dexec.args="any arguments to the app"

## Products
- [Google BigQuery][2]

## Language
- [Java][3]

[2]: https://cloud.google.com/bigquery
[3]: https://java.com

