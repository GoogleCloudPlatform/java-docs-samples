# Pub/Sub Lite with Cloud Dataflow

[![Open in Cloud
Shell](http://gstatic.com/cloudssh/images/open-btn.svg)](https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=pubsublite/streaming-analytics/README.md)

Samples showing how to use [Pub/Sub Lite] with [Cloud Dataflow].

## Pub/Sub Lite to Cloud Storage sample

[PubsubliteToGcs.java](src/main/java/examples/PubsubliteToGcs.java)

This sample shows how to create an [Apache Beam] streaming pipeline that reads
messages from [Pub/Sub Lite], group the messages using a fixed-sized windowing
function, and writes them to [Cloud Storage].

Resources needed for this example:

1. A pair of Pub/Sub Lite topic and subscription.
1. A Cloud Storage bucket.

### Setting up

1. [Enable the
   APIs](https://console.cloud.google.com/flows/enableapi?apiid=dataflow,compute_component,logging,storage_api,pubsublite.googleapis.com):
   Cloud Dataflow, Compute Engine, Cloud Logging, Cloud Storage, Pub/Sub Lite.

      > _When you enable Cloud Dataflow, which uses Compute Engine, a default
      > Compute Engine service account with the Editor role (`roles/editor`) is
      > created._

1. You can skip this step if you are trying this example in a Google Cloud
   environment like Cloud Shell.

   Otherwise,
   [create](https://cloud.google.com/iam/docs/creating-managing-service-accounts#iam-service-accounts-create-gcloud)
   a user-managed service account and grant it the following roles on your
   project:
   - `roles/dataflow.admin`
   - `roles/pubsublite.viewer`
   - `roles/pubsublite.subscriber`
   - `roles/logging.viewer`

   Then
   [create](https://cloud.google.com/iam/docs/creating-managing-service-account-keys#iam-service-account-keys-create-gcloud)
   a service account key and point  `GOOGLE_APPLICATION_CREDNETIALS` to your
   downloaded key file.

   ```sh
   export GOOGLE_APPLICATION_CREDENTIALS=path/to/your/key/file
   ```

1. Create a Cloud Storage bucket. Your bucket name needs to be globally unique.

   ```sh
   export PROJECT_ID=$(gcloud config get-value project)
   export BUCKET=your-gcs-bucket

   gsutil mb gs://$BUCKET
   ```

1. Create a Pub/Sub Lite topic and subscription. Set `LITE_LOCATION` to a
   [Pub/Sub Lite location].

   ```sh
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

1. Set `DATAFLOW_REGION` to a [Dataflow region] close to your Pub/Sub Lite
   location.

   ```sh
   export DATAFLOW_REGION=your-dateflow-region
   ```

### Running the example

[PubsubliteToGcs.java](src/main/java/examples/PubsubliteToGcs.java)

The following example runs a streaming pipeline. Choose `DirectRunner` to test
it locally or `DataflowRunner` to run it on Dataflow.

- `--subscription`: the Pub/Sub Lite subscription to read messages from
- `--output`: the full filepath of the output files
- `--windowSize [optional]`: the window size in minutes, defaults to 1
- `--runner [optional]`: `DataflowRunner` or `DirectRunner`
- `--project [optional]`: your project ID, optional if using `DirectRunner`
- `--region [optional]`: the Dataflow region, optional if using `DirectRunner`
- `--tempLocation`: a Cloud Storage location for temporary files, optional if
  using `DirectRunner`

Gradle:

```sh
gradle execute -Dexec.args="\
    --subscription=projects/$PROJECT_ID/locations/$LITE_LOCATION/subscriptions/$SUBSCRIPTION \
    --output=gs://$BUCKET/samples/output \
    --windowSize=1 \
    --runner=DataflowRunner \
    --project=$PROJECT_ID \
    --region=$DATAFLOW_REGION \
    --tempLocation=gs://$BUCKET/temp"
```

Maven:

```sh
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

[Publish] some messages to your Lite topic. Then check for files in your Cloud
Storage bucket.

```sh
gsutil ls "gs://$BUCKET/samples/output*"
```

## (Optional) Creating a custom Dataflow template

With a [`metadata.md`](metadata.md), you can create a [Dataflow Flex template].
Custom Dataflow Flex templates can be shared. You can run them with different
input parameters.

1. Create a fat JAR. You should see
   `target/pubsublite-streaming-bundled-1.0.jar` as an output.

   ```sh
   mvn clean package -DskipTests=true
   ls -lh
   ```

1. Provide names and locations for your template file and template container
   image.

   ```sh
   export TEMPLATE_PATH="gs://$BUCKET/samples/pubsublite-to-gcs.json"
   export TEMPLATE_IMAGE="gcr.io/$PROJECT_ID/pubsublite-to-gcs:latest"
   ```

1. Build a custom Flex template.

   ```sh
   gcloud dataflow flex-template build $TEMPLATE_PATH \
   --image-gcr-path "$TEMPLATE_IMAGE" \
   --sdk-language "JAVA" \
   --flex-template-base-image JAVA11 \
   --metadata-file "metadata.json" \
   --jar "target/pubsublite-streaming-bundled-1.0.jar" \
   --env FLEX_TEMPLATE_JAVA_MAIN_CLASS="examples.PubsubliteToGcs"
   ```

1. Run a job with the custom Flex template using `gcloud` or in Cloud Console.

   > Note: Pub/Sub Lite allows only one subscriber to pull messages from one
   > partition. If your Pub/Sub Lite topic has only one partition and you use a
   > subscription attached to that topic in more than one Dataflow jobs, only one
   > of them will get messages.

   ```sh
   gcloud dataflow flex-template run "pubsublite-to-gcs-`date +%Y%m%d`" \
   --template-file-gcs-location "$TEMPLATE_PATH" \
   --parameters subscription="projects/$PROJECT_ID/locations/$LITE_LOCATION/subscriptions/$SUBSCRIPTION" \
   --parameters output="gs://$BUCKET/samples/template-output" \
   --parameters windowSize=1 \
   --region "$DATAFLOW_REGION" 
   ```

## Cleaning up

1. Stop the pipeline. If you use `DirectRunner`, `Ctrl+C` to cancel. If you use
   `DataflowRunner`, [click](https://console.cloud.google.com/dataflow/jobs) on
   the job you want to stop, then choose "Cancel".

1. Delete the Lite topic and subscription.

   ```sh
   gcloud pubsub lite-topics delete $TOPIC
   gcloud pubsub lite-subscription delete $SUBSCRIPTION
   ```

1. Delete the Cloud Storage objects:

   ```sh
   gsutil -m rm -rf "gs://$BUCKET/samples/output*"
   ```

1. Delete the template image in Cloud Registry and delete the Flex template if
   you have created them.

   ```sh
   gcloud container images delete $TEMPLATE_IMAGE
   gsutil rm $TEMPLATE_PATH
   ```

1. Delete the Cloud Storage bucket:

   ```sh
   gsutil rb "gs://$BUCKET"
   ```

[Apache Beam]: https://beam.apache.org/
[Pub/Sub Lite]: https://cloud.google.com/pubsub/lite/docs/
[Cloud Dataflow]: https://cloud.google.com/dataflow/docs/
[Cloud Storage]: https://cloud.google.com/storage/docs/
[Publish]: https://cloud.google.com/pubsub/lite/docs/publishing/
[Pub/Sub Lite location]: https://cloud.google.com/pubsub/lite/docs/locations/
[Dataflow region]: https://cloud.google.com/dataflow/docs/concepts/regional-endpoints/
[Dataflow Flex template]: https://cloud.google.com/dataflow/docs/guides/templates/using-flex-templates
