# Cloud Eventarc - Generic tutorial

This sample shows how to create a service that processes generic [CloudEvents](https://cloudevents.io/).

For more details on how to work with this sample read the [Google Cloud Run Java Samples README](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/run).

[![Run in Google Cloud][run_img]][run_link]

[run_img]: https://storage.googleapis.com/cloudrun/button.svg
[run_link]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/events-generic


## Dependencies

* **Spring Boot**: Web server framework.
* **Jib**: Container build tool.
* **Junit + SpringBootTest**: [development] Test running framework.
* **MockMVC**: [development] Integration testing support framework.

## Quickstart

Use the [Jib Maven Plugin](https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin) to build and push your container image:

```sh
mvn jib:build -Dimage gcr.io/$(gcloud config get-value project)/eventarc-generic
```

Deploy your Cloud Run service:

```sh
gcloud run deploy eventarc-generic \
--image gcr.io/$(gcloud config get-value project)/eventarc-generic
```

## Test

Test your Cloud Run service by sending CloudEvents: 

```sh
CLOUD_RUN_URL=$(gcloud run services describe eventarc-generic --platform managed --format 'value(status.url)')
curl -XPOST $CLOUD_RUN_URL \
-H "Content-Type: application/json" \
-H "ce-id: 1413058901901494" \
-H "ce-source: //books.googleapis.com/book/MY-BOOK" \
-H "ce-specversion: 1.0" \
-H "ce-type: my-type" \
-d '{"key": "value"}'
```

You may observe the Cloud Run service receiving an event in Cloud Logging.