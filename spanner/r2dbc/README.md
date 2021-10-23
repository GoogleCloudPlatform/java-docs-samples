# Cloud Spanner R2DBC Example

This sample application demonstrates using Spring Data R2DBC with [Google Cloud Spanner](https://cloud.google.com/spanner/).

## Maven
This sample uses the [Apache Maven][maven] build system. Before getting started, be
sure to [download][maven-download] and [install][maven-install] it. When you use
Maven as described here, it will automatically download the needed client
libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

## Setup

1.  Follow the set-up instructions in [the documentation](https://cloud.google.com/java/docs/setup).

2.  Enable APIs for your project.
    [Click here](https://console.cloud.google.com/flows/enableapi?apiid=spanner.googleapis.com&showconfirmation=true)
    to visit Cloud Platform Console and enable the Google Cloud Spanner API.

3.  Create a Cloud Spanner instance and database via the Cloud Plaform Console's
    [Cloud Spanner section](http://console.cloud.google.com/spanner).

4.  Enable application default credentials by running the command `gcloud auth application-default login`.

## Run the Example

1. Set up the following environment variables to help the application locate your database:

    ````
    export project=[PROJECT]
    export instance=[INSTANCE]
    export database=[DATABASE]
    ````

2. Then run the application from command line, after switching to this directory:

    ````
    mvn spring-boot:run
    ````

3. Go to http://localhost:8080/index.html and experiment with it.
You'll be able to create and drop a simple table called `NAMES`, containing two columns: a unique identifier (`UUID`) and a single data column called `NAME`.

All functionality is done through the Spring Data objects that were automatically configured by Spring Boot.