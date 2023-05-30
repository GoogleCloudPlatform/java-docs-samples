# App Engine Flexible Environment - Pub/Sub Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=flexible/pubsub/README.md">
<img alt="Open in Cloud Shell" src="http://gstatic.com/cloudssh/images/open-btn.png"></a>

## Clone the sample app

Copy the sample apps to your local machine, and cd to the pubsub directory:

```sh
git clone https://github.com/GoogleCloudPlatform/java-docs-samples
cd java-docs-samples/flexible/pubsub
```

## Setup

Make sure [`gcloud`](https://cloud.google.com/sdk/docs/) is installed and
authenticated.

Create a topic

```sh
gcloud pubsub topics create <your-topic-name>
```

Create a push subscription, to send messages to a Google Cloud Project URL such
 as <https://<your-project-id>.appspot.com/push>.

```sh
gcloud pubsub subscriptions create <your-subscription-name> \
  --topic <your-topic-name> \
  --push-endpoint \
  https://<your-project-id>.appspot.com/pubsub/push?token=<your-verification-token> \
  --ack-deadline 30
```

## Deploy

Update the environment variables `PUBSUB_TOPIC` and `PUBSUB_VERIFICATION_TOKEN`
in [`app.yaml`](src/main/appengine/app.yaml), then:

```sh
mvn clean package appengine:deploy
```

The home page of this application provides a form to publish messages and also
provides a view of the most recent messages received over the push endpoint and
persisted in storage.
