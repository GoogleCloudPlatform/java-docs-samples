# Dataflow flex templates

[![Open in Cloud Shell](http://gstatic.com/cloudssh/images/open-btn.svg)](https://console.cloud.google.com/cloudshell/editor)

Samples showing how to create and run an
[Apache Beam](https://beam.apache.org/) template with a custom Docker image on
[Google Cloud Dataflow](https://cloud.google.com/dataflow/docs/).

## Before you begin

Follow the
[Getting started with Google Cloud Dataflow](../README.md)
page, and make sure you have a Google Cloud project with billing enabled
and a *service account JSON key* set up in your `GOOGLE_APPLICATION_CREDENTIALS`
environment variable.
Additionally, for this sample you need the following:

1. Create a Cloud Storage bucket.

    ```sh
    export BUCKET="your-gcs-bucket"
    gsutil mb gs://$BUCKET
    ```

1. Create a BigQuery dataset.

    ```sh
    export PROJECT="$(gcloud config get-value project)"
    export DATASET="samples"
    export TABLE="kafka_to_bigquery"

    bq mk --dataset "$PROJECT:$DATASET"
    ```

1. Clone the `java-docs-samples` repository.

    ```sh
    git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
    ```

1. Navigate to the sample code directory.

    ```sh
    cd java-docs-samples/dataflow/flex-templates
    ```

## Kafka to Cloud Storage sample

### Starting the Kafka service

* [kafka/Dockerfile](kafka/Dockerfile)
* [kafka/start-kafka.sh](kafka/start-kafka.sh)
* [kafka/create-topic.sh](kafka/create-topic.sh)

> <details><summary>
> <i>[optional]</i> Run the Kafka service locally for testing.
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
>   --name=kafka \
>   --net=kafka-net \
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

First we need to build the
[Docker](https://docs.docker.com/engine/docker-overview/)
image for the Kafka service.
We are using [Cloud Build](https://cloud.google.com/cloud-build) so we don't
need a local installation of Docker.

> *Note:* You can speed up subsequent builds with
> [Kaniko cache](https://cloud.google.com/cloud-build/docs/kaniko-cache)
> in Cloud Build.
>
> ```sh
> # [optional] Enable to use Kaniko cache by default.
> gcloud config set builds/use_kaniko True
> ```

Cloud Build allows you to
[build a Docker image using a `Dockerfile`](https://cloud.google.com/cloud-build/docs/quickstart-docker#build_using_dockerfile).
and saves it into
[Container Registry](https://cloud.google.com/container-registry/),
where the image is accessible to other Google Cloud products.

Images starting with `gcr.io/<PROJECT>/` are saved into your project's
Container Registry.

```sh
export KAFKA_IMAGE="gcr.io/$PROJECT/samples/dataflow/kafka:latest"

# Build the image into Container Registry, this is roughly equivalent to:
#   gcloud auth configure-docker
#   docker image build -t $KAFKA_IMAGE kafka/
#   docker push $KAFKA_IMAGE
gcloud builds submit --tag $KAFKA_IMAGE kafka/
```

The Kafka service must be accessible to *external* applications.
For this we need a
[static IP address](https://cloud.google.com/compute/docs/ip-addresses/reserve-static-external-ip-address)
for the Kafka service to live.

```sh
# Select your default compute/region, or default to "us-central1".
export REGION=${$(gcloud config get-value compute/region):-"us-central1"}

# Create a new static IP address for the Kafka service to use.
gcloud compute addresses create --region "$REGION" kafka-address

# Get the static address into a variable.
export KAFKA_ADDRESS=$(gcloud compute addresses describe --region="$REGION" --format='value(address)' kafka-address)
```

> *Note:* Do not use `--global` to create the static IP address since the
> Kafka service must reside in a specific region.

We also need to
[create a firewall rule](https://cloud.google.com/compute/docs/containers/configuring-options-to-run-containers#publishing_container_ports)
to allow incoming messages to the server.
Kafka uses port `9092` and Zookeeper uses port `2181` by default, unless
configured differently.

```sh
# Create a firewall rule to open the port used by Zookeeper and Kafka.
# Allow connections to port 9092 to VM instances with the "kafka-server" tag.
gcloud compute firewall-rules create allow-kafka \
  --target-tags "kafka-server" \
  --allow tcp:2181,tcp:9092
```

Now we can start a new
[Compute Engine](https://cloud.google.com/compute/)
VM (Virtual Machine) instance for the Kafka service
[using the Docker image](https://cloud.google.com/compute/docs/instances/create-start-instance#from-container-image)
we created in Container Registry.

For this sample, we don't need a high performance VM, so we are using an
[e2-small](https://cloud.google.com/compute/docs/machine-types#e2_shared-core_machine_types)
machine with shared CPU cores for a more cost-effective option.

To learn more about pricing, see the
[VM instances pricing](https://cloud.google.com/compute/vm-instance-pricing) page.

```sh
# Select your default compute/zone, or default to "$REGION-a".
# Note that the zone *must* be in the same region as the static IP address.
export ZONE=${$(gcloud config get-value compute/zone):-"$REGION-a"}

# Create and start a new instance.
# The --address flag binds the VM's address to the static address we created.
# The --container-env KAFKA_ADDRESS is an environment variable passed to the
# container to configure Kafka to use the static address of the VM.
# The --tags "kafka-server" is used by the firewakll rule (next step).
gcloud compute instances create-with-container kafka-vm --zone "$ZONE" \
  --machine-type "e2-small" \
  --address "$KAFKA_ADDRESS" \
  --container-image "$KAFKA_IMAGE" \
  --container-env "KAFKA_ADDRESS=$KAFKA_ADDRESS" \
  --tags "kafka-server"
```

### Building the Flex Template image

* [Dockerfile](Dockerfile)
* [KafkaToBigQuery.java](src/main/java/org/apache/beam/samples/KafkaToBigQuery.java)
* [pom.xml](pom.xml)
* [command-spec.json](command-spec.json)

> <details><summary>
> <i>[optional]</i> Run the Apache Beam pipeline for testing.
> <i>(Click to expand)</i>
> </summary>
>
> ```sh
> # If you omit the --bootstrapServer argument, it connects to localhost.
> # If you are running the Kafka server locally, you can omit --bootstrapServer.
> mvn compile exec:java \
>   -Dexec.mainClass=org.apache.beam.samples.KafkaToBigQuery \
>   -Dexec.args="\
>     --outputTable=$PROJECT:$DATASET.$TABLE \
>     --bootstrapServer=$KAFKA_IP:9092"
> ```
>
> </details>

```sh
# Build and package the application as a "fat jar" file.
mvn clean package

# [optional] Note the size of the fat jar compared to the original.
ls -lh target/*.jar
```

The [container-spec.json](container-spec.json) file contains the link to the
reference to the Container Registry image, as well as the SDK information and
input parameters to run the template.

```sh
export TEMPLATE_IMAGE="gcr.io/$PROJECT/samples/dataflow/kafka-to-bigquery:latest"
export TEMPLATE_PATH="gs://$BUCKET/samples/dataflow/kafka_to_bigquery/container-spec.json"

# Build the Dataflow Flex Template image.
gcloud builds submit --tag $TEMPLATE_IMAGE .

# Copy the container-spec.json file to Cloud Storage, making sure to replace
# the project ID placeholder with your project ID.
sed "s|<IMAGE>|$TEMPLATE_IMAGE|g" container-spec.json | gsutil cp - $TEMPLATE_PATH

# To launch the template using the container spec in Cloud Storage.
curl -X POST \
  "https://dataflow-staging.sandbox.googleapis.com/v1b3/projects/$PROJECT/locations/us-central1/flexTemplates:launch" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $(gcloud auth print-access-token)" \
  -d '{
    "launch_parameter": {
      "jobName": "kafka-to-bigquery-'$(date +%Y%m%d-%H%M%S)'",
      "parameters": {
        "outputTable": "'$PROJECT:$DATASET.$TABLE'",
        "bootstrapServer": "'$KAFKA_IP':9092"
      },
      "container_spec_gcs_path": "'$TEMPLATE_PATH'"
    }
  }'
```

#### Clean up

```sh
gcloud compute instances delete kafka-vm
gcloud container images delete gcr.io/[PROJECT-ID]/quickstart-image:tag1 --force-delete-tags
```
