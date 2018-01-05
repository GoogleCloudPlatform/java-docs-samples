# Pull Task Queue sample for Google App Engine

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/taskqueues-pull/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use [pull task queues][appid] on [Google App
Engine][ae-docs].

[appid]: https://cloud.google.com/appengine/docs/java/taskqueue/overview-pull
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Setup

    gcloud init

## Running locally
This example uses the
[Cloud SDK based maven plugin](https://cloud.google.com/appengine/docs/java/tools/using-maven).
To run this sample locally:

    mvn appengine:run

## Deploying

    mvn appengine:deploy

