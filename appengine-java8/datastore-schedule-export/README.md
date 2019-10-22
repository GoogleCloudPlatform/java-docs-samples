# Scheduling a Cloud Datastore export

This Google App Engine (GAE) app receives export requests at `/cloud-datastore-export` and
sends an export request to the [Cloud Datastore Admin API](https://cloud.google.com/datastore/docs/reference/admin/rest/v1/projects/export). 

## Before you begin

This app requires the following to complete export operations:

1. A Google Cloud project with billing enabled.
1. A Cloud Storage bucket for your export files.
1. The App Engine default service account must have permission
to write to the Cloud Storage bucket and have the Cloud Datastore Import Export Admin IAM role.

For more information on completing these requirements, see the
[Cloud Datastore documentation](https://cloud.google.com/datastore/docs/schedule-export#before_you_begin).

## Deploying

Set the target project in gcloud:

    gcloud config set project PROJECT_NAME

Deploy the GAE app:

    mvn appengine:deploy

The app takes the following parameters:

* `output_url_prefix` (required)-specifies where to save your Cloud Datastore export. If the URL ends with a `/`, it's used as is. Otherwise, the app adds a timesamp to the url.
* `kind` (optional, multiple)-restricts export to only these kinds.
* `namespace_id` (optional, multiple)-restricts export to only these namespaces.

Modify and deploy the cronjob:

    gcloud app deploy cron.yaml

You can test your cron job by running the job manually:

<a href="https://console.cloud.google.com/appengine/cronjobs">Open the Cron Jobs page</a>
