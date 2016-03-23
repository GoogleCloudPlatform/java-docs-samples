# Google Cloud Storage (GCS) and the Google Java API Client library

Google Cloud Storage Service features a REST-based API that allows developers to store and access arbitrarily-large objects. These sample Java applications demonstrate how to access the Google Cloud Storage JSON API using the Google Java API Client Libraries. For more information, read the [Google Cloud Storage JSON API Overview][1].

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

	mvn package

You can then run a given `ClassName` via:

	mvn exec:java -Dexec.mainClass=StorageSample \
		-Dexec.args="ABucketName"

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

