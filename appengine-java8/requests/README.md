# Request Handling sample for Google App Engine

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/requests/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample provides Java code samples in support of the "Handling Requests" description  [Requests][requests-doc] on [Google App
Engine][ae-docs].

[requests-doc]: https://cloud.google.com/appengine/docs/java/requests
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Setup

    gcloud init

## Running locally
This example uses the
[Cloud SDK Maven plugin](https://cloud.google.com/appengine/docs/java/tools/using-maven).
To run this sample locally:

    mvn appengine:run

To see the results of the RequestsServlet, open `localhost:8080/requests` in a WWW browser.

To see the results of the LoggingServlet, open `localhost:8080/requests/log` in a browser
and examine the logs to see the actual messages.

## Deploying
In the following command, replace YOUR-PROJECT-ID with your
[Google Cloud Project ID](https://developers.google.com/console/help/new/#projectnumber)
and SOME-VERSION with a valid version number.

    mvn appengine:deploy

