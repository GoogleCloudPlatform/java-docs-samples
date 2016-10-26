# Getting Started with Cloud Storage and the Google Cloud Client libraries

[Google Cloud Storage][storage]  is unified object storage for developers and enterprises, from live
data serving to data analytics/ML to data archival.
These sample Java applications demonstrate how to access the Cloud Storage API using
the [Google Cloud Client Library for Java][google-cloud-java].

[storage]: https://cloud.google.com/storage/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

	mvn clean package -DskipTests

You can then run a given `ClassName` via:

	mvn exec:java -Dexec.mainClass=com.example.storage.ClassName \
	    -DpropertyName=propertyValue \
		-Dexec.args="any arguments to the app"

### Creating a new bucket (using the quickstart sample)

    mvn exec:java -Dexec.mainClass=com.example.storage.QuickstartSample \
        -Dexec.args="my-bucket-name"
