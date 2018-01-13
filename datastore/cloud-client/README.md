# Getting Started with Cloud Datastore and the Google Cloud Client libraries

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=datastore/cloud-client/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Google Cloud Datastore][Datastore] is a highly-scalable NoSQL database for your applications.
These sample Java applications demonstrate how to access the Datastore API using
the [Google Cloud Client Library for Java][google-cloud-java].

[Datastore]: https://cloud.google.com/datastore/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

	mvn clean package -DskipTests
	
You can then run a given `ClassName` via:

	mvn exec:java -Dexec.mainClass=com.example.bigquery.ClassName \
	    -DpropertyName=propertyValue \
		-Dexec.args="any arguments to the app"

### Creating a new entity (using the quickstart sample)

    mvn exec:java -Dexec.mainClass=com.example.datastore.QuickstartSample
