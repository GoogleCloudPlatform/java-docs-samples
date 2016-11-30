# App Engine Flexible Environment - Pub/Sub Sample

## Clone the sample app

Copy the sample apps to your local machine, and cd to the pubsub directory:

```
git clone https://github.com/GoogleCloudPlatform/java-docs-samples
cd java-docs-samples/flexible/pubsub
```

## Create a topic and subscription

Create a topic and subscription, which includes specifying the
endpoint to which the Pub/Sub server should send requests:

```
gcloud beta pubsub topics create <your-topic-name>
gcloud beta pubsub subscriptions create <your-subscription-name> \
  --topic <your-topic-name> \
  --push-endpoint \
  https://<your-project-id>.appspot.com/pubsub/push?token=<your-token> \
  --ack-deadline 30
```

## Run

Make sure `gcloud` is installed and authenticated. Set the following
environment variables and run using shown Maven command. You can then
direct your browser to  `http://localhost:8080/`

```
export PUBSUB_VERIFICATION_TOKEN=<your-token>
export PUBSUB_TOPIC=<your-topic-name>
export GOOGLE_CLOUD_PROJECT=<your-project-id>

mvn jetty:run
```


### Send fake subscription push messages with:

```
curl -i --data @sample_message.json
"localhost:8080/pubsub/push?token=<your-token>"
```

## Deploy

Put topic and token in `app.yaml`, then:

```
mvn appengine:deploy
```

## Test
Tests use a live service and will create necessary topic and 
subscription for tests. 

Set the following environment variables before running tests.
```
export PUBSUB_VERIFICATION_TOKEN=<your-token>
export PUBSUB_TOPIC=<your-test-topic-name>
export PUBSUB_SUBSCRIPTION=<your-test-topic-subscription-name>
export DATASTORE_TEST_ENTRY_KIND=<your-test-entry-id>
export GOOGLE_CLOUD_PROJECT=<your-project-id>
```

Run tests in terminal using Maven
```
mvn verify
```
