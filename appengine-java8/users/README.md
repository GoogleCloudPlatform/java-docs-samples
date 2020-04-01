# Users Authentication sample for Google App Engine

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/users/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use the [Users API][appid] on [Google App
Engine][ae-docs].

[appid]: https://cloud.google.com/appengine/docs/java/users/
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Running locally
This example uses the
[Maven gcloud plugin](https://cloud.google.com/appengine/docs/java/tools/using-maven).
To run this sample locally:

    $ mvn appengine:run

## Deploying
In the following command, replace YOUR-PROJECT-ID with your
[Google Cloud Project ID](https://developers.google.com/console/help/new/#projectnumber).

    $ mvn clean package appengine:deploy
