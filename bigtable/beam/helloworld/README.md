# Bigtable Dataflow Hello World 

This folder has samples showing how to read and write data with Bigtable in a 
Dataflow pipeline.

## Running instructions

1. Create a Bigtable instance

1. Create a table with a column family stats_summary 

    ```cbt createtable mobile-time-series families="stats_summary"```

1. Set up the environment variables

    ```
    GOOGLE_CLOUD_PROJECT=your-project-id
    BIGTABLE_PROJECT=your-project-id
    INSTANCE_ID=your-instance-id
    TABLE_ID=your-table-id
    REGION=us-central1
    ```

1. Run the command to write the data

    ```
    mvn compile exec:java -Dexec.mainClass=HelloWorldWrite \
    "-Dexec.args=--project=$GOOGLE_CLOUD_PROJECT --bigtableProjectId=$BIGTABLE_PROJECT \
    --bigtableInstanceId=$INSTANCE_ID --bigtableTableId=$TABLE_ID \
    --runner=dataflow --region=$REGION"
    ```

1. Run the command to read the data

    ```
    mvn compile exec:java -Dexec.mainClass=HelloWorldRead \
    "-Dexec.args=--project=$GOOGLE_CLOUD_PROJECT --bigtableProjectId=$BIGTABLE_PROJECT \
    --bigtableInstanceId=$INSTANCE_ID --bigtableTableId=$TABLE_ID \
    --runner=dataflow --region=$REGION"
    ```
   

### Running locally

To run either of these programs on your local machine, you can use the direct dataflow runner
by setting `--runner=direct`. If you're running it locally, you don't need the 
`--project` or `--region` parameters either.