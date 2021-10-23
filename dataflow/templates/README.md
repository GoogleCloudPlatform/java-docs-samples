# Cloud Dataflow Templates

[![Open in Cloud Shell](http://gstatic.com/cloudssh/images/open-btn.svg)](https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=dataflow/templates/README.md)

Samples showing how to create and run an
[Apache Beam](https://beam.apache.org/) template on
[Google Cloud Dataflow](https://cloud.google.com/dataflow/docs/).

## Before you begin

Follow the
[Getting started with Google Cloud Dataflow](../README.md)
page, and make sure you have a Google Cloud project with billing enabled
and a *service account JSON key* set up in your `GOOGLE_APPLICATION_CREDENTIALS` environment variable.
Additionally, for this sample you need the following:

1. Create a Cloud Storage bucket.

   ```sh
   export BUCKET=your-gcs-bucket
   gsutil mb gs://$BUCKET
   ```

1. Clone the `java-docs-samples` repository.

   ```sh
   git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
   ```

1. Navigate to the sample code directory.

   ```sh
   cd java-docs-samples/dataflow/templates
   ```

## Templates

### WordCount

* [WordCount.java](src/main/java/com/example/dataflow/templates/WordCount.java)
* [WordCount_metadata](WordCount_metadata)
* [pom.xml](pom.xml)

The following sample creates a WordCount Dataflow template showcasing different uses of `ValueProvider`s.

Make sure you have the following variables set up:

```bash
export PROJECT=$(gcloud config get-value project)
export BUCKET=your-gcs-bucket
export TEMPLATE_LOCATION=gs://$BUCKET/samples/dataflow/templates/WordCount
```

Then, to create the template in the desired Cloud Storage location.

```bash
# Create the template.
mvn compile exec:java \
  -Dexec.mainClass=com.example.dataflow.templates.WordCount \
  -Dexec.args="\
    --isCaseSensitive=false \
    --project=$PROJECT \
    --templateLocation=$TEMPLATE_LOCATION \
    --runner=DataflowRunner"

# Upload the metadata file.
gsutil cp WordCount_metadata "$TEMPLATE_LOCATION"_metadata
```

> For more information, see
> [Creating templates](https://cloud.google.com/dataflow/docs/guides/templates/creating-templates).

Finally, you can run the template via `gcloud` or through the
[GCP Console create Dataflow job page](https://console.cloud.google.com/dataflow/createjob).

```bash
export JOB_NAME=wordcount-$(date +'%Y%m%d-%H%M%S')
export INPUT=gs://apache-beam-samples/shakespeare/kinglear.txt

gcloud dataflow jobs run $JOB_NAME \
  --gcs-location $TEMPLATE_LOCATION \
  --parameters inputFile=$INPUT,outputBucket=$BUCKET
```

> For more information, see
> [Executing templates](https://cloud.google.com/dataflow/docs/guides/templates/executing-templates).

You can check your submitted jobs in the
[GCP Console Dataflow page](https://console.cloud.google.com/dataflow).

## Cleanup

To avoid incurring charges to your GCP account for the resources used:

```bash
# Remove only the files created by this sample.
gsutil -m rm -rf "$TEMPLATE_LOCATION*"
gsutil -m rm -rf "gs://$BUCKET/samples/dataflow/wordcount/"

# [optional] Remove the Cloud Storage bucket.
gsutil rb gs://$BUCKET
```
