# Getting Started with BigQuery and the Google Java API Client library

Google's BigQuery Service features a REST-based API that allows developers to create applications to run ad-hoc queries
on massive datasets. These sample Java applications demonstrate how to access the BigQuery API using the Google Java API
Client Libraries.

For more information, read the [Getting Started with BigQuery and the Google Java API Client
library][1] codelab.

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

## Dependencies
- [Google APIs Client Library for Java][4]

[1]: https://cloud.google.com/bigquery/bigquery-api-quickstart
[2]: https://developers.google.com/bigquery
[3]: https://java.com
[4]: http://code.google.com/p/google-api-java-client/

