# Google Cloud Tasks App Engine Queue Samples

Sample command-line program for interacting with the Cloud Tasks API
using App Engine queues.

App Engine queues push tasks to an App Engine HTTP target. This directory
contains both the App Engine app to deploy, as well as the snippets to run
locally to push tasks to it, which could also be called on App Engine.

`CreateTask.java` is a simple command-line program to create
tasks to be pushed to the App Engine app.

`TaskServlet.java` is the main App Engine app. This app serves as an endpoint to receive
App Engine task attempts.


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
gcloud beta tasks queues create-app-engine-queue my-appengine-queue
```

Note: A newly created queue will route to the default App Engine service and
version unless configured to do otherwise.

## Deploying the App Engine app
[Using Maven and the App Engine Plugin](https://cloud.google.com/appengine/docs/flexible/java/using-maven)
& [Maven Plugin Goals and Parameters](https://cloud.google.com/appengine/docs/flexible/java/maven-reference)

```
mvn appengine:deploy
```

## Run the Sample Using the Command Line

Set environment variables:

First, your project ID:

```
export GOOGLE_CLOUD_PROJECT=<YOUR_GOOGLE_CLOUD_PROJECT>
```

Then the queue ID, as specified at queue creation time. Queue IDs already
created can be listed with `gcloud beta tasks queues list`.

```
export QUEUE_ID=my-appengine-queue
```

And finally the location ID, which can be discovered with
`gcloud beta tasks queues describe $QUEUE_ID`, with the location embedded in
the "name" value (for instance, if the name is
"projects/my-project/locations/us-central1/queues/my-appengine-queue", then the
location is "us-central1").

```
export LOCATION_ID=<YOUR_ZONE>
```

Create a task, targeted at the `/tasks/create` endpoint, with a payload specified:

```
mvn exec:java -Dexec.mainClass="com.example.task.CreateTask" \
    -Dexec.args="--project-id $GOOGLE_CLOUD_PROJECT \
    --queue $QUEUE_ID --location $LOCATION_ID --payload hello"
```

The App Engine app serves as a target for the push requests. It has an
endpoint `/tasks/create` that reads the payload (i.e., the request body) of the
HTTP POST request and logs it. The log output can be viewed with [Stackdriver Logging](https://console.cloud.google.com/logs/viewer?minLogLevel=0).

Create a task that will be scheduled for a time in the future using the
`--in-seconds` flag:

```
mvn exec:java -Dexec.mainClass="com.example.task.CreateTask" \
    -Dexec.args="--project-id $GOOGLE_CLOUD_PROJECT \
    --queue $QUEUE_ID --location $LOCATION_ID --payload hello --in-seconds 30"
```
