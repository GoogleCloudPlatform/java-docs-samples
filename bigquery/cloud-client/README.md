# Getting Started with BigQuery and the Google Java API Client library

[Google's BigQuery Service][BigQuery] features a REST-based API that allows
developers to create applications to run ad-hoc queries on massive datasets.
These sample Java applications demonstrate how to access the BigQuery API using
the [Google Cloud Client Library for Java][google-cloud-java].

[BigQuery]: https://cloud.google.com/bigquery/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/gcloud-java

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

	mvn clean package -DskipTests

You can then run a given `ClassName` via:

	mvn exec:java -Dexec.mainClass=com.example.bigquery.ClassName \
	    -DpropertyName=propertyValue \
		-Dexec.args="any arguments to the app"

### Running a synchronous query

    mvn exec:java -Dexec.mainClass=com.example.bigquery.SyncQuerySample \
        -Dquery='SELECT corpus FROM `publicdata.samples.shakespeare` GROUP BY corpus;' \
        -DuseLegacySql=false
