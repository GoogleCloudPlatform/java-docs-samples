# Google Cloud Tasks App Engine Flexible Queue Samples

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=flexible/cloud-tasks/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This is a sample AppEngine app demonstrating use of the Cloud Tasks API
using App Engine Queues.

App Engine queues push tasks to an App Engine HTTP target. This
application can both create and receive Cloud Tasks.

## Initial Setup

 * Set up a Google Cloud Project and enable billing.
 * Enable the
 [Cloud Tasks API](https://console.cloud.google.com/launcher/details/google/cloudtasks.googleapis.com).
 * Download and install the [Cloud SDK](https://cloud.google.com/sdk).
 * Download and install [Maven](http://maven.apache.org/install.html).
 * Add an [AppEngine](https://pantheon.corp.google.com/appengine) App to the project

## Creating a queue

To create a queue using the Cloud SDK, use the following gcloud command:

```bash
gcloud alpha tasks queues create-appengine-queue my-appengine-queue
```

In this example, the queue will be named `my-appengine-queue`.

### Deploy Sample

To deploy this sample to your AppEngine Project, use the following
command:

```bash
gcloud config set project $YOUR_PROJECT_ID
mvn appengine:deploy
```

You can open a browser to your your application with:

```bash
gcloud app browse
```

You can also stream the logs for the application with:

```bash
gcloud app logs tail -s default
```

## Cloud Tasks in Action

You can create a Cloud Task by sending a POST request to the
application.

Optionally, you can set up your settings as environment variables:
```
export PROJECT_ID=<YOUR_PROJECT_ID>
export LOCATION_ID=<YOUR_ZONE>
export QUEUE_ID=<YOUR_QUEUE_NAME>
```

Next, you can send a POST request to trigger the `/create_task`
endpoint:
```bash
curl -d "project=$PROJECT_ID" \
     -d "location=$LOCATION_ID" \
     -d "queue=$QUEUE_ID" \
     -d "message=Hello World!" \
     --request POST https://<YOUR_PROJECT_URL>.appspot.com/create_task
```

This endpoint will create a Cloud Task to trigger the
`/example_task_handler` endpoint, which will be visible the
application's logs:
```
Received task with payload: Hello World!
```