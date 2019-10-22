# App Engine Cron Service sample for Google App Engine

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=flexible/cron/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to deploy App Engine Cron Service to ping a servlet deployed in the app.

## Running locally
    $ mvn jetty:run

## Deploying app
    $ mvn gcloud:deploy

## Deploying cron job
    $ gcloud app deploy cron.yaml