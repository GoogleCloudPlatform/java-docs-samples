# A Java Task Queue example for Google App Engine

This sample demonstrates how to use the [TaskQueue API][taskqueue-api] on [Google App
Engine][ae-docs].

[taskqueue-api]: https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/taskqueue/package-summary
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Setup

    gcloud init

## Running locally
This example uses the
[Maven gcloud plugin](https://cloud.google.com/appengine/docs/java/tools/using-maven).
To run this sample locally:

    mvn appengine:run

Go to the site `localhost:8080` to add elements to the queue.  They will appear in the log as the result of the Enqueue servlet transmitting the data to the Worker servlet.

## Deploying

    mvn appengine:deploy

