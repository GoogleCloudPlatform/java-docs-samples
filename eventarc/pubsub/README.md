# Cloud Eventarc - Pub/Sub tutorial

This sample shows how to create a service that processes Pub/Sub messages.

For more details on how to work with this sample read the [Google Cloud Run Java Samples README](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/run).

[![Run in Google Cloud][run_img]][run_link]

[run_img]: https://storage.googleapis.com/cloudrun/button.svg
[run_link]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/events-pubsub


## Dependencies

* **Spring Boot**: Web server framework.
* **Jib**: Container build tool.
* **Junit + SpringBootTest**: [development] Test running framework.
* **MockMVC**: [development] Integration testing support framework.

## Quickstart

Create a Cloud Pub/Sub topic:

```sh
gcloud pubsub topics create my-topic
```

Use the [Jib Maven Plugin](https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin) to build and push your container image:

```sh
mvn jib:build -Dimage gcr.io/$(gcloud config get-value project)/cloudrun-events-pubsub
```

Deploy your Cloud Run service:

```sh
gcloud run deploy cloudrun-events-pubsub \
--image gcr.io/$(gcloud config get-value project)/cloudrun-events-pubsub
 ```

Create a Cloud Pub/Sub trigger:

```sh
gcloud alpha events triggers create pubsub-trigger \
--target-service cloudrun-events-pubsub \
--type com.google.cloud.pubsub.topic.publish \
--parameters topic=my-topic
```

## Test

Test your Cloud Run service by publishing a message to the topic: 

```sh
gcloud pubsub topics publish my-topic --message="Hello there"
```

You may observe the Run service receiving an event in Cloud Logging.