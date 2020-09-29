# Cloud Eventarc - Cloud Storage via Audit Logs tutorial

This sample shows how to create a service that processes GCS events.

For more details on how to work with this sample read the [Google Cloud Run Java Samples README](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/run).

[![Run in Google Cloud][run_img]][run_link]

[run_img]: https://storage.googleapis.com/cloudrun/button.svg
[run_link]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/events-storage


## Dependencies

* **Spring Boot**: Web server framework.
* **Jib**: Container build tool.
* **Junit + SpringBootTest**: [development] Test running framework.
* **MockMVC**: [development] Integration testing support framework.

## Setup

Configure environment variables:

```sh
export MY_RUN_SERVICE=gcs-service
export MY_RUN_CONTAINER=gcs-container
export MY_GCS_TRIGGER=gcs-trigger
export MY_GCS_BUCKET="$(gcloud config get-value project)-gcs-bucket"
```

## Quickstart

Use the [Jib Maven Plugin](https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin) to build and push your container image:

```sh
mvn jib:build -Dimage gcr.io/$(gcloud config get-value project)/$MY_RUN_CONTAINER
```

Deploy your Cloud Run service:

```sh
gcloud run deploy $MY_RUN_SERVICE \
--image gcr.io/$(gcloud config get-value project)/$MY_RUN_CONTAINER \
--allow-unauthenticated
```

Create a _single region_ Cloud Storage bucket:

```sh
gsutil mb -p $(gcloud config get-value project) -l us-central1 gs://"$MY_GCS_BUCKET"
```

Create a Cloud Storage (via Audit Log) trigger:

```sh
gcloud alpha events triggers create $MY_GCS_TRIGGER \
--target-service $MY_RUN_SERVICE \
--type com.google.cloud.auditlog.event \
--parameters methodName=storage.buckets.update \
--parameters serviceName=storage.googleapis.com \
--parameters resourceName=projects/_/buckets/"$MY_GCS_BUCKET"
```

## Test

Test your Cloud Run service by creating a GCS event:

```sh
gsutil defstorageclass set NEARLINE gs://$MY_GCS_BUCKET
```

Observe the Cloud Run service printing upon receiving an event in Cloud Logging:

```sh
gcloud logging read "resource.type=cloud_run_revision AND \
resource.labels.service_name=$MY_RUN_SERVICE" --project \
$(gcloud config get-value project) --limit 30 --format 'value(textPayload)'
```
