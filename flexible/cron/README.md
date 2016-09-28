# App Engine Cron Service sample for Google App Engine
This sample demonstrates how to deploy App Engine Cron Service to ping a servlet deployed in the app.

## Running locally
    $ mvn jetty:run

## Deploying app
    $ mvn gcloud:deploy

## Deploying cron job
    $ gcloud app deploy cron.yaml