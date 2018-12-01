# Scheduling a Cloud Datastore export

This Google App Engine (GAE) app receives export requests at `/cloud-datastore-export` and
sends an export request to the Cloud Datastore Admin API.

## Deploying

Deploy the GAE app:

    mvn appengine:deploy

Deploy the cronjob:

    gcloud app deploy cron.yaml

You can test your cron job by running the job manually:

<a href="https://console.cloud.google.com/appengine/cronjobs">Open the Cron Jobs page</a>