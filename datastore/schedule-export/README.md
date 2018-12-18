# Scheduling a Cloud Datastore export

This Google App Engine (GAE) app receives export requests at `/cloud-datastore-export` and
sends an export request to the [Cloud Datastore Admin API](https://cloud.google.com/datastore/docs/reference/admin/rest/v1/projects/export). You must give the GAE service account permission to initiate Cloud Datastore exports and
to write to the targe Cloud Storage bucket, see [Scheduling an Export](https://cloud.google.com/datastore/docs/schedule-export) in the Cloud Datastore documentation.

## Deploying

Deploy the GAE app:

    mvn appengine:deploy

The app takes the following parameters:

* `output_url_prefix` (required)-specifies where to save your Cloud Datastore export. If the URL ends with a `/`, it's used as is. Otherwise, the app adds a timesamp to the url.
* 'kind' (optional, multiple)-restricts export to only these kinds.
* 'namespace_id' (optional, multiple)-restricts export to only these namespaces.

Modify and deploy the cronjob:

    gcloud app deploy cron.yaml

You can test your cron job by running the job manually:

<a href="https://console.cloud.google.com/appengine/cronjobs">Open the Cron Jobs page</a>
