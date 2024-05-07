# Batch write flow control example

This is an example pipeline to demo how to use the batch write flow control
feature using CloudBigtableIO and BigtableIO.

## Running instructions

1. Create a Bigtable instance in the console or using gCloud.

1. Create a table with column family `cf`.

1. Set up the environment variables

```
GOOGLE_CLOUD_PROJECT=<your-project-id>
INSTANCE_ID=<your-instance-id>
TABLE_ID=<your-table-id>
REGION=<your-region>
NUM_ROWS=<number-of-rows>
NUM_COLS_PER_ROW=<number-of-columns-per-row>
NUM_BYTES_PER_COL=<number-of-bytes-per-col>
NUM_WORKERS=<number-of-workers>
MAX_NUM_WORKERS=<max-number-of-workers>
USE_CLOUD_BIGTABLE_IO=<true/false>

```

1. Run the command

```
mvn compile exec:java -Dexec.mainClass=bigtable.BatchWriteFlowControlExample \
"-Dexec.args=--runner=dataflow \
  --project=$GOOGLE_CLOUD_PROJECT \
  --bigtableInstanceId=$INSTANCE_ID \
  --bigtableTableId=$TABLE_ID \
  --bigtableRows=$NUM_ROWS \
  --bigtableColsPerRow=$NUM_COLS_PER_ROW \
  --bigtableBytesPerCol=$NUM_BYTES_PER_COL\
  --region=$REGION \
  --numWorkers=$NUM_WORKERS \
  --maxNumWorkers=$MAX_NUM_WORKERS \
  --useCloudBigtableIo=$USE_CLOUD_BIGTABLE_IO"
```
