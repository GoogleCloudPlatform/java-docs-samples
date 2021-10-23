# Dataflow flex templates

[![Open in Cloud Shell](http://gstatic.com/cloudssh/images/open-btn.svg)](https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=dataflow/flex-templates/README.md)

Samples showing how to create and run an
[Apache Beam](https://beam.apache.org/) template with a custom Docker image on
[Google Cloud Dataflow](https://cloud.google.com/dataflow/docs/).

## Streaming Beam SQL sample (basic)

[streaming_beam_sql](streaming_beam_sql)

This sample shows how to deploy an Apache Beam streaming pipeline that reads
[JSON encoded](https://www.w3schools.com/whatis/whatis_json.asp) messages from
[Pub/Sub](https://cloud.google.com/pubsub), uses
[Beam SQL](https://beam.apache.org/documentation/dsls/sql/overview/)
to transform the message data, and writes the results to a
[BigQuery](https://cloud.google.com/bigquery) table.

## Kafka to BigQuery sample (advanced)

[kafka_to_bigquery](kafka_to_bigquery)

This sample shows how to deploy an Apache Beam streaming pipeline that reads
[JSON encoded](https://www.w3schools.com/whatis/whatis_json.asp) messages from
[Apache Kafka](https://kafka.apache.org/), decodes them, and writes them into a
[BigQuery](https://cloud.google.com/bigquery) table.

For this, we need two parts running:

1. A Kafka service container accessible through an external IP address.
   This services publishes messages to a topic.
2. An Apache Beam streaming pipeline running in Dataflow Flex Templates.
   This subscribes to a Kafka topic, consumes the messages that are published
   to that topic, processes them, and writes them into a BigQuery table.
