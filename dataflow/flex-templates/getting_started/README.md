# Dataflow flex template: Getting started sample

## Before you begin

Make sure you have followed the
[Dataflow setup instructions](../../README.md).

## Create a Cloud Storage bucket

```sh
export BUCKET="your-bucket"
gcloud storage buckets create gs://$BUCKET
```

## Create an Artifact Registry repository

```sh
export REGION="us-central1"
export REPOSITORY="your-repository"

gcloud artifacts repositories create $REPOSITORY \
    --repository-format=docker \
    --location=$REGION
```

## Build the JAR file

```sh
mvn clean package
```

## Build the template

```sh
export PROJECT="project-id"

gcloud dataflow flex-template build gs://$BUCKET/getting_started_java.json \
    --image-gcr-path "$REGION-docker.pkg.dev/$PROJECT/$REPOSITORY/getting-started-java:latest" \
    --sdk-language "JAVA" \
    --flex-template-base-image JAVA11 \
    --metadata-file "metadata.json" \
    --jar "target/flex-template-getting-started-1.0.jar" \
    --env FLEX_TEMPLATE_JAVA_MAIN_CLASS="com.example.dataflow.FlexTemplateGettingStarted"
```

## Run the template

```sh

gcloud dataflow flex-template run "flex-`date +%Y%m%d-%H%M%S`"  \
    --template-file-gcs-location "gs://$BUCKET/getting_started_java.json" \
    --region $REGION \
    --parameters output="gs://$BUCKET/output-"
```

## Clean up

To delete the resources that you created:

```sh
gcloud artifacts repositories delete $REPOSITORY --location $REGION --quiet
gcloud storage rm gs://$BUCKET --recursive
```


## What's next?

For more information about building and running flex templates, see
📝 [Use Flex Templates](https://cloud.google.com/dataflow/docs/guides/templates/using-flex-templates).

