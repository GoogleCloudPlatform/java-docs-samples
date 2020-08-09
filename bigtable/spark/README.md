# Spark Applications for Cloud Bigtable

## Overview

The project shows how to load or save data to [Cloud Bigtable](https://cloud.google.com/bigtable) using [Apache Spark](https://spark.apache.org/) and [Apache HBase™ Spark Connector](https://github.com/apache/hbase-connectors/tree/master/spark) . 

The project uses [sbt](https://www.scala-sbt.org/) as the build tool.

## Prerequisites

1. A [Google Cloud project](https://console.cloud.google.com/) with billing enabled.
Please be aware of [Cloud Bigtable](https://cloud.google.com/bigtable/pricing) pricing.

1. [Google Cloud SDK](https://cloud.google.com/sdk/) installed.

1. [sbt](https://www.scala-sbt.org/) installed.

1. [Apache Spark](https://spark.apache.org/) installed. Download Spark built for Scala 2.11.

1. A basic familiarity with [Apache Spark](https://spark.apache.org/) and [Scala](https://www.scala-lang.org/).

## Assemble the Example

The Spark application can be assembled into an uber/fat jar with all of its dependencies and configuration. To build use `sbt` as follows:

```
sbt clean assembly
```

The above command should build `target/scala-2.11/bigtable-spark-samples-assembly-0.1.jar` file.

**NOTE**: Since the Cloud Bigtable configuration is included in the (fat) jar, any changes will require re-assembling it.

## Start Bigtable Emulator

```
gcloud beta emulators bigtable start
```

## Configure the Example

Create environment variables for the following commands:

```
BIGTABLE_TABLE=wordcount
NUMBER_OF_ROWS=5
```

## Run

```
$(gcloud beta emulators bigtable env-init)
```

```
$SPARK_HOME/bin/spark-submit \
  --packages org.apache.hbase.connectors.spark:hbase-spark:1.0.0 \
  target/scala-2.11/bigtable-spark-samples-assembly-0.1.jar \
  $BIGTABLE_TABLE $NUMBER_OF_ROWS
```

## Verify

There should be one table.

```
$ cbt \
  -project=your-google-cloud-project \
  -instance=your-bigtable-instance \
  ls
wordcount
```

There should be the number of rows that you requested on command line.

```
cbt \
  -project=your-google-cloud-project \
  -instance=your-bigtable-instance \
  read $BIGTABLE_TABLE
```

## Configure the Example to use Cloud Bigtable

Set the Google Cloud Project ID and Cloud Bigtable instance ID in [hbase-site.xml](src/main/resources/hbase-site.xml) configuration file.
