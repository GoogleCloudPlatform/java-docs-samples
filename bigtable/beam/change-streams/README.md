# Bigtable Change Streams

This folder has samples showing how to use Cloud Bigtable's Change Stream
feature.

## Hello World

This example writes a message to standard output when a change is made to
Bigtable.

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
   --instance=INSTANCE_ID --project=$GOOGLE_CLOUD_PROJECT 
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
### Clean up

1. Stop your Dataflow job to avoid incurring any costs.

   1. List the jobs to get the job id.

      ```sh
      gcloud dataflow jobs list --region=$REGION
      ```

   1. Cancel the job

        ```sh
        gcloud dataflow jobs cancel ${JOB_ID} --region=$REGION
        ```

1. Disable change stream on the table.

   ```sh
    gcloud alpha bigtable instances tables update $TABLE_ID --instance=$INSTANCE_ID \
    --clear-change-stream-retention-period
   ```

1. Delete the table.

    ```sh
    cbt -instance=$INSTANCE_ID -project=$GOOGLE_CLOUD_PROJECT deletetable $TABLE_ID
    ```

### Testing

1. Set environment variables

   ```sh
   GOOGLE_CLOUD_PROJECT="project-id"
   BIGTABLE_TESTING_INSTANCE="instance-id"
   ```

1. Run the command to create a test table with change streams enabled

   ```sh
   gcloud bigtable instances tables create change-stream-hello-world-test \
    --column-families=cf1,cf2 --change-stream-retention-period=7d \
   --instance=$BIGTABLE_TESTING_INSTANCE --project=$GOOGLE_CLOUD_PROJECT
   ```

1. Run the test

   ```sh
   mvn clean test -Dtest=ChangeStreamsHelloWorldTest
   ```

## Song rank

This example keeps track of songs listened to and gets the top 5 songs over a
period of time.
The top 5 songs are output to standard out and files which can be local or on
Google Cloud Storage.

1. Create a Bigtable instance or use an existing one

1. Set up the environment variables

    ```sh
    GOOGLE_CLOUD_PROJECT=your-project-id
    BIGTABLE_PROJECT=your-project-id
    INSTANCE_ID=your-instance-id
    TABLE_ID=song-rank
    REGION=us-central1
    OUTPUT_LOCATION=gs://your-bucket-id/  # Exclude the gs:// to save locally 
    ```

1. Create a table with a change streams enabled

   ```sh
    gcloud alpha bigtable instances tables create $TABLE_ID \
    --column-families=cf --change-stream-retention-period=7d \
    --instance=$INSTANCE_ID --project=$GOOGLE_CLOUD_PROJECT 

1. Run command to start the pipeline

    ```sh
    mvn compile exec:java -Dexec.mainClass=SongRank \
    "-Dexec.args=--project=$GOOGLE_CLOUD_PROJECT --bigtableProjectId=$BIGTABLE_PROJECT \
    --bigtableInstanceId=$INSTANCE_ID --bigtableTableId=$TABLE_ID --outputLocation=$OUTPUT_LOCATION \
    --runner=dataflow --region=$REGION --experiments=use_runner_v2"
    ```

1. Stream some data which contains song listens for various users

   ```sh
   cbt -instance=$INSTANCE_ID -project=$GOOGLE_CLOUD_PROJECT import \
   $TABLE_ID song-rank-data.csv  column-family=cf batch-size=1
   ```

1. Observe the output on GCS and see the most popular songs.

    ```sh
    gsutil cat ${OUTPUT_LOCATION}/song-charts/GlobalWindow-pane-0-00000-of-00001.txt 
    ```

   Example output:
    ```
    2023-07-06T19:53:38.232Z [KV{The Wheels on the Bus, 199}, KV{Twinkle, Twinkle, Little Star, 199}, KV{Ode to Joy , 192}, KV{Row, Row, Row Your Boat, 186}, KV{Take Me Out to the Ball Game, 182}]
    2023-07-06T19:53:49.536Z [KV{Old MacDonald Had a Farm, 20}, KV{Take Me Out to the Ball Game, 18}, KV{Für Elise, 17}, KV{Ode to Joy , 15}, KV{Mary Had a Little Lamb, 12}]
    2023-07-06T19:53:50.425Z [KV{Twinkle, Twinkle, Little Star, 20}, KV{The Wheels on the Bus, 17}, KV{Row, Row, Row Your Boat, 13}, KV{Happy Birthday to You, 12}, KV{Over the Rainbow, 9}]
    ```
### Clean up

1. Stop your Dataflow job to avoid incurring any costs.

    1. List the jobs to get the job id.

       ```sh
       gcloud dataflow jobs list --region=$REGION
       ```

    1. Cancel the job
       
         ```sh
         gcloud dataflow jobs cancel ${JOB_ID} --region=$REGION
         ```

1. Disable change stream on the table.
   
   ```sh
    gcloud alpha bigtable instances tables update song-rank --instance=$INSTANCE_ID \
    --clear-change-stream-retention-period
   ```

1. Delete the table `song-rank`.

    ```sh
    cbt -instance=$INSTANCE_ID -project=$GOOGLE_CLOUD_PROJECT deletetable song-rank
    ```


### Testing

1. Set environment variables

   ```sh
   GOOGLE_CLOUD_PROJECT="project-id"
   BIGTABLE_TESTING_INSTANCE="instance-id"
   ```

1. Run the command to create a test table with change streams enabled

   ```sh
   gcloud alpha bigtable instances tables create song-rank-test \
    --column-families=cf --change-stream-retention-period=7d \
   --instance=$BIGTABLE_TESTING_INSTANCE --project=$GOOGLE_CLOUD_PROJECT

1. Run the test (output location is local)

   ```sh
   mvn clean test -Dtest=SongRankTest
   ```

## Running locally

To run either program on your local machine, you can use the direct Beam runner
by
setting `--runner=DirectRunner` (also the default if not specified). If you're running it locally, you don't need the
`--project` or `--region` parameters.
