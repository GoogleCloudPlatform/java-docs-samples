# Bigtable Change Streams

This folder has samples showing how to use Cloud Bigtable's Change Stream
feature.

## Hello World

### Running

1. Create a Bigtable instance or use an existing one

1. Set up the environment variables

    ```sh
    GOOGLE_CLOUD_PROJECT=your-project-id
    BIGTABLE_PROJECT=your-project-id
    INSTANCE_ID=your-instance-id
    TABLE_ID=your-table-id
    REGION=us-central1
    ```

1. Create a table with a change streams enabled

   ```sh
   gcloud alpha bigtable instances tables create $TABLE_ID \
    --column-families=cf1 --change-stream-retention-period=7d \
   --instance=$BIGTABLE_TESTING_INSTANCE --project=$GOOGLE_CLOUD_PROJECT 
   ```

1. Run command to start the pipeline

    ```sh
    mvn compile exec:java -Dexec.mainClass=ChangeStreamsHelloWorld \
    "-Dexec.args=--project=$GOOGLE_CLOUD_PROJECT --bigtableProjectId=$BIGTABLE_PROJECT \
    --bigtableInstanceId=$INSTANCE_ID --bigtableTableId=$TABLE_ID \
    --runner=dataflow --region=$REGION --experiments=use_runner_v2"
    ```

1. Make changes to your data via the clients or with the `cbt` CLI, and view the
   output of the stream in the Dataflow worker logs.

### Testing

1. Set environment variables

   ```sh
   GOOGLE_CLOUD_PROJECT="project-id"
   BIGTABLE_TESTING_INSTANCE="instance-id"
   ```

1. Run the command to create a test table with change streams enabled

   ```sh
   gcloud alpha bigtable instances tables create change-stream-hello-world-test \
    --column-families=cf1,cf2 --change-stream-retention-period=7d \
   --instance=$BIGTABLE_TESTING_INSTANCE --project=$GOOGLE_CLOUD_PROJECT
   ```

1. Run the test

   ```sh
   mvn clean test
   ```

## Running locally

To run this program on your local machine, you can use the direct Beam runner by
setting `--runner=direct`. If you're running it locally, you don't need the
`--project` or `--region` parameters.
