# Google App Engine Information App for Java11

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor==appengine-java11/gaeinfo/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to show all system environment metadata and properties on Google App
Engine standard app.

## Setup your Google Cloud Platform Project

* If you haven't already, Download and initialize the [Cloud SDK](https://cloud.google.com/sdk/)

    `gcloud init`

* If you haven't already, Create an App Engine app within the current Google Cloud Project

    `gcloud app create`

* If you haven't already, Setup [Application Default Credentials](https://developers.google.com/identity/protocols/application-default-credentials)

    `gcloud auth application-default login`

## Setup the Sample App

- Copy the sample apps to your local machine:
```
  git clone https://github.com/GoogleCloudPlatform/java-docs-samples
```

- Add the [appengine-simple-jetty-main](../README.md#appengine-simple-jetty-main)
Main class to your classpath:
```
  cd java-docs-samples/appengine-java11/appengine-simple-jetty-main
  mvn install
```

- Move into the `appengine-java11/gaeinfo` directory and compile the app:
```
  cd ../gaeinfo
  mvn package
```

## Deploy

- Deploy to App Engine standard environment using the following Maven command.
```
   mvn clean package appengine:deploy
```
- Direct your browser to `https://<your-project-id>.appspot.com`.
- View more in-depth metrics data on the [StackDriver Monitoring Dashboard][dashboard]

Note: The first time the app is run (or after any metrics definitions have
been deleted) it may take up to 5 minutes for the MetricDescriptors to sync
with StackDriver before any results are shown. If you do not see results,
please wait a few moments and try again.

[dashboard]: https://console.cloud.google.com/monitoring
