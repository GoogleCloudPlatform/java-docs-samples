# Google Cloud Endpoints
This sample demonstrates how to use Google Cloud Endpoints using Java.

## Pre-deployment steps

### Edit and deploy the OpenAPI specification

1. Open the [src/main/appengine/swagger.yaml](src/main/appengine/swagger.yaml) file in your favorite editor, and replace the YOUR-PROJECT-ID `host` line with your actual Google Cloud Platform project id.

2. Deploy the service configuration. For information on how to do this, see the Configuring Endpoints and Deploying the Sample API sections [here](https://cloud.google.com/endpoints/docs/quickstart-container-engine).

## Deploying to Google App Engine Flexible Environment

### Running locally
    $ mvn jetty:run

### Deploying
    $ mvn gcloud:deploy

### Calling your API

Please refer to the Google Cloud Endpoints [documentation](https://cloud.google.com/endpoints/docs/app-engine/) for App Engine Flexible Environment to learn about creating an API Key and calling your API.

## Deploying to Google Container Engine

### Deploy the sample API to the GKE cluster

To deploy to a cluster:

For instructions on how to create a GKE cluster, please refer to the GKE [documentation](https://cloud.google.com/container-engine/docs/quickstart).

1. Edit the Kubernetes configuration file called gke.yaml in this directory, replacing SERVICE_NAME and SERVICE_VERSION shown in the snippet below with the values returned when you deployed the API:

    ```
    containers:
      - name: esp
        image: b.gcr.io/endpoints/endpoints-runtime:0.3
        args: [
          "-p", "8080",            # the port ESP listens on
          "-a", "127.0.0.1:8081",  # the backend address
          "-s", "SERVICE_NAME",
          "-v", "SERVICE_VERSION",
        ]
    ```

2. Start the service using the kubectl create command:

    ```
    kubectl create -f gke.yaml
    ```

3. Get the service's external IP address (it can take a few minutes after you start your service in the container before the external IP address is ready):

    ```
    kubectl get service
    ```

4. [Create an API key](https://console.cloud.google.com/apis/credentials) in the API credentials page.
  * Click Create credentials, then select API key > Server key, then click Create.
  * Copy the key, then paste it into the following export statement:

    ```
    export ENDPOINTS_KEY=AIza...
    ```

5. Test the app by making a call to the EXTERNAL-IP returned in the previous step, at port 8080. For example, this curl will test the app:

    ```
    curl -d '{"message":"hello world"}' -H "content-type:application/json" http://[EXTERNAL-IP]/echo?key=${ENDPOINTS_KEY}
    ```
