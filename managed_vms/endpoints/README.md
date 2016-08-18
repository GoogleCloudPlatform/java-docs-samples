# Google Cloud Endpoints on App Engine flexible environment
This sample demonstrates how to use Google Cloud Endpoints on Google App Engine Flexible Environment using Java.

## Edit the Swagger API specification

Open the [src/main/appengine/swagger.yaml](src/main/appengine/swagger.yaml) file in your favorite editor, and replace the YOUR-PROJECT-ID `host` line with your actual Google Cloud Platform project Id.

## Running locally
    $ mvn jetty:run

## Deploying
    $ mvn gcloud:deploy

## Calling your API

Please refer to the Google Cloud Endpoints [documentation](https://cloud.google.com/endpoints/docs/app-engine/) for App Engine Flexible Environment to learn about creating an API Key and calling your API.
