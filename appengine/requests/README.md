# Request Handling sample for Google App Engine

**Java 7 runtime support on App Engine standard was [deprecated](https://cloud.google.com/appengine/docs/deprecations/java7) on
December 13, 2017 and will be shut down entirely on January 16, 2019. It is replaced by the
[Java 8 Runtime Environment](https://cloud.google.com/appengine/docs/standard/java/runtime-java8).**

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/requests/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Samples for the Java 8 runtime can be found [here](/appengine-java8).

This sample provides Java code samples in support of the "Handling Requests" description  [Requests][requests-doc] on [Google App
Engine][ae-docs].

[requests-doc]: https://cloud.google.com/appengine/docs/java/requests
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Running locally
This example uses the
[Maven gcloud plugin](https://cloud.google.com/appengine/docs/java/managed-vms/maven).
To run this sample locally:

    $ mvn appengine:devserver

To see the results of the RequestsServlet, open `localhost:8080` in a WWW browser.

To see the results of the LoggingServlet, open `localhost:8080/logs` in a WWW browser
and examine the logs to see the actual messages.

## Deploying
In the following command, replace YOUR-PROJECT-ID with your
[Google Cloud Project ID](https://developers.google.com/console/help/new/#projectnumber)
and SOME-VERSION with a valid version number.

    $ mvn appengine:update -Dappengine.appId=YOUR-PROJECT-ID -Dappengine.version=SOME-VERSION

## Setup
To save your project settings so that you don't need to enter the
 parameters, you can:

1. Update the `<application>` tag in src/main/webapp/WEB-INF/appengine-web.xml
   with your project name.

2. Update the `<version>` tag in src/main/webapp/WEB-INF/appengine-web.xml
   with a valid version number.


You will now be able to run

    $ mvn appengine:update

without the need for any additional parameters.
