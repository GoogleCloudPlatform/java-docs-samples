# Bulk data generator

This is a tool to fill up a Bigtable instance with a ton of data for demonstration purposes.
It will set a table to a specific size, so if you set it to a smaller size than
your instance, it will delete tables, so proceed with caution.

## Running instructions

1. Create a Bigtable instance

2. Set up the environment variables

```
GOOGLE_CLOUD_PROJECT=your-project-id
INSTANCE_ID=your-instance-id
BIGTABLE_SIZE=1.5 // Size in terabytes in .5 increments
REGION=us-central1
```

3. Run the command

```
mvn compile exec:java -Dexec.mainClass=bigtable.BulkWrite \
"-Dexec.args=--bigtableInstanceId=$INSTANCE_ID \
--runner=dataflow --project=$GOOGLE_CLOUD_PROJECT \
--bigtableSize=$BIGTABLE_SIZE --region=$REGION"
```
