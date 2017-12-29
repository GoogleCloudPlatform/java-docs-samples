# Getting Started with BigQuery Data Transfer API

[BigQuery Data Transfer Service][BigQuery Data Transfer] features an API that
allows developers to create transfer jobs from data sources to BigQuery.
These sample Java applications demonstrate how to access the BigQuery Data
Transfer API using the [Google Cloud Client Library for
Java][google-cloud-java].

[BigQuery Data Transfer]: https://cloud.google.com/bigquery/docs/transfer-service-overview
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

	mvn clean package -DskipTests

You can then run a given `ClassName` via:

	mvn exec:java -Dexec.mainClass=com.example.bigquerydatatransfer.ClassName \
	    -DpropertyName=propertyValue \
		-Dexec.args="any arguments to the app"

### Listing available data sources

    mvn exec:java -Dexec.mainClass=com.example.bigquerydatatransfer.QuickstartSample \
        -Dexec.args='YOUR_PROJECT_ID'
