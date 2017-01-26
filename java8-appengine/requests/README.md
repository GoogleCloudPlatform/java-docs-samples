# Request Handling sample for Google App Engine

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
