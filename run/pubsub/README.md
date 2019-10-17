# Cloud Run Pub/Sub Tutorial Sample

This sample shows how to create a service that processes Pub/Sub messages.

Use it with the [Cloud Pub/Sub with Cloud Run tutorial](http://cloud.google.com/run/docs/tutorials/pubsub).

[![Run in Google Cloud][run_img]][run_link]

[run_img]: https://storage.googleapis.com/cloudrun/button.svg
[run_link]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/cloudrun-pubsub

## Build

```
docker build --tag pubsub-tutorial:java .
```

## Run

```
PORT=8080 && docker run --rm -p 8080:${PORT} -e PORT=${PORT} pubsub-tutorial:java
```

## Test

```
mvn clean verify
```

## Deploy

```
gcloud builds submit --tag gcr.io/${GOOGLE_CLOUD_PROJECT}/pubsub-tutorial
gcloud beta run deploy pubsub-tutorial --image gcr.io/${GOOGLE_CLOUD_PROJECT}/pubsub-tutorial
```
