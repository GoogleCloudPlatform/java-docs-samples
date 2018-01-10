# Google Cloud Tasks Pull Queue Samples

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=cloud-tasks/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Sample command-line program for interacting with the Google Cloud Tasks
API using pull queues.

Pull queues let you add tasks to a queue, then programatically remove
and interact with them. Tasks can be added or processed in any
environment, such as on Google App Engine or Google Compute Engine.

`PullQueue.java` is a simple command-line program to demonstrate listing
queues, creating tasks, and pulling and acknowledging tasks.

## Initial Setup

 * Set up a Google Cloud Project and enable billing.
 * Enable the
 [Cloud Tasks API](https://console.cloud.google.com/launcher/details/google/cloudtasks.googleapis.com).
 * Download and install the [Cloud SDK](https://cloud.google.com/sdk).
 * Download and install [Maven](http://maven.apache.org/install.html).


## Creating a queue

To create a queue using the Cloud SDK, use the following gcloud command:

```bash
gcloud alpha tasks queues create-pull-queue my-pull-queue
```

In this example, the queue will be named `my-pull-queue`.

## Running the Samples

From the project folder, build your project with:

```
mvn clean assembly:single
```

Optionally, you can set up your settings as environment variables:

```
export PROJECT_ID=<YOUR_PROJECT_ID>
export LOCATION_ID=<YOUR_ZONE>
export QUEUE_ID=<YOUR_QUEUE_NAME>
```

Next, create a task for a queue:

```
java -cp target/cloudtasks-1.0.0-jar-with-dependencies.jar \
    com.example.PullQueue create-task --project=$PROJECT_ID --location=$LOCATION_ID --queue=$QUEUE_ID
```

Finally, pull and acknowledge a task:

```
java -cp target/cloudtasks-1.0.0-jar-with-dependencies.jar \
    com.example.PullQueue pull-and-ack-task --project=$PROJECT_ID --location=$LOCATION_ID --queue=$QUEUE_ID
```
Note that usually, there would be a processing step in between pulling a task and acknowledging it.

