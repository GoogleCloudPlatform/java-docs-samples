# Google Cloud Tasks Samples

This sample demonstrates using the Cloud Tasks client library. These code snippets
are not executable, but are a copy-and-pasteable resource for your own code
base. For executable code, see the [App Engine Quickstart](https://cloud.google.com/tasks/docs/quickstart-appengine).

See the [Cloud Tasks documentation](https://cloud.google.com/tasks/docs/) for more info on [Creating HTTP Target tasks](https://cloud.google.com/tasks/docs/creating-http-target-tasks).

`CreateHttpTask.java` constructs a task with an HTTP target and pushes it
to your queue.

`CreateHttpTask.java` constructs a task with an HTTP target and OIDC token and
pushes it to your queue.

## Initial Setup

 * Set up a Google Cloud Project and enable billing.
 * Enable the
 [Cloud Tasks API](https://console.cloud.google.com/launcher/details/google/cloudtasks.googleapis.com).
 * Download and install the [Cloud SDK](https://cloud.google.com/sdk).
 * Download and install [Maven](http://maven.apache.org/install.html).
 * Set up [Google Application Credentials](https://cloud.google.com/docs/authentication/getting-started).

## Creating a queue

To create a queue using the Cloud SDK, use the following `gcloud` command:

```
gcloud tasks queues create <QUEUE_NAME>
```

The location of your queue is the same as your Google Cloud Project. It can be discovered by using the following `gcloud` command:

```
gcloud tasks queues describe <QUEUE_NAME>
```
the location embedded in the "name" value (for instance, if the name is
"projects/my-project/locations/us-central1/queues/my-queue", then the
location is "us-central1").

## Creating Tasks with HTTP Targets

Set an endpoint to your task handler by replacing the variable `url` with your
HTTP target in `CreateHttpTask.java`.

The sample will create a task and add it to your queue. As the queue processes
each task, it will send the task to the specific URL endpoint.

## Using HTTP Targets with Authentication Tokens

Set an endpoint to your task handler by replacing the variable `url` with your
HTTP target in `CreateHttpTaskWithToken.java`.

Your Cloud Tasks [service account][sa],
(service-PROJECT_NUMBER@gcp-sa-cloudtasks.iam.gserviceaccount.com), must
have the role of: `Service Account Token Creator` to generate a tokens.

Create or use an existing [service account][sa] to authenticate the OIDC token.

[sa]: https://cloud.google.com/iam/docs/service-accounts
