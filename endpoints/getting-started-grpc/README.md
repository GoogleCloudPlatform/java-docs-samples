# Endpoints Getting Started with gRPC & Java Quickstart

It is assumed that you have a
working
[gRPC](http://www.grpc.io/docs/),
[ProtoBuf](https://github.com/google/protobuf#protocol-compiler-installation) and
Java environment, a Google Cloud account
and [SDK](https://cloud.google.com/sdk/) configured.

1. Build the code:

    ```bash
    ./gradlew build
    ```

1. Test running the code, optional:

    ```bash
    # In the background or another terminal run the server:
    java -jar server/build/libs/server.jar

    # Check the client parameters:
    java -jar client/build/libs/client.jar --help

    # Run the client
    java -jar client/build/libs/client.jar --greetee 'Endpoints!'
    ```

1. Generate the `out.pb` from the proto file.

    ```bash
    protoc --include_imports --include_source_info api/src/main/proto/helloworld.proto --descriptor_set_out out.pb
    ```

1. Edit, `api_config.yaml`. Replace `MY_PROJECT_ID` with your project id.

1. Deploy your service config to Service Management:

    ```bash
    gcloud endpoints services deploy out.pb api_config.yaml
    # The Config ID should be printed out, looks like: 2017-02-01r0, remember this

    # set your project to make commands easier
    GCLOUD_PROJECT=<Your Project ID>

    # Print out your Config ID again, in case you missed it
    gcloud endpoints configs list --service hellogrpc.endpoints.${GCLOUD_PROJECT}.cloud.goog
    ```

1. Also get an API key from the Console's API Manager for use in the client later. (https://console.cloud.google.com/apis/credentials)

1. Build a docker image for your gRPC server, store in your Registry

    ```bash
    gcloud container builds submit --tag gcr.io/${GCLOUD_PROJECT}/java-grpc-hello:1.0 .
    ```

1. Either deploy to GCE (below) or GKE (further down)

### GCE

1. Create your instance and ssh in.

    ```bash
    gcloud compute instances create grpc-host --image-family gci-stable --image-project google-containers --tags=http-server
    gcloud compute ssh grpc-host
    ```

1. Set some variables to make commands easier

    ```bash
    GCLOUD_PROJECT=$(curl -s "http://metadata.google.internal/computeMetadata/v1/project/project-id" -H "Metadata-Flavor: Google")
    SERVICE_NAME=hellogrpc.endpoints.${GCLOUD_PROJECT}.cloud.goog
    ```

1. Pull your credentials to access Container Registry, and run your gRPC server container

    ```bash
    /usr/share/google/dockercfg_update.sh
    docker run --detach --name=grpc-hello gcr.io/${GCLOUD_PROJECT}/java-grpc-hello:1.0
    ```

1. Run the Endpoints proxy

    ```bash
    docker run \
        --detach \
        --name=esp \
        --publish 80:9000 \
        --link=grpc-hello:grpc-hello \
        gcr.io/endpoints-release/endpoints-runtime:1 \
        --service=${SERVICE_NAME} \
        --rollout_strategy=managed \
        --http2_port=9000 \
        --backend=grpc://grpc-hello:50051
    ```

1. Back on your local machine, get the external IP of your GCE instance.

    ```bash
    gcloud compute instances list
    ```

1. Run the client

    ```bash
    java -jar client/build/libs/client.jar --host <IP of GCE Instance>:80 --api_key <API Key from Console>
    ```

### GKE

1. Create a cluster

    ```bash
    gcloud container clusters create my-cluster
    ```

1. Edit `deployment.yaml`. Replace `SERVICE_NAME` and `GCLOUD_PROJECT` with your values.

1. Deploy to GKE

    ```bash
    kubectl create -f ./deployment.yaml
    ```

1. Get IP of load balancer, run until you see an External IP.

    ```bash
    kubectl get svc grpc-hello
    ```

1. Run the client

    ```bash
    java -jar client/build/libs/client.jar --host <IP of GKE LoadBalancer>:80 --api_key <API Key from Console>
    ```
