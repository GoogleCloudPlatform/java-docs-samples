# Google Cloud Tasks Samples

* 
Cloud Tasks is a whitelist-only Alpha. Request an invite here 
https://docs.google.com/forms/d/1g6yRocQ3wtdTArfO4JX8DoqOhYmsoTVgrlFnS0mV1bo/viewform?edit_requested=true
*

Sample program for interacting with the Cloud Tasks API.

This repo contains samples for interacting with Cloud Tasks pull queues
and App Engine queues.

`CloudTasksPullQueuesSnippets.java` is a simple command-line program to
demonstrate  listing queues, creating tasks for pull queues, and pulling and
acknowledging tasks.

The `com.google.cloud.tasks.appenginequeues` package contains both an App Engine
servlet app to target the tasks at, as well as a command line tool to create
sample tasks with a payload. The servlet app will store the payload in Cloud
Datastore so you can verify the task has run.

## Prerequisites to run locally:

The samples require a Java environment with
[Maven](https://maven.apache.org/what-is-maven.html) installed.

All samples require a Google Cloud Project whitelisted for the Cloud Tasks API.
To create a project and enable the API, go to the [Google Developers
Console](https://console.developer.google.com). From there,

    * Enable the Cloud Tasks API
    * Enable the Cloud Datastore API (used by the sample App Engine app)

To build the client library, run the following commands:

    * cd cloud-tasks-client
    * mvn install

To install the Java application dependencies, run the following commands:

    * mvn install

### Authentication

To set up authentication locally, download the
[Cloud SDK](https://cloud.google.com/sdk), and run

    gcloud auth application-default login

On App Engine, authentication credentials will be automatically detected.

On Compute Engine and Container Engine, authentication credentials will be
automatically detected, but the instances must have been created with the
necessary scopes.

In any other environment, for example Compute Engine instance without the
necessary scopes, you should set `GOOGLE_APPLICATION_CREDENTIALS` environment
variable to a JSON key file for a service account.

See the [authentication guide](https://cloud.google.com/docs/authentication)
for more information.

## Creating the queues

Queues can not currently be created by the API. To create the queues using the
Cloud SDK, use the provided queue.yaml:

    gcloud app deploy queue.yaml

After running this command, a pull queue with queue ID `my-pull-queue` and an
App Engine queue with a queue ID of `my-appengine-ID` will be created.

## Running the Pull Queue Samples

Set the following environment variables

    export GOOGLE_CLOUD_PROJECT=your-google-cloud-project-id
    export LOCATION_ID=us-central1
    export QUEUE_ID=my-pull-queue

Run:

    ./run_pull_queue_example.sh $GOOGLE_CLOUD_PROJECT $LOCATION_ID $QUEUE_ID

The sample program will list queues, create a task, pull the task, and
acknowledge the task.

## Running the App Engine Queue samples

### Deploying the App Engine app

To deploy the App Engine app:

    mvn clean appengine:deploy

Verify the index page is serving:

    gcloud app browse

The App Engine app serves as a target for the push requests. It has an
endpoint `/payload` that stores the payload it receives in the HTTP POST
data in Cloud Datastore. The payload can be accessed in your browser at the
same `/payload` endpoint with a GET request.

### Creating a Task targeting the App Engine app

Set the following environment variables

    export GOOGLE_CLOUD_PROJECT=your-google-cloud-project-id
    export LOCATION_ID=us-central1
    export QUEUE_ID=my-appengine-queue
    export QUEUE_NAME=projects/$GOOGLE_CLOUD_PROJECT/locations/$LOCATION_ID/queues/$QUEUE_ID
    export PAYLOAD=mytaskpayload

Now run the sample app to create a Cloud Task targeting your App Engine app.

    ./run_create_appengine_task.sh $QUEUE_NAME $PAYLOAD

Now if you view your App Engine app:

    gcloud app browse

You will see `mytaskpayload` value set from the command line argument above
has been set in Cloud Datastore.

You can look in `CloudTasksAppEngineQueueSnippets.java` to see how the Task
with an App Engine target was created.

## Testing the Samples

To run the integration tests, you must have deployed the App Engine app using
the instructions above.

Set the environment variable to your project variables:

    export GOOGLE_CLOUD_PROJECT=your-google-cloud-project-id
    export LOCATION_ID=us-central1
    export APPENGINE_QUEUE_ID=my-appengine-queue
    export PULL_QUEUE_ID=my-pull-queue

Then run:

    mvn install -DskipTests=False

## Contributing changes

Contributions are not accepted during Alpha.

## Licensing

* See [LICENSE](LICENSE)


