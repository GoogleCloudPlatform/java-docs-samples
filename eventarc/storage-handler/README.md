# Eventarc - Cloud Storage Events

This sample shows how to create a service that processes GCS events.

For more details on how to work with this sample read the [Google Cloud Run Java Samples README](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/main/run).

[![Run in Google Cloud][run_img]][run_link]

## Dependencies

* **Spring Boot**: Web server framework.
* **Junit + SpringBootTest**: [development] Test running framework.
* **MockMVC**: [development] Integration testing support framework.

## Setup

Configure environment variables:

```sh
export MY_RUN_SERVICE=gcs-service
export MY_GCS_TRIGGER=gcs-trigger
export MY_GCS_BUCKET="$(gcloud config get-value project)-gcs-bucket"
export SERVICE_ACCOUNT=gcs-trigger-svcacct
export PROJECT_ID=$(gcloud config get-value project)
```

## Quickstart

Deploy your Cloud Run service:

```sh
gcloud run deploy $MY_RUN_SERVICE \
--source .
--region us-central1
```

Create a _single region_ Cloud Storage bucket:

```sh
gsutil mb -p $PROJECT_ID -l us-central1 gs://"$MY_GCS_BUCKET"
```

Create a Service Account for Eventarc trigger

```
gcloud iam service-accounts create $SERVICE_ACCOUNT
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:$SERVICE_ACCOUNT@$PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/eventarc.eventReceiver" \
  --role="roles/run.invoker"
```

Create a Cloud Storage trigger:

```sh
gcloud eventarc triggers create $MY_GCS_TRIGGER \
--destination-run-service=$MY_RUN_SERVICE \
--destination-run-region=us-central1 \
--event-filters="type=google.cloud.storage.object.v1.finalized" \
--event-filters="bucket=$MY_GCS_BUCKET" \
--service-account=$SERVICE_ACCOUNT@$PROJECT_ID.iam.gserviceaccount.com
```

## Test

Test your Cloud Run service by creating a GCS event:

```sh
touch testfile.txt
gsutil copy testfile.txt gs://$MY_GCS_BUCKET
```

Observe the Cloud Run service printing upon receiving an event in Cloud Logging:

```sh
gcloud logging read "resource.type=cloud_run_revision AND \
resource.labels.service_name=$MY_RUN_SERVICE" --project \
$PROJECT_ID --limit 30 --format 'value(textPayload)'
```

[run_img]: https://storage.googleapis.com/cloudrun/button.svg
[run_link]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=eventarc/storage-handler
