# Google Cloud Tasks and App Engine Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/tasks/app/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use the [Cloud Tasks API][task-api] on [Google App
Engine][ae-docs].

[task-api]: https://cloud.google.com/tasks/docs/
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Initial Setup

 * Set up a Google Cloud Project and enable billing.
 * Enable the
 [Cloud Tasks API](https://console.cloud.google.com/launcher/details/google/cloudtasks.googleapis.com).
 * Download and install the [Cloud SDK](https://cloud.google.com/sdk).
 * Download and install [Maven](http://maven.apache.org/install.html).
 * Set up [Google Application Credentials](https://cloud.google.com/docs/authentication/getting-started).

## Creating a queue

To create a queue using the Cloud SDK, use the following gcloud command:

```
gcloud tasks queues create default
```

## Deploying the App Engine app

First, update the `projectId` and `locationId` variables to match the values of
your queue in file `Enqueue.java`. To find these values use the following gcloud
command:

```
gcloud tasks queues describe default
```

Second, [Using Maven and the App Engine Plugin](https://cloud.google.com/appengine/docs/flexible/java/using-maven)
deploy to Google App Engine:

```
mvn clean package appengine:deploy
```
