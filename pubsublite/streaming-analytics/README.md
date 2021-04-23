# Pub/Sub Lite with Cloud Dataflow

[![Open in Cloud Shell](http://gstatic.com/cloudssh/images/open-btn.svg)](https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=pubsublite/streaming-analytics/README.md)

Samples showing how to use [Pub/Sub Lite] with [Cloud Dataflow].

## Pub/Sub Lite to Cloud Storage sample

[PubsubliteToGcs.java](src/main/java/examples/PubsubliteToGcs.java)

This sample shows how to create an [Apache Beam] streaming pipeline that reads
messages from [Pub/Sub Lite], group the messages using a fixed-sized windowing
function, and writes them to [Cloud Storage].

Resources needed for this example:

1. A pair of Pub/Sub Lite topic and subscription. 
2. A Cloud Storage bucket.

### Setting up

1. [Enable the APIs](https://console.cloud.google.com/flows/enableapi?apiid=dataflow,compute_component,logging,storage_api,pubsublite.googleapis.com): Cloud Dataflow, Compute Engine, Cloud Logging, Cloud Storage, Pub/Sub Lite.
  > _When you enable Cloud Dataflow, which uses Compute Engine, a default Compute Engine service account with the Editor role (`roles/editor`) is created._
2. You can skip this step if you are trying this example in a Google Cloud environment like Cloud Shell.

   Otherwise, [create](https://cloud.google.com/iam/docs/creating-managing-service-accounts#iam-service-accounts-create-gcloud) a user-managed service account and grant it the following roles on your project:
   - `roles/dataflow.admin`
   - `roles/pubsublite.viewer`
   - `roles/pubsublite.subscriber`
   - `roles/logging.viewer`

   Then [create](https://cloud.google.com/iam/docs/creating-managing-service-account-keys#iam-service-account-keys-create-gcloud) a service account key and point  `GOOGLE_APPLICATION_CREDNETIALS` to your downloaded key file.
```bash
export GOOGLE_APPLICATION_CREDENTIALS=path/to/your/key/file
```
3. Create a Cloud Storage bucket. Your bucket name needs to be globally unique.
```bash
export PROJECT_ID=$(gcloud config get-value project)
export BUCKET=your-gcs-bucket

gsutil mb gs://$BUCKET
``` 
4. Create a Pub/Sub Lite topic and subscription. Set `LITE_LOCATION` to a [Pub/Sub Lite location].
```bash
export TOPIC=your-lite-topic
export SUBSCRIPTION=your-lite-subscription
export LITE_LOCATION=your-lite-location

gcloud pubsub lite-topics create $TOPIC \
    --zone=$LITE_LOCATION \
    --partitions=1 \
    --per-partition-bytes=30GiB
gcloud pubsub lite-subscriptions create $SUBSCRIPTION \
    --zone=$LITE_LOCATION \
    --topic=$TOPIC
```
5. Set `DATAFLOW_REGION` to a [Dataflow region] close to your Pub/Sub Lite location.
```
export DATAFLOW_REGION=your-dateflow-region
```
   
### Running the example

[PubsubliteToGcs.java](src/main/java/examples/PubsubliteToGcs.java)

The following example runs a streaming pipeline. Choose `DirectRunner` to test it locally or `DataflowRunner` to run it on Dataflow.

+ `--subscription`: the Pub/Sub Lite subscription to read messages from
+ `--output`: the full filepath of the output files
+ `--windowSize [optional]`: the window size in minutes, defaults to 1
+ `--runner [optional]`: `DataflowRunner` or `DirectRunner`
+ `--project [optional]`: your project ID, optional if using `DirectRunner`
+ `--region [optional]`: the Dataflow region, optional if using `DirectRunner`
+ `--tempLocation`: a Cloud Storage location for temporary files, optional if using `DirectRunner`

```bash
mvn compile exec:java \
  -Dexec.mainClass=examples.PubsubliteToGcs \
  -Dexec.args="\
    --subscription=projects/$PROJECT_ID/locations/$LITE_LOCATION/subscriptions/$SUBSCRIPTION \
    --output=gs://$BUCKET/samples/output \
    --windowSize=1 \
    --runner=DataflowRunner \
    --project=$PROJECT_ID \
    --region=$DATAFLOW_REGION \
    --tempLocation=gs://$BUCKET/temp"
```

[Publish] some messages to your Lite topic. Then check for files in your Cloud Storage bucket.

```bash
gsutil ls "gs://$BUCKET/samples/output*"
```

## Cleaning up

1. Stop the pipeline. If you use `DirectRunner`, `Ctrl+C` to cancel. If you use `DataflowRunner`, [click](https://console.cloud.google.com/dataflow/jobs) on the job you want to stop, then choose "Cancel".

1. Delete the Lite topic and subscription.
```bash
gcloud pubsub lite-topics delete $TOPIC
gcloud pubsub lite-subscription delete $SUBSCRIPTION
```
   
1. Delete the Cloud Storage objects:
```bash
gsutil -m rm -rf "gs://$BUCKET/samples/output*"
gsutil rb gs://$BUCKET
```

[Apache Beam]: https://beam.apache.org/
[Pub/Sub Lite]: https://cloud.google.com/pubsub/lite/docs/
[Cloud Dataflow]: https://cloud.google.com/dataflow/docs/
[Cloud Storage]: https://cloud.google.com/storage/docs/
[Publish]: https://cloud.google.com/pubsub/lite/docs/publishing/
[Pub/Sub Lite location]: https://cloud.google.com/pubsub/lite/docs/locations/
[Dataflow region]: https://cloud.google.com/dataflow/docs/concepts/regional-endpoints/
