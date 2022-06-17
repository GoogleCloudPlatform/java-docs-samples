# Bigtable workload generator

This is a tool to perform a high number of reads to a Bigtable table for
demonstration purposes. It is deployed as a Dataflow template, so it can easily
be run as a Dataflow job.

## Template

### Running

1. Set your environment variables

    ```
    TEMPLATE_PATH="gs://cloud-bigtable-dataflow-templates/generate-workload.json"
    INSTANCE_ID=YOUR-INSTANCE-ID
    TABLE_ID=YOUR-TABLE-ID
    REGION=us-central1
    WORKLOAD_QPS=100 # Optional, default to 1000
    WORKLOAD_DURATION=15 # Optional, default to 10
    ```

1. Run this command to start a job from dataflow template:

    ```
    JOB_NAME="generate-bigtable-workload-`date +%Y%m%d-%H%M%S`"
    gcloud dataflow flex-template run $JOB_NAME \
    --template-file-gcs-location "$TEMPLATE_PATH" \
    --parameters bigtableInstanceId="$INSTANCE_ID" \
    --parameters bigtableTableId="$TABLE_ID" \
    --region "$REGION" \
    --parameters workloadRate=$WORKLOAD_QPS
    --parameters workloadDurationMinutes=$WORKLOAD_DURATION
    ```

### Deploying a template instructions

These instructions are for maintenance of the workload generator, but if you 
would like to modify this example and deploy the template yourself, you can 
follow them to do so.

1. Build the project

    ```
    mvn clean package -DskipTests
    ```

1. Set the environment variables. To deploy a version on your project, update 
   these with your own resources as described in the [Using Flex Templates](https://cloud.google.com/dataflow/docs/guides/templates/using-flex-templates)
   documentation.

   ```
   export TEMPLATE_PATH="gs://cloud-bigtable-dataflow-templates/generate-workload.json"
   export TEMPLATE_IMAGE="gcr.io/cloud-bigtable-ecosystem/dataflow/generate-workload:latest"
   export LOGS_PATH="gs://cloud-bigtable-dataflow-templates-logs/workload-generator"
   ```

1. Deploy the template

   ```
   gcloud dataflow flex-template build $TEMPLATE_PATH \
   --image-gcr-path "$TEMPLATE_IMAGE" \
   --sdk-language "JAVA" \
   --flex-template-base-image JAVA11 \
   --metadata-file "metadata.json" \
   --jar "target/workload-generator-0.1.jar" \
   --env FLEX_TEMPLATE_JAVA_MAIN_CLASS="bigtable.WorkloadGenerator" \
   --gcs-log-dir="$LOGS_PATH"
   ```

   Note: Make sure your account or service account has cloudbuild and storage permissions.

## Building and running

If you would like to modify this and run it yourself you can use these commands:

1. Create a Bigtable instance and table

1. Set up the environment variables

   ```
   GOOGLE_CLOUD_PROJECT=your-project-id
   INSTANCE_ID=your-instance-id
   REGION=us-central1
   TABLE_ID=your-table-id 
   WORKLOAD_QPS=100 # Optional
   WORKLOAD_DURATION=15 # Optional, default to 10
   ```

1. Run the command

   ```
   mvn compile exec:java -Dexec.mainClass=WorkloadGenerator \
   "-Dexec.args=--bigtableInstanceId=$INSTANCE_ID --bigtableTableId=$TABLE_ID \
   --runner=dataflow --project=$GOOGLE_CLOUD_PROJECT \
   --region=$REGION" \
   --workloadQPS=$WORKLOAD_QPS 
   --workloadDurationMinutes=$WORKLOAD_DURATION 
   ```
