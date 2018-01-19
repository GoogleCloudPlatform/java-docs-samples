# Google Cloud API Showcase: Using Cloud Pub/Sub & Translate on App Engine Standard Java 8 Environment

This sample demonstrates how to use [Google Cloud Pub/Sub][pubsub] and [Google Translate][translate]
from [Google App Engine standard environment][ae-docs].

[pubsub]: https://cloud.google.com/pubsub/docs/
[translate]: https://cloud.google.com/translate/docs/
[ae-docs]: https://cloud.google.com/appengine/docs/java/

The home page of this application provides a form to publish messages using Google/Cloud PubSub (though publishing in
local development is currently not supported). The application then receives these published messages over a push
subscription endpoint, translates it from a source language into a target language, and finally stores in Google Cloud
Datastore.

The home page also provides a view of the most recently translated messages persisted in storage.

## Clone the sample app

Copy the sample apps to your local machine, and cd to the translate-pubsub directory:

```
git clone https://github.com/GoogleCloudPlatform/java-docs-samples
cd java-docs-samples/appengine-java8/translate-pubsub
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
- Enable [Pub/Sub](https://console.cloud.google.com/launcher/details/google/pubsub.googleapis.com) and 
  [Translate](https://console.cloud.google.com/launcher/details/google/translate.googleapis.com) APIs

- Choose a topic name and verification token.

  - Set the following environment variables. The verification token is used to ensure that the end point only handles
    requests that are sent matching the verification token. You can use `uuidgen` on MacOS X, Windows, and Linux to
    generate a unique verification token. There are also online tools to generate UUIDs, such as 
    [uuidgenerator.net][uuid].

```
export PUBSUB_TOPIC=<your-topic-name>
export PUBSUB_VERIFICATION_TOKEN=<your-verification-token>
```

[uuid]: https://www.uuidgenerator.net/

- Create a topic
```
gcloud pubsub topics create $PUBSUB_TOPIC
```

- Create a push subscription, to send messages to a Google Cloud Project URL such as
  https://<your-project-id>.appspot.com/push.

```
gcloud pubsub subscriptions create <your-subscription-name> \
  --topic $PUBSUB_TOPIC \
  --push-endpoint \
  https://<your-project-id>.appspot.com/pubsub/push?token=$PUBSUB_VERIFICATION_TOKEN \
  --ack-deadline 30
```

## Run locally
Run using shown Maven command. You can then direct your browser to `http://localhost:8080/` to see translated messages
created in the following step.

```
mvn appengine:run
```

## Send fake subscription push messages

```
   curl -H "Content-Type: application/json" -i --data @sample_message.json \
   "localhost:8080/pubsub/push?token=$PUBSUB_VERIFICATION_TOKEN"
```

## Deploy

Update the environment variables `PUBSUB_TOPIC` and `PUBSUB_VERIFICATION_TOKEN` in
[`appengine-web.xml`](src/main/webapp/WEB-INF/appengine-web.xml), then:

```
   mvn appengine:deploy
```

Direct your browser to `https://<your-project-id>.appspot.com`.
