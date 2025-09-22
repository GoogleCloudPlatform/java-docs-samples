# Dataflow Flex templates - Kafka to BigQuery

[![Open in Cloud Shell](http://gstatic.com/cloudssh/images/open-btn.svg)](https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=dataflow/flex-templates/kafka_to_bigquery/README.md)

Samples showing how to create and run an
[Apache Beam](https://beam.apache.org/) template with a custom Docker image on
[Google Cloud Dataflow](https://cloud.google.com/dataflow/docs/).

## Before you begin

If you are not familiar with Dataflow Flex templates, please see the
[Streaming Beam SQL](../streaming-beam-sql/) sample first.

Follow the
[Getting started with Google Cloud Dataflow](../README.md)
page, and make sure you have a Google Cloud project with billing enabled
and a *service account JSON key* set up in your `GOOGLE_APPLICATION_CREDENTIALS`
environment variable.
Additionally, for this sample you need the following:

1. [Enable the APIs](https://console.cloud.google.com/flows/enableapi?apiid=appengine.googleapis.com,cloudscheduler.googleapis.com,cloudbuild.googleapis.com):
    App Engine, Cloud Scheduler, Cloud Build.

1. Create a [Cloud Storage bucket](https://cloud.google.com/storage/docs/creating-buckets).

    ```sh
    export BUCKET="your-gcs-bucket"
    gsutil mb gs://$BUCKET
    ```

1. Create a [BigQuery dataset](https://cloud.google.com/bigquery/docs/datasets).

    ```sh
    export PROJECT="$(gcloud config get-value project)"
    export DATASET="beam_samples"
    export TABLE="kafka_to_bigquery"

    bq mk --dataset "$PROJECT:$DATASET"
    ```

1. Select the compute region and zone to use.

    ```sh
    # Select your default compute/region, or default to "us-central1".
    export REGION=${"$(gcloud config get-value compute/region)":-"us-central1"}

    # Select your default compute/zone, or default to "$REGION-a".
    # Note that the zone *must* be in $REGION.
    export ZONE=${"$(gcloud config get-value compute/zone)":-"$REGION-a"}
    ```

1. Clone the `java-docs-samples` repository.

    ```sh
    git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
    ```

1. Navigate to the sample code directory.

    ```sh
    cd java-docs-samples/dataflow/flex-templates/kafka_to_bigquery
    ```

## Kafka to BigQuery sample

This sample shows how to deploy an Apache Beam streaming pipeline that reads
[JSON encoded](https://www.w3schools.com/whatis/whatis_json.asp) messages from
[Apache Kafka](https://kafka.apache.org/), decodes them, and writes them into a
[BigQuery](https://cloud.google.com/bigquery) table.

For this, we need two parts running:

1. A Kafka server container accessible through an external IP address.
   This services publishes messages to a topic.

    * [kafka/Dockerfile](kafka/Dockerfile)
    * [kafka/start-kafka.sh](kafka/start-kafka.sh)
    * [kafka/create-topic.sh](kafka/create-topic.sh)

2. An Apache Beam streaming pipeline running in Dataflow Flex Templates.
   This subscribes to a Kafka topic, consumes the messages that are published
   to that topic, processes them, and writes them into a BigQuery table.

    * [Dockerfile](Dockerfile)
    * [KafkaToBigQuery.java](src/main/java/org/apache/beam/samples/KafkaToBigQuery.java)
    * [pom.xml](pom.xml)
    * [metadata.json](metadata.json)

### Starting the Kafka server

> <details><summary>
> <i>(Optional)</i> Run the Kafka server locally for development.
> <i>(Click to expand)</i>
> </summary>
>
> Note that you **must** have
> [Docker installed in your machine](https://docs.docker.com/install/)
> to run the container locally.
> You do **not need** Docker installed to run in Cloud, skip this section if
> you want to go straight to building and deploying in Cloud.
>
> ```sh
> # Create a network where containers can communicate.
> docker network create kafka-net
>
> # Build the image.
> docker image build -t kafka kafka/
>
> # Run a detached container (in the background) using the network we created.
> docker run -d --rm \
>   --name "kafka" \
>   --net "kafka-net" \
>   -p 2181:2181 -p 9092:9092 \
>   kafka
> ```
>
> Once you are done, you can stop and delete the resources.
>
> ```sh
> # Stop the container.
> docker kill kafka
>
> # Delete the Docker network.
> docker network rm kafka-net
> ```
>
> For more information about creating a Docker application, see
> [Containerizing an application](https://docs.docker.com/get-started/part2/).
>
> </details>

The Kafka server must be accessible to *external* applications.
For this we need an
[external static IP address](https://cloud.google.com/compute/docs/ip-addresses/reserve-static-external-ip-address)
for the Kafka server to live. Not an internal IP address.

> ℹ️ If you already have a Kafka server running you can skip this section.
> Just make sure to store its IP address into an environment variable.
>
> ```sh
> export KAFKA_ADDRESS="123.456.789"
> ```

```sh
# Create a new static IP address for the Kafka server to use.
gcloud compute addresses create --region "$REGION" kafka-address

# Get the static address into a variable.
export KAFKA_ADDRESS=$(gcloud compute addresses describe --region="$REGION" --format='value(address)' kafka-address)
```

> ℹ️ Do not use `--global` to create the static IP address since the
> Kafka server must reside in a specific region.

We also need to
[create a firewall rule](https://cloud.google.com/compute/docs/containers/configuring-options-to-run-containers#publishing_container_ports)
to allow incoming messages to the server.

Kafka uses port `9092` and Zookeeper uses port `2181` by default, unless
configured differently.

```sh
# Create a firewall rule to open the port used by Zookeeper and Kafka.
# Allow connections to ports 2181, 9092 in VMs with the "kafka-server" tag.
gcloud compute firewall-rules create allow-kafka \
  --target-tags "kafka-server" \
  --allow tcp:2181,tcp:9092
```

Now we can start a new
[Compute Engine](https://cloud.google.com/compute/)
VM (Virtual Machine) instance for the Kafka server
[using the Docker image](https://cloud.google.com/compute/docs/instances/create-start-instance#from-container-image)
we created in Container Registry.

For this sample, we don't need a high performance VM, so we are using an
[e2-small](https://cloud.google.com/compute/docs/machine-types#e2_shared-core_machine_types)
machine with shared CPU cores for a more cost-effective option.

To learn more about pricing, see the
[VM instances pricing](https://cloud.google.com/compute/vm-instance-pricing) page.

```sh
export KAFKA_IMAGE="gcr.io/$PROJECT/samples/dataflow/kafka:latest"

# Note: If the project name has `:` in it that signifies a project within an
# organization (e.g. `example.com:project-id`), replace those with `/` so that
# the Kafka image can be found appropriately.

# Build the Kafka server image into Container Registry.
gcloud builds submit --tag $KAFKA_IMAGE kafka/

# If a different topic, address, kafka port, or zookeeper port is desired,
# update the following environment variables before starting the server.
# Otherwise, the default values will be used in the Dockerfile:
export KAFKA_TOPIC=<topic-name>
export KAFKA_ADDRESS=<kafka-address>
export KAFKA_PORT=<kafka-port>
export ZOOKEEPER_PORT=<zookeeper-port>

# Create and start a new instance.
# The --address flag binds the VM's address to the static address we created.
# The --container-env KAFKA_ADDRESS is an environment variable passed to the
# container to configure Kafka to use the static address of the VM.
# The --tags "kafka-server" is used by the firewakll rule.
gcloud compute instances create-with-container kafka-vm \
  --zone "$ZONE" \
  --machine-type "e2-small" \
  --address "$KAFKA_ADDRESS" \
  --container-image "$KAFKA_IMAGE" \
  --container-env "KAFKA_ADDRESS=$KAFKA_ADDRESS" \
  --tags "kafka-server"
```

Note: The Kafka server should be running at this point, but in its current state
no messages are being sent to a topic, which will cause the KafkaToBigQuery
template to fail.


### Sending messages to Kafka server

SSH into the `kafka-vm` that was created earlier and issue
the below commands that are required based on your timing. Messages sent before
the template is started will be present when the template is started. If the 
desire is to send messages after the template has started, then the messages
will be processed as they are sent.

Pre-Requisite SSH into the Kafka VM

```sh
$ gcloud compute ssh kafka-vm --zone "$ZONE"
```

1. Create a Topic

```sh
docker run --rm --network host bitnami/kafka:3.4.0 \
/opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 \
--create --topic <topic-name> --partitions 1 --replication-factor 1
```

2. Send Messages to the Topic

Run the console producer to send messages. After running the command, type a
message and press Enter. You can send multiple messages. Press Ctrl+C to stop
the producer.

Note: You can run this step either before starting the Dataflow template
(messages will be ready) or while it's running (messages will be processed as
they arrive).

```sh
docker run -i --rm --network host bitnami/kafka:3.4.0 \
/opt/bitnami/kafka/bin/kafka-console-producer.sh \
--bootstrap-server localhost:9092 --topic <topic-name>
```

3. (Optional) Verify the Messages

You can check that your messages were sent correctly by starting a consumer.
This will print all messages from the beginning of the topic. Press Ctrl+C to
exit.

```sh
docker run -it --rm --network host bitnami/kafka:3.4.0 \
/opt/bitnami/kafka/bin/kafka-console-consumer.sh \
--bootstrap-server localhost:9092 --topic <topic-name> --from-beginning
```

4. (Optional) Delete a Topic

```sh
docker run --rm --network host bitnami/kafka:3.4.0 \
/opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 \
--delete --topic <topic-name>
```


### Creating and running a Flex Template

> <details><summary>
> <i>(Optional)</i> Run the Apache Beam pipeline locally for development.
> <i>(Click to expand)</i>
> </summary>
>
> ```sh
> # If you omit the --bootstrapServer argument, it connects to localhost.
> # If you are running the Kafka server locally, you can omit --bootstrapServer.
> mvn compile exec:java \
>   -Dexec.mainClass=org.apache.beam.samples.KafkaToBigQuery \
>   -Dexec.args="\
>     --project=$PROJECT \
>     --outputTable=$PROJECT:$DATASET.$TABLE \
>     --bootstrapServer=$KAFKA_ADDRESS:9092"
> ```
>
> </details>

First, let's build the container image.

```sh
# Build and package the application as an uber-jar file.
mvn clean package
```

Now we can create the template file.

```sh
export TEMPLATE_IMAGE="gcr.io/$PROJECT/samples/dataflow/kafka-to-bigquery-sql:latest"
export TEMPLATE_PATH="gs://$BUCKET/samples/dataflow/templates/kafka-to-bigquery.json"

# Build the Flex Template.
gcloud dataflow flex-template build $TEMPLATE_PATH \
    --image-gcr-path "$TEMPLATE_IMAGE" \
    --sdk-language "JAVA" \
    --flex-template-base-image JAVA11 \
    --metadata-file "metadata.json" \
    --jar "target/kafka-to-bigquery-1.0.jar" \
    --env FLEX_TEMPLATE_JAVA_MAIN_CLASS="org.apache.beam.samples.KafkaToBigQuery"
```

Finally, to run a Dataflow job using the template.

```sh
export REGION="us-central1"

# Run the Flex Template.
gcloud dataflow flex-template run "kafka-to-bigquery-`date +%Y%m%d-%H%M%S`" \
    --template-file-gcs-location "$TEMPLATE_PATH" \
    --parameters inputTopic="messages" \
    --parameters outputTable="$PROJECT:$DATASET.$TABLE" \
    --parameters bootstrapServer="$KAFKA_ADDRESS:9092" \
    --region "$REGION"
```

Note: If one of the parameters is a deeply nested json or dictionary, use the
gcloud `--flags-file` parameter to pass in a yaml file a list of all the
parameters including the nested dictionary. Passing in the dictionary straight
from the command line will give a gcloud error. The parameters file can look
like this:

```yaml
--parameters:
  inputTopic: messages
  outputTable: $PROJECT:$DATASET.$TABLE
  bootstrapServer: $KAFKA_ADDRESS:9092
  schema:
    '{type: object, properties: {processing_time: {type: TIMESTAMP}, url: {type: STRING}, rating: {type: STRING}}}'
```

Run the following query to check the results in BigQuery.

```sh
bq query --use_legacy_sql=false 'SELECT * FROM `'"$PROJECT.$DATASET.$TABLE"'`'
```

### Cleaning up

After you've finished this tutorial, you can clean up the resources you created
on Google Cloud so you won't be billed for them in the future.
The following sections describe how to delete or turn off these resources.

#### Clean up the Flex template resources

1. Stop the Dataflow pipeline.

    ```sh
    gcloud dataflow jobs list \
        --filter 'NAME:kafka-to-bigquery AND STATE=Running' \
        --format 'value(JOB_ID)' \
      | xargs gcloud dataflow jobs cancel
    ```

1. Delete the template spec file from Cloud Storage.

    ```sh
    gsutil rm $TEMPLATE_PATH
    ```

1. Delete the Flex Template container images from Container Registry.

    ```sh
    gcloud container images delete $TEMPLATE_IMAGE --force-delete-tags
    ```

#### Clean up the Kafka server

1. Delete the Kafka server VM instance.

    ```sh
    gcloud compute instances delete kafka-vm
    ```

1. Delete the firewall rule, this does not incur any charges.

    ```sh
    gcloud compute firewall-rules delete allow-kafka
    ```

1. Delete the static address.

    ```sh
    gcloud compute addresses delete --region "$REGION" kafka-address
    ```

1. Delete the Kafka container image from Container Registry.

    ```sh
    gcloud container images delete $KAFKA_IMAGE --force-delete-tags
    ```

#### Clean up Google Cloud project resources

1. Delete the BigQuery table.

    ```sh
    bq rm -f -t $PROJECT:$DATASET.$TABLE
    ```

1. Delete the BigQuery dataset, this alone does not incur any charges.

    > ⚠️ The following command also deletes all tables in the dataset.
    > The tables and data cannot be recovered.
    >
    > ```sh
    > bq rm -r -f -d $PROJECT:$DATASET
    > ```

1. Delete the Cloud Storage bucket, this alone does not incur any charges.

    > ⚠️ The following command also deletes all objects in the bucket.
    > These objects cannot be recovered.
    >
    > ```sh
    > gsutil rm -r gs://$BUCKET
    > ```
