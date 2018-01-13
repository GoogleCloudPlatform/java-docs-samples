# Using Google Cloud Pub/Sub on App Engine Standard Java 8 Environment

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/pubsub/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use [Google Cloud Pub/Sub][pubsub]
from [Google App Engine standard environment][ae-docs].

[pubsub]: https://cloud.google.com/pubsub/docs/
[ae-docs]: https://cloud.google.com/appengine/docs/java/

The home page of this application provides a form to publish messages using Google/Cloud PubSub. The application
then receives these published messages over a push subscription endpoint and then stores in Google Cloud Datastore.
The home page provides a view of the most recent messages persisted in storage.

## Clone the sample app

Copy the sample apps to your local machine, and cd to the pubsub directory:

```
git clone https://github.com/GoogleCloudPlatform/java-docs-samples
cd java-docs-samples/appengine-java8/pubsub
```

## Setup

- Make sure [`gcloud`](https://cloud.google.com/sdk/docs/) is installed and initialized:
```
   gcloud init
```
- If this is the first time you are creating an App Engine project
```
   gcloud app create
```
- For local development, [set up](https://cloud.google.com/docs/authentication/getting-started) authentication
- [Enable](https://console.cloud.google.com/launcher/details/google/pubsub.googleapis.com) Pub/Sub API

- Create a topic
```
gcloud beta pubsub topics create <your-topic-name>
```

- Create a push subscription, to send messages to a Google Cloud Project URL such as https://<your-project-id>.appspot.com/push.

The verification token is used to ensure that the end point only handles requests that are sent matching the verification token.
You can use `uuidgen` on MacOS X, Windows, and Linux to generate a unique verification token.

```
gcloud beta pubsub subscriptions create <your-subscription-name> \
  --topic <your-topic-name> \
  --push-endpoint \
  https://<your-project-id>.appspot.com/pubsub/push?token=<your-verification-token> \
  --ack-deadline 30
```

## Run locally
Set the following environment variables and run using shown Maven command. You can then
direct your browser to `http://localhost:8080/`

```
export PUBSUB_TOPIC=<your-topic-name>
export PUBSUB_VERIFICATION_TOKEN=<your-verification-token>
mvn appengine:run
```

## Send fake subscription push messages with:

```
   curl -H "Content-Type: application/json" -i --data @sample_message.json
   "localhost:8080/pubsub/push?token=<your-token>"
```

## Deploy

Update the environment variables `PUBSUB_TOPIC` and `PUBSUB_VERIFICATION_TOKEN` in
[`appengine-web.xml`](src/main/webapp/WEB-INF/appengine-web.xml),
then:

```
   mvn appengine:deploy
```

Direct your browser to `https://project-id.appspot.com`.
