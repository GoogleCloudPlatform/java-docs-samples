# App Engine Flexible Environment - Pub/Sub Sample

## Clone the sample app

Copy the sample apps to your local machine, and cd to the pubsub directory:

```
git clone https://github.com/GoogleCloudPlatform/java-docs-samples
cd java-docs-samples/flexible/pubsub
```

## Setup

Make sure `gcloud` is installed and authenticated.

Create a topic
```
gcloud beta pubsub topics create <your-topic-name>
```

Create a subscription, which includes specifying the endpoint to which the Pub/Sub server should send requests.
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
export PUBSUB_SUBSCRIPTION_ID=<your-subscription-id>
mvn jetty:run
```


### Send fake subscription push messages with:

```
curl -H "Content-Type: application/json" -i --data @sample_message.json
"localhost:8080/pubsub/push?token=<your-token>"
```

## Deploy

Update the environment variables `PUBSUB_TOPIC`, `PUBSUB_VERIFICATION_TOKEN` and `PUBSUB_SUBSCRIPTION_ID` in `app.yaml`,
then:

```
mvn appengine:deploy
```

The home page of this application provides a form to publish messages and also provides a view of the most recent messages
received over the push endpoint and persisted in storage.