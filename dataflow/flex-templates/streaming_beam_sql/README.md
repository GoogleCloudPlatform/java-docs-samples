# Dataflow flex templates - Streaming Beam SQL

[![Open in Cloud Shell](http://gstatic.com/cloudssh/images/open-btn.svg)](https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=dataflow/flex-templates/streaming_beam_sql/README.md)

📝 Docs: [Using Flex Templates](https://cloud.google.com/dataflow/docs/guides/templates/using-flex-templates)

Samples showing how to create and run an
[Apache Beam](https://beam.apache.org/) template with a custom Docker image on
[Google Cloud Dataflow](https://cloud.google.com/dataflow/docs/).

## Before you begin

Follow the
[Getting started with Google Cloud Dataflow](../README.md)
page, and make sure you have a Google Cloud project with billing enabled
and a *service account JSON key* set up in your `GOOGLE_APPLICATION_CREDENTIALS`
environment variable.
Additionally, for this sample you need the following:

1. [Enable the APIs](https://console.cloud.google.com/flows/enableapi?apiid=appengine.googleapis.com,cloudscheduler.googleapis.com,cloudbuild.googleapis.com):
    App Engine, Cloud Scheduler, Cloud Build.

1. Create a
    [Cloud Storage bucket](https://cloud.google.com/storage/docs/creating-buckets).

    ```sh
    export BUCKET="your-gcs-bucket"
    gsutil mb gs://$BUCKET
    ```

1. Create a
    [Pub/Sub topic](https://cloud.google.com/pubsub/docs/admin#creating_a_topic)
    and a
    [subscription](https://cloud.google.com/pubsub/docs/admin#creating_subscriptions)
    to that topic.
    This is a streaming source of data for the sample.

    ```sh
    # For simplicity we use the same topic name as the subscription name.
    export TOPIC="messages"
    export SUBSCRIPTION="$TOPIC"

    gcloud pubsub topics create $TOPIC
    gcloud pubsub subscriptions create --topic $TOPIC $SUBSCRIPTION
    ```

1. Create a
    [Cloud Scheduler job](https://cloud.google.com/scheduler/docs/quickstart)
    to publish "positive" and "negative" ratings every
    [1 and 2 minutes](https://cloud.google.com/scheduler/docs/configuring/cron-job-schedules).
    This publishes messages to the Pub/Sub source topic.

    ```sh
    # Create a publisher for "positive ratings" that publishes 1 message per minute
    # If an App Engine app does not exist for the project, this step will create one.
    gcloud scheduler jobs create pubsub positive-ratings-publisher \
      --schedule="* * * * *" \
      --topic="$TOPIC" \
      --message-body='{"url": "https://beam.apache.org/", "review": "positive"}'

    # Start the job.
    gcloud scheduler jobs run positive-ratings-publisher

    # Create and run another similar publisher for "negative ratings" that
    # publishes 1 message every 2 minutes.
    gcloud scheduler jobs create pubsub negative-ratings-publisher \
      --schedule="*/2 * * * *" \
      --topic="$TOPIC" \
      --message-body='{"url": "https://beam.apache.org/", "review": "negative"}'

    gcloud scheduler jobs run negative-ratings-publisher
    ```

1. Create a [BigQuery dataset](https://cloud.google.com/bigquery/docs/datasets).
    This is a table to write the output data.

    ```sh
    export PROJECT="$(gcloud config get-value project)"
    export DATASET="beam_samples"
    export TABLE="streaming_beam_sql"

    bq mk --dataset "$PROJECT:$DATASET"
    ```

1. Clone the
    [`java-docs-samples` repository](https://github.com/GoogleCloudPlatform/java-docs-samples)
    and navigate to the code sample.

    ```sh
    git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
    cd java-docs-samples/dataflow/flex-templates/streaming_beam_sql
    ```

## Pub/Sub to BigQuery with Beam SQL sample

This sample shows how to deploy an Apache Beam streaming pipeline that reads
[JSON encoded](https://www.w3schools.com/whatis/whatis_json.asp) messages from
[Pub/Sub](https://cloud.google.com/pubsub), uses
[Beam SQL](https://beam.apache.org/documentation/dsls/sql/overview/)
to transform the message data, and writes the results to a
[BigQuery](https://cloud.google.com/bigquery) table.

* [Dockerfile](Dockerfile)
* [StreamingBeamSql.java](src/main/java/org/apache/beam/samples/StreamingBeamSql.java)
* [pom.xml](pom.xml)
* [metadata.json](metadata.json)

### Build the Flex Template

> <details><summary>
> <i>(Optional)</i> Run the Apache Beam pipeline locally for development.
> <i>(Click to expand)</i>
> </summary>
>
> ```sh
> mvn compile exec:java \
>   -Dexec.mainClass=org.apache.beam.samples.StreamingBeamSql \
>   -Dexec.args="\
>     --project=$PROJECT \
>     --inputSubscription=$SUBSCRIPTION \
>     --outputTable=$PROJECT:$DATASET.$TABLE \
>     --tempLocation=gs://$BUCKET/samples/dataflow/temp"
> ```
>
> </details>

Build the Java project into an
[*Uber JAR* file](https://maven.apache.org/plugins/maven-shade-plugin/).

```sh
# Build and package the application as an uber-jar file.
mvn clean package

# (Optional) Note the size of the uber-jar file compared to the original.
ls -lh target/*.jar
```

This *Uber JAR* file has all the dependencies embedded so it.
You can run this file as a standalone application with no external
dependencies on other libraries.

To run a template, you need to create a *template spec* file containing all the
necessary information to run the job, such as the SDK information and metadata.

The [`metadata.json`](metadata.json) file contains additional information for
the template such as the `name`, `description`, and input `parameters` field.

We used
[regular expressions](https://docs.microsoft.com/en-us/dotnet/standard/base-types/regular-expression-language-quick-reference)
for validation on the input
[Pub/Sub subscription](https://cloud.google.com/pubsub/docs/admin#resource_names)
and [BigQuery table](https://cloud.google.com/bigquery/docs/tables#table_naming).

The template file must be created in a Cloud Storage location,
and is used to run a new Dataflow job.

A container image is created, which includes a self-contained application of your pipeline.
Images starting with `gcr.io/PROJECT/` are saved into your project's
Container Registry, where the image is accessible to other Google Cloud products.

```sh
export TEMPLATE_PATH="gs://$BUCKET/samples/dataflow/templates/streaming-beam-sql.json"
export TEMPLATE_IMAGE="gcr.io/$PROJECT/samples/dataflow/streaming-beam-sql:latest"

# Build the Flex Template.
gutil cp metadata.json $TEMPLATE_PATH
mvn compile com.google.cloud.tools:jib-maven-plugin:2.8.0:build  -Dimage=gcr.io/$PROJECT_ID/$IMAGE_NAME 
```

The template is now available through the template file in the Cloud Storage
location that you specified.

### Running a Flex Template pipeline

You can now run the Apache Beam pipeline in Dataflow by referring to the
template file and passing the template
[parameters](https://cloud.devsite.corp.google.com/dataflow/docs/guides/specifying-exec-params#setting-other-cloud-dataflow-pipeline-options)
required by the pipeline.

```sh
export REGION="us-central1"

# Run the template.
gcloud dataflow flex-template run "streaming-beam-sql-`date +%Y%m%d-%H%M%S`" \
    --template-file-gcs-location "$TEMPLATE_PATH" \
    --parameters inputSubscription="$SUBSCRIPTION" \
    --parameters outputTable="$PROJECT:$DATASET.$TABLE" \
    --region "$REGION"
```

Check the results in BigQuery by running the following query:

```sh
bq query --use_legacy_sql=false 'SELECT * FROM `'"$PROJECT.$DATASET.$TABLE"'`'
```

While this pipeline is running, you can see new rows appended into the BigQuery
table every minute.

You can manually publish more messages from the
[Cloud Scheduler page](https://console.cloud.google.com/cloudscheduler)
to see how that affects the page review scores.

You can also publish messages directly to a topic through the
[Pub/Sub topics page](https://console.cloud.google.com/cloudpubsub/topic/list)
by selecting the topic you want to publish to,
and then clicking the "Publish message" button at the top.
This way you can test your pipeline with different URLs,
just make sure you pass valid JSON data since this sample does not do any
error handling for code simplicity.

Try sending the following message and check back the BigQuery table about
a minute later.

```json
{"url": "https://cloud.google.com/bigquery/", "review": "positive"}
```

### Cleaning up

After you've finished this tutorial, you can clean up the resources you created
on Google Cloud so you won't be billed for them in the future.
The following sections describe how to delete or turn off these resources.

#### Clean up the Flex template resources

1. Stop the Dataflow pipeline.

    ```sh
    gcloud dataflow jobs list \
        --filter 'NAME:streaming-beam-sql AND STATE=Running' \
        --format 'value(JOB_ID)' \
      | xargs gcloud dataflow jobs cancel
    ```

1. Delete the template spec file from Cloud Storage.

    ```sh
    gsutil rm $TEMPLATE_PATH
    ```

1. Delete the Flex Template container image from Container Registry.

    ```sh
    gcloud container images delete $TEMPLATE_IMAGE --force-delete-tags
    ```

#### Clean up Google Cloud project resources

1. Delete the Cloud Scheduler jobs.

    ```sh
    gcloud scheduler jobs delete negative-ratings-publisher
    gcloud scheduler jobs delete positive-ratings-publisher
    ```

1. Delete the Pub/Sub subscription and topic.

    ```sh
    gcloud pubsub subscriptions delete $SUBSCRIPTION
    gcloud pubsub topics delete $TOPIC
    ```

1. Delete the BigQuery table.

    ```sh
    bq rm -f -t $PROJECT:$DATASET.$TABLE
    ```

1. Delete the BigQuery dataset, this alone does not incur any charges.

    > ⚠️ The following command also deletes all tables in the dataset.
    > The tables and data cannot be recovered.
    >
    > ```sh
    > bq rm -r -f -d $PROJECT:$DATASET
    > ```

1. Delete the Cloud Storage bucket, this alone does not incur any charges.

    > ⚠️ The following command also deletes all objects in the bucket.
    > These objects cannot be recovered.
    >
    > ```sh
    > gsutil rm -r gs://$BUCKET
    > ```

## Limitations

* You must use a Google-provided base image to package your containers using Docker.
* You cannot update streaming jobs using Flex Template.
* You cannot use FlexRS for Flex Template jobs.

📝 Docs: [Using Flex Templates](https://cloud.google.com/dataflow/docs/guides/templates/using-flex-templates)
