# Cloud Run Pub/Sub Tutorial Sample

This sample shows how to create a service that processes Pub/Sub messages.

Use it with the [Cloud Pub/Sub with Cloud Run tutorial](http://cloud.google.com/run/docs/tutorials/pubsub).

[![Run in Google Cloud][run_img]][run_link]

[run_img]: https://storage.googleapis.com/cloudrun/button.svg
[run_link]: https://console.cloud.google.com/cloudshell/editor?shellonly=true&cloudshell_image=gcr.io/cloudrun/button&cloudshell_git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&cloudshell_working_dir=run/cloudrun-pubsub

## Build

```
docker build --tag pubsub-tutorial:java .
```

## Run

```
docker run --rm -p 9090:8080 pubsub-tutorial:java
```

## Test

```
mvn clean verify
```

## Deploy

```
gcloud builds submit --tag gcr.io/${GOOGLE_CLOUD_PROJECT}/pubsub-tutorial
gcloud alpha run deploy pubsub-tutorial --image gcr.io/${GOOGLE_CLOUD_PROJECT}/pubsub-tutorial
```
