# Google Cloud Datastore Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/datastore/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use [Google Cloud Datastore][java-datastore]
from [Google App Engine standard environment][ae-docs].

[java-datastore]: https://cloud.google.com/appengine/docs/java/datastore/
[ae-docs]: https://cloud.google.com/appengine/docs/java/


## Running locally

This example uses the
[Cloud SDK Maven plugin](https://cloud.google.com/appengine/docs/java/tools/using-maven).
To run this sample locally:

    $ mvn appengine:run

To see the results of the sample application, open
[localhost:8080](http://localhost:8080) in a web browser.


## Deploying

In the following command, replace YOUR-PROJECT-ID with your
[Google Cloud Project ID](https://developers.google.com/console/help/new/#projectnumber)
and SOME-VERSION with a valid version number.

    $ mvn appengine:deploy
