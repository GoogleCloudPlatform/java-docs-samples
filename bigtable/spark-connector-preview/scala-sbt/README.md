# Bigtable Spark Example Using Scala and sbt

This example uses Scala and sbt for package management to write data
to a Bigtable table and read it back.

## Compiling the project

To compile the code, you can run
the following command (after installing sbt) from inside the current
directory:

```
sbt clean assembly
```

The target JAR will be located under
`target/scala-2.12/bigtable-spark-example-assembly-0.1.jar`.

## Running the example using Dataproc

To submit this PySpark job to Dataproc, you will need a Bigtable project and
instance ID, as well as a Bigtable table name, which will be the three required
arguments. By default, a new table is created by the application, but you can
provide an optional fourth arguemnt `false` for `createNewTable` (assuming
that you have already created a table with the column family `example_family`).

To run the job using dataproc, you can run the following command:

```
gcloud dataproc jobs submit spark \
--cluster=$BIGTABLE_SPARK_DATAPROC_CLUSTER \
--region=$BIGTABLE_SPARK_DATAPROC_REGION \
--class=bigtable.spark.example.WordCount \
--jars=target/scala-2.12/bigtable-spark-example-assembly-0.1.jar  \
--  \
$BIGTABLE_SPARK_PROJECT_ID \
$BIGTABLE_SPARK_INSTANCE_ID \
$BIGTABLE_SPARK_TABLE_NAME
```

## Expected output

The following text should be shown in the output of the Spark job.

```
Reading the DataFrame from Bigtable:
+-----+-----+
|count| word|
+-----+-----+
|    0|word0|
|    1|word1|
|    2|word2|
|    3|word3|
|    4|word4|
|    5|word5|
|    6|word6|
|    7|word7|
|    8|word8|
|    9|word9|
+-----+-----+
```


To verify that the data has been written to Bigtable, you can run the following
command (requires [cbt CLI](https://cloud.google.com/bigtable/docs/cbt-overview)):

```
cbt -project=$BIGTABLE_SPARK_PROJECT_ID -instance=$BIGTABLE_SPARK_INSTANCE_ID \
read $BIGTABLE_SPARK_TABLE_NAME
```

With this expected output:
```
----------------------------------------
word0
  example_family:Count                     @ 2023/07/11-18:31:51.349000
    "\x00\x00\x00\x00"

----------------------------------------
word1
  example_family:Count                     @ 2023/07/11-18:31:51.385000
    "\x00\x00\x00\x01"

----------------------------------------
.
.
.
```
