# Spark Applications for Cloud Bigtable

## Overview

The project shows how to read data from or write data to [Cloud Bigtable](https://cloud.google.com/bigtable) using [Apache Spark](https://spark.apache.org/) and [Apache HBase™ Spark Connector](https://github.com/apache/hbase-connectors/tree/master/spark).

**Apache Spark** is the execution environment that can distribute and parallelize data processing (loading data from and writing data to various data sources).
Apache Spark provides DataSource API for external systems to plug into as data sources (also known as data providers).

**Apache HBase™ Spark Connector** implements the DataSource API for Apache HBase and allows executing relational queries on data stored in Cloud Bigtable.

**Google Cloud Bigtable** is a fully-managed cloud service for a NoSQL database of petabyte-scale and large analytical and operational workloads.
`bigtable-hbase-2.x-hadoop` provides a bridge from the HBase API to Cloud Bigtable that allows Spark queries to interact with Bigtable using the native Spark API.

**Google Cloud Dataproc** is a fully-managed cloud service for running [Apache Spark](https://spark.apache.org/) applications and [Apache Hadoop](https://hadoop.apache.org/) clusters.

## Tasks

FIXME Remove the section once all tasks done.

- [ ] Avoid specifying dependencies at runtime (remove `--packages` option for `spark-submit`)
- [ ] Make sure README.md is up-to-date before claiming the PR done

## Prerequisites

1. [Google Cloud project](https://console.cloud.google.com/)

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

Set the following environment variable to reference the assembly file.

```
BIGTABLE_SPARK_ASSEMBLY_JAR=target/scala-2.11/bigtable-spark-samples-assembly-0.1.jar
```

## Run Examples with Bigtable Emulator

Start the Bigtable Emulator.

```
gcloud beta emulators bigtable start
```

Set the following environment variables for the sample applications to use:

```
SPARK_HOME=your-spark-home
BIGTABLE_SPARK_PROJECT_ID=your-project-id
BIGTABLE_SPARK_INSTANCE_ID=your-bigtable-instance

BIGTABLE_SPARK_WORDCOUNT_TABLE=wordcount
BIGTABLE_SPARK_WORDCOUNT_FILE=src/test/resources/Romeo-and-Juliet-prologue.txt

BIGTABLE_SPARK_COPYTABLE_TABLE=copytable
```

Initialize the environment to point to the Bigtable Emulator.

```
$(gcloud beta emulators bigtable env-init)
```

### Create Tables

Create the tables using `cbt createtable` command.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  createtable $BIGTABLE_SPARK_WORDCOUNT_TABLE \
  "families=cf"
```

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  createtable $BIGTABLE_SPARK_COPYTABLE_TABLE \
  "families=cf"
```

List tables using `cbt ls` command.

```
$ cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  ls
copytable
wordcount
```

### Wordcount

The following `spark-submit` uses [example.Wordcount](src/main/scala/example/Wordcount.scala).

```
$SPARK_HOME/bin/spark-submit \
  --packages org.apache.hbase.connectors.spark:hbase-spark:1.0.0 \
  --class example.Wordcount \
  $BIGTABLE_SPARK_ASSEMBLY_JAR \
  $BIGTABLE_SPARK_PROJECT_ID $BIGTABLE_SPARK_INSTANCE_ID \
  $BIGTABLE_SPARK_WORDCOUNT_TABLE $BIGTABLE_SPARK_WORDCOUNT_FILE
```

### Verify

Use `cbt count` to count the number of rows in the `BIGTABLE_SPARK_WORDCOUNT_TABLE` table.

```
$ cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  count $BIGTABLE_SPARK_WORDCOUNT_TABLE
88
```

**TIP** For details about using the `cbt` tool, including a list of available commands, see the [cbt Reference](https://cloud.google.com/bigtable/docs/cbt-reference).

### CopyTable

The following `spark-submit` uses [example.CopyTable](src/main/scala/example/CopyTable.scala).

```
$SPARK_HOME/bin/spark-submit \
  --packages org.apache.hbase.connectors.spark:hbase-spark:1.0.0 \
  --class example.CopyTable \
  $BIGTABLE_SPARK_ASSEMBLY_JAR \
  $BIGTABLE_SPARK_PROJECT_ID $BIGTABLE_SPARK_INSTANCE_ID \
  $BIGTABLE_SPARK_WORDCOUNT_TABLE $BIGTABLE_SPARK_COPYTABLE_TABLE
```

### Verify

Use `cbt count` to count the number of rows in the `BIGTABLE_SPARK_COPYTABLE_TABLE` table.

```
$ cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  count $BIGTABLE_SPARK_COPYTABLE_TABLE
88
```

## Run Wordcount with Cloud Bigtable

### Create Cloud Bigtable Instance

Create a Cloud Bigtable instance using the Google Cloud Console (as described in the [Create a Cloud Bigtable instance](https://cloud.google.com/bigtable/docs/quickstart-cbt#create-instance)) or `gcloud beta bigtable instances`.

```
BIGTABLE_SPARK_CLUSTER_ID=your-cluster-id
BIGTABLE_SPARK_CLUSTER_ZONE=your-zone-id
BIGTABLE_SPARK_INSTANCE_DISPLAY_NAME=your-display-name

gcloud beta bigtable instances \
  create $BIGTABLE_SPARK_INSTANCE_ID \
  --cluster=$BIGTABLE_SPARK_CLUSTER_ID \
  --cluster-zone=$BIGTABLE_SPARK_CLUSTER_ZONE \
  --display-name=$BIGTABLE_SPARK_INSTANCE_DISPLAY_NAME \
  --instance-type=DEVELOPMENT
```

Check out the available Cloud Bigtable instances and make sure yours is listed.

```
gcloud beta bigtable instances list
```

### Create Table

Create a table using `cbt createtable` command.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  createtable $BIGTABLE_SPARK_WORDCOUNT_TABLE \
  "families=cf"
```

List tables using `cbt ls` command.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  ls
```

### Submit Wordcount

```
$SPARK_HOME/bin/spark-submit \
  --packages org.apache.hbase.connectors.spark:hbase-spark:1.0.0 \
  --class example.Wordcount \
  $BIGTABLE_SPARK_ASSEMBLY_JAR \
  $BIGTABLE_SPARK_PROJECT_ID $BIGTABLE_SPARK_INSTANCE_ID \
  $BIGTABLE_SPARK_WORDCOUNT_TABLE $BIGTABLE_SPARK_WORDCOUNT_FILE
```

### Verify

Use `cbt count` to count the number of rows in the `BIGTABLE_SPARK_WORDCOUNT_TABLE` table. There should be 
88 rows.

```
$ cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  count $BIGTABLE_SPARK_WORDCOUNT_TABLE
88
```

### Delete Cloud Bigtable Instance

Use `cbt listinstances` to list existing Bigtable instances.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  listinstances
```

There should be at least `BIGTABLE_SPARK_INSTANCE_ID` instance. Delete it using `cbt deleteinstance`.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  deleteinstance $BIGTABLE_SPARK_INSTANCE_ID
```

## Submit DataFrameDemo to Cloud Dataproc

This section describes the steps to submit [DataFrameDemo](src/main/scala/example/CopyTable.scala) application to [Google Cloud Dataproc](https://cloud.google.com/dataproc/).

**TIP** Read [Quickstart using the gcloud command-line tool](https://cloud.google.com/dataproc/docs/quickstarts/quickstart-gcloud) that shows how to use the Google Cloud SDK `gcloud` command-line tool to create a Google Cloud Dataproc cluster and more.

### Configure Environment

```
BIGTABLE_SPARK_PROJECT_ID=your-project-id
BIGTABLE_SPARK_INSTANCE_ID=your-bigtable-instance-id
BIGTABLE_SPARK_DATAPROC_CLUSTER=spark-cluster
BIGTABLE_SPARK_REGION=europe-west4
BIGTABLE_SPARK_JAR=target/scala-2.11/bigtable-spark-samples-assembly-0.1.jar
BIGTABLE_SPARK_CLASS=example.DataFrameDemo
BIGTABLE_SPARK_TABLE=DataFrameDemo
```

**NOTE** `BIGTABLE_SPARK_REGION` should point to your region. Read [Available regions and zones](https://cloud.google.com/compute/docs/regions-zones#available) in the official documentation.

### Authenticate

Authenticate to a Google Cloud Platform API using service or user accounts.
Learn about [authenticating to a GCP API](https://cloud.google.com/docs/authentication/) in the Google Cloud documentation.

**NOTE**: In most situations, we recommend [authenticating as a service account](https://cloud.google.com/docs/authentication/production) to a Google Cloud Platform (GCP) API.

```
GOOGLE_APPLICATION_CREDENTIALS=/your/service/account.json
```

### Create Google Cloud Dataproc Cluster

```
BIGTABLE_SPARK_DATAPROC_CLUSTER=spark-cluster
BIGTABLE_SPARK_REGION=your-region
BIGTABLE_SPARK_PROJECT_ID=your-project-id

gcloud dataproc clusters create $BIGTABLE_SPARK_DATAPROC_CLUSTER \
  --region=$BIGTABLE_SPARK_REGION \
  --project=$BIGTABLE_SPARK_PROJECT_ID
```

### Configure Cloud Bigtable

```
BIGTABLE_SPARK_INSTANCE_ID=your-instance-id
BIGTABLE_SPARK_CLUSTER_ID=your-cluster-id
BIGTABLE_SPARK_CLUSTER_ZONE=your-cluster-zone

gcloud bigtable instances create $BIGTABLE_SPARK_INSTANCE_ID \
    --cluster=$BIGTABLE_SPARK_CLUSTER_ID \
    --cluster-zone=$BIGTABLE_SPARK_CLUSTER_ZONE \
    --display-name=$BIGTABLE_SPARK_INSTANCE_ID
```

```
BIGTABLE_SPARK_PROJECT_ID=bigtable-spark-connector
BIGTABLE_SPARK_DataFrameDemo_TABLE=DataFrameDemo
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  createtable $BIGTABLE_SPARK_DataFrameDemo_TABLE
```

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  ls
```

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  createfamily $BIGTABLE_SPARK_DataFrameDemo_TABLE rowkey

cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  createfamily $BIGTABLE_SPARK_DataFrameDemo_TABLE cf1

cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  createfamily $BIGTABLE_SPARK_DataFrameDemo_TABLE cf2

cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  createfamily $BIGTABLE_SPARK_DataFrameDemo_TABLE cf3
```

### Submit Wordcount Job

Submit the Wordcount job to a Cloud Dataproc instance.

```
gcloud dataproc jobs submit spark \
  --cluster=$BIGTABLE_SPARK_CLUSTER \
  --region=$BIGTABLE_SPARK_REGION \
  --class=$BIGTABLE_SPARK_CLASS \
  --jars=$BIGTABLE_SPARK_JAR \
  --properties=spark.jars.packages='org.apache.hbase.connectors.spark:hbase-spark:1.0.0' \
  -- \
  $BIGTABLE_SPARK_PROJECT_ID $BIGTABLE_SPARK_INSTANCE_ID $BIGTABLE_SPARK_DataFrameDemo_TABLE
```

### Verify

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
  read $BIGTABLE_SPARK_DataFrameDemo_TABLE
```

### Clean Up

Delete the Bigtable instance.

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  deleteinstance $BIGTABLE_SPARK_INSTANCE_ID
```

```
cbt \
  -project=$BIGTABLE_SPARK_PROJECT_ID \
  listinstances
```

Delete the Dataproc cluster.

```
gcloud dataproc clusters delete $BIGTABLE_SPARK_DATAPROC_CLUSTER \
  --region=$BIGTABLE_SPARK_REGION \
  --project=$BIGTABLE_SPARK_PROJECT_ID \
  --quiet
```
