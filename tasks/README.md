# Google Cloud Tasks Samples

This sample demonstrates using the Cloud Tasks client library.

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

To create a queue using the Cloud SDK, use the following gcloud command:

```
gcloud beta tasks queues create <QUEUE_NAME>
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
export QUEUE_ID=<QUEUE_NAME>
```

And finally the location ID, which can be discovered with
`gcloud beta tasks queues describe $QUEUE_ID`, with the location embedded in
the "name" value (for instance, if the name is
"projects/my-project/locations/us-central1/queues/my-queue", then the
location is "us-central1").

```
export LOCATION_ID=<YOUR_ZONE>
```

### Creating Tasks with HTTP Targets

Set an environment variable for the endpoint to your task handler. This is an
example url:
```
export URL=https://example.com/taskshandler
```

Running the sample will create a task and add it to your queue. As the queue
processes each task, it will send the task to the specific URL endpoint:

```
mvn exec:java@HttpTask"
```

### Using HTTP Targets with Authentication Tokens

Your Cloud Tasks [service account][sa],
(service-<project-number>@gcp-sa-cloudtasks.iam.gserviceaccount.com), must
have the role of: `Service Account Token Creator` to generate a tokens.

Create or use an existing [service account][sa] to replace `<SERVICE_ACCOUNT_EMAIL>`
in `CreateHttpTaskWithToken.java`. This service account will be used to
authenticate the OIDC token.

Running the sample with command:
```
mvn exec:java@WithToken"
```


[sa]: https://cloud.google.com/iam/docs/service-accounts
