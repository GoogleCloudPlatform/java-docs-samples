# App Identity sample for Google App Engine

Samples for the Java 8 runtime can be found [here](/appengine-java8).

This sample demonstrates how to use the [App Identity API][appid] on [Google App
Engine][ae-docs].

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/appidentity/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[appid]: https://cloud.google.com/appengine/docs/java/appidentity/
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Running locally
This example uses the
[Maven Cloud SDK plugin](https://cloud.google.com/appengine/docs/java/tools/using-maven).
To run this sample locally:

    $ mvn appengine:run

## Deploying

    $ mvn appengine:deploy
