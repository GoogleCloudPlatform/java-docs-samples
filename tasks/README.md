# Google Cloud Tasks Pull Queue Samples

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=tasks/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Sample command-line program for interacting with the Google Cloud Tasks
API using pull queues.

Pull queues let you add tasks to a queue, then programatically remove
and interact with them. Tasks can be added or processed in any
environment, such as on Google App Engine or Google Compute Engine.

`Quickstart.java` is a simple command-line program to demonstrate listing
queues, creating tasks, and pulling and acknowledging tasks.

## Initial Setup

 * Set up a Google Cloud Project and enable billing.
 * Enable the
 [Cloud Tasks API](https://console.cloud.google.com/launcher/details/google/cloudtasks.googleapis.com).
 * Download and install the [Cloud SDK](https://cloud.google.com/sdk).
 * Download and install [Maven](http://maven.apache.org/install.html).


## Creating a queue

To create a queue using the Cloud SDK, use the following gcloud command:

```
gcloud beta tasks queues create-pull-queue my-pull-queue
```

In this example, the queue will be named `my-pull-queue`.

## Running the Samples

From the project folder, build your project with:

```
mvn clean install
```

Optionally, you can set up your settings as environment variables:

```
export GOOGLE_CLOUD_PROJECT=<YOUR_PROJECT_ID>
export LOCATION_ID=<YOUR_ZONE>
export QUEUE_ID=my-pull-queue
```

Next, create a task for a queue:

```
mvn exec:java -Dexec.mainClass="com.example.Quickstart" \
    -Dexec.args="create-task \
    --project $GOOGLE_CLOUD_PROJECT --queue $QUEUE_ID --location $LOCATION_ID"
```

Finally, pull and acknowledge a task:

```
mvn exec:java -Dexec.mainClass="com.example.task.Quickstart" \
    -Dexec.args="lease-and-ack-task \
    --project $GOOGLE_CLOUD_PROJECT --queue $QUEUE_ID --location $LOCATION_ID"
```
Note that usually, there would be a processing step in between pulling a task and acknowledging it.
