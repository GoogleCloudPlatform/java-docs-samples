# Getting Started with Google Translate API and the Google Cloud Client libraries

[Google Translate API][translate] provides a simple programmatic interface for translating an
arbitrary string into any supported language.
These sample Java applications demonstrate how to access the Cloud Storage API using
the [Google Cloud Client Library for Java][google-cloud-java].

[translate]: https://cloud.google.com/translate/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

	mvn clean package -DskipTests

You can then run a given `ClassName` via:

	mvn exec:java -Dexec.mainClass=com.example.translate.ClassName \
	    -DpropertyName=propertyValue \
		-Dexec.args="any arguments to the app"

### Translate a string (using the quickstart sample)

    mvn exec:java -Dexec.mainClass=com.example.translate.QuickstartSample \
        -Dexec.args="YOUR_API_KEY"
