# Cloud Spanner JDBC Example

This sample application demonstrates using JDBC with [Google Cloud Spanner](https://cloud.google.com/spanner/).

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

Run the following command on the command line in the project directory:

```
mvn clean compile exec:java -Dexec.args="<command> my-instance my-database"
```
