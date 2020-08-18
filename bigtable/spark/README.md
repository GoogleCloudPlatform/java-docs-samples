# Spark Applications for Cloud Bigtable

## Overview

The project shows how to load or save data to [Cloud Bigtable](https://cloud.google.com/bigtable) using [Apache Spark](https://spark.apache.org/) and [Apache HBase™ Spark Connector](https://github.com/apache/hbase-connectors/tree/master/spark).

- Apache Spark is the execution environment that can distribute and parallelize data processing (loading data from and writing data to various data sources).
Apache Spark provides DataSource API for external systems to plug into as data sources (also known as data providers).
- The Apache HBase™ Spark Connector implements the DataSource API for Apache HBase.
- `bigtable-hbase-2.x-hadoop` provides a bridge from the HBase API to Cloud Bigtable that allows Spark queries to interact with Bigtable using the native Spark API.

## Prerequisites

1. A [Google Cloud project](https://console.cloud.google.com/) with billing enabled.
Please be aware of [Cloud Bigtable](https://cloud.google.com/bigtable/pricing) pricing.

1. [Google Cloud SDK](https://cloud.google.com/sdk/) installed.

1. [sbt](https://www.scala-sbt.org/) installed.

1. [Apache Spark](https://spark.apache.org/) installed. Download Spark built for Scala 2.11.

1. A basic familiarity with [Apache Spark](https://spark.apache.org/) and [Scala](https://www.scala-lang.org/).

## Assemble the Examples

Execute the following `sbt` command to assemble the sample applications as a single uber/fat jar (with all of its dependencies and configuration).

```
sbt clean assembly
```

The above command should build `target/scala-2.11/bigtable-spark-samples-assembly-0.1.jar` file.

## Test Examples using Bigtable Emulator

Start the Bigtable Emulator.

```
gcloud beta emulators bigtable start
```

Set the following environment variables for the sample applications to use:

```
GOOGLE_CLOUD_PROJECT=your-project-id
BIGTABLE_INSTANCE=your-bigtable-instance
```

Initialize the environment to point to the Bigtable Emulator.

```
$(gcloud beta emulators bigtable env-init)
```

Use one of the Spark sample applications as the `--class` parameter.

### Wordcount

The following `spark-submit` uses [example.Wordcount](src/main/scala/example/Wordcount.scala).

```
$SPARK_HOME/bin/spark-submit \
  --packages org.apache.hbase.connectors.spark:hbase-spark:1.0.0 \
  --class example.Wordcount \
  target/scala-2.11/bigtable-spark-samples-assembly-0.1.jar \
  $GOOGLE_CLOUD_PROJECT $BIGTABLE_INSTANCE \
  wordcount-rdd README.md
```

### DataFrameDemo

The following `spark-submit` uses [example.DataFrameDemo](src/main/scala/example/DataFrameDemo.scala).

```
$SPARK_HOME/bin/spark-submit \
  --packages org.apache.hbase.connectors.spark:hbase-spark:1.0.0 \
  --class example.DataFrameDemo \
  target/scala-2.11/bigtable-spark-samples-assembly-0.1.jar \
  $GOOGLE_CLOUD_PROJECT $BIGTABLE_INSTANCE \
  wordcount-dataframe 5
```

### Verify

There should be one table.

```
$ cbt \
  -project=$GOOGLE_CLOUD_PROJECT \
  -instance=$BIGTABLE_INSTANCE \
  ls
wordcount
```

There should be the number of rows that you requested on command line.

```
cbt \
  -project=$GOOGLE_CLOUD_PROJECT \
  -instance=$BIGTABLE_INSTANCE \
  read $BIGTABLE_TABLE
```

## Run Wordcount with Cloud Bigtable

```
APP_NAME=example.Wordcount
WORDCOUNT_BIGTABLE_TABLE=wordcount-rdd
WORDCOUNT_FILE=README.md
```

```
cbt \
  -project=$GOOGLE_CLOUD_PROJECT \
  -instance=$BIGTABLE_INSTANCE \
  createtable $WORDCOUNT_BIGTABLE_TABLE
```

```
cbt \
  -project=$GOOGLE_CLOUD_PROJECT \
  -instance=$BIGTABLE_INSTANCE \
  ls
```

```
cbt \
  -project=$GOOGLE_CLOUD_PROJECT \
  -instance=$BIGTABLE_INSTANCE \
  createfamily $WORDCOUNT_BIGTABLE_TABLE cf
```

```
$SPARK_HOME/bin/spark-submit \
  --packages org.apache.hbase.connectors.spark:hbase-spark:1.0.0 \
  --class APP_NAME \
  target/scala-2.11/bigtable-spark-samples-assembly-0.1.jar \
  $GOOGLE_CLOUD_PROJECT $BIGTABLE_INSTANCE \
  $WORDCOUNT_BIGTABLE_TABLE WORDCOUNT_FILE
```

```
cbt \
  -project=$GOOGLE_CLOUD_PROJECT \
  -instance=$BIGTABLE_INSTANCE \
  read $WORDCOUNT_BIGTABLE_TABLE
```

## Delete Cloud Bigtable Instance

```
cbt \
  -project=$GOOGLE_CLOUD_PROJECT \
  listinstances
```

```
cbt \
  -project=$GOOGLE_CLOUD_PROJECT \
  deleteinstance $BIGTABLE_INSTANCE
```
