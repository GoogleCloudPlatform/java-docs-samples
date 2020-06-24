<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/bigquery/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

# Google Cloud API Showcase: BigQuery & Cloud Monitoring in App Engine standard environment for Java 8

This API Showcase demonstrates how to run an App Engine standard environment application with dependencies on both 
[Google BigQuery][bigquery] and [StackDriver Monitoring][monitoring].

[bigquery]: https://cloud.google.com/bigquery/docs
[monitoring]: https://cloud.google.com/monitoring/docs

The home page of this application provides a form to initiate a query of public data, in this case StackOverflow
questions tagged with `google-bigquery`.

The home page also provides a summary view of the metrics that have been logged in the past 30 days.

## Clone the sample app

Copy the sample apps to your local machine, and cd to the `appengine-java8/bigquery` directory:

```
git clone https://github.com/GoogleCloudPlatform/java-docs-samples
cd appengine-java8/bigquery
```

## Setup

- Make sure [`gcloud`](https://cloud.google.com/sdk/docs/) is installed and initialized:
```
   gcloud init
```
- If this is the first time you are creating an App Engine project
```
   gcloud app create
```
- For local development, [set up][set-up] authentication
- Enable [BigQuery][bigquery-api] and [Monitoring][monitoring-api] APIs
  
[set-up]: https://cloud.google.com/docs/authentication/getting-started
[bigquery-api]: https://console.cloud.google.com/launcher/details/google/bigquery-json.googleapis.com
[monitoring-api]: https://console.cloud.google.com/launcher/details/google/monitoring.googleapis.com

## Run locally
Run using shown Maven command. You can then direct your browser to `http://localhost:8080/` to see the most recent query
run (since the app started) and the metrics from the past 30 days.

```
mvn appengine:run
```

Note: The first time the app is run (or after any metrics definitions have been deleted) it may take up to 5 minutes for
the MetricDescriptors to sync with StackDriver before any results are shown. If you do not see results, please wait a
few moments and try again.

## Deploy

- Deploy to App Engine standard environment using the following Maven command.
```
   mvn clean package appengine:deploy
```
- Direct your browser to `https://<your-project-id>.appspot.com`.
- View more in-depth metrics data on the [Cloud Monitoring Dashboard][dashboard]

[dashboard]: https://pantheon.corp.google.com/monitoring

