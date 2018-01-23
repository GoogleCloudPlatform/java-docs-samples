# App Engine Flexible Environment - Pub/Sub Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=flexible/pubsub/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


## Clone the sample app

Copy the sample apps to your local machine, and cd to the pubsub directory:

```
git clone https://github.com/GoogleCloudPlatform/java-docs-samples
cd java-docs-samples/flexible/pubsub
```

## Setup

Make sure [`gcloud`](https://cloud.google.com/sdk/docs/) is installed and authenticated.

Create a topic
```
gcloud beta pubsub topics create <your-topic-name>
```

Create a push subscription, to send messages to a Google Cloud Project URL
 such as https://<your-project-id>.appspot.com/push.
```
gcloud beta pubsub subscriptions create <your-subscription-name> \
  --topic <your-topic-name> \
  --push-endpoint \
  https://<your-project-id>.appspot.com/pubsub/push?token=<your-verification-token> \
  --ack-deadline 30
```
## Run

Set the following environment variables and run using shown Maven command. You can then
direct your browser to `http://localhost:8080/`

```
export PUBSUB_TOPIC=<your-topic-name>
export PUBSUB_VERIFICATION_TOKEN=<your-verification-token>
mvn jetty:run
```


### Send fake subscription push messages with:

```
curl -H "Content-Type: application/json" -i --data @sample_message.json
"localhost:8080/pubsub/push?token=<your-token>"
```

## Deploy

Update the environment variables `PUBSUB_TOPIC` and `PUBSUB_VERIFICATION_TOKEN` in [`app.yaml`](src/main/appengine/app.yaml),
then:

```
mvn appengine:deploy
```

The home page of this application provides a form to publish messages and also provides a view of the most recent messages
received over the push endpoint and persisted in storage.