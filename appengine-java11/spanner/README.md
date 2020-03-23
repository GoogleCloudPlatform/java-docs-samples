# Google Cloud Spanner Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java11/spanner/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use [Google Cloud Spanner][spanner-docs]
from [Google App Engine standard Java 11 environment][ae-docs].

[spanner-docs]: https://cloud.google.com/spanner/docs/
[ae-docs]: https://cloud.google.com/appengine/docs/java/


## Setup your Google Cloud Platform Project
- Install the [Google Cloud SDK](https://cloud.google.com/sdk/) and run:
```
   gcloud init
```
If this is your first time creating an App engine application:
```
   gcloud app create
```

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

- [Create a Spanner instance](https://cloud.google.com/spanner/docs/quickstart-console#create_an_instance).

- Update `SPANNER_INSTANCE` value in `[app.yaml](src/main/appengine/app.yaml)` with your instance id.

- Move into the `appengine-java11/spanner` directory and compile the app:
```
  cd ../spanner
  mvn package
```

## Endpoints
- `/spanner` : will run sample operations against the spanner instance in order. Individual tasks can be run
using the `task` query parameter. See [SpannerTasks](src/main/java/com/example/appengine/spanner/SpannerTasks.java)
for supported set of tasks.
Note : by default all the spanner example operations run in order, this operation may take a while to return.

## Deploying
```
mvn clean package appengine:deploy
```

To see the results of the deployed sample application, open
`https://PROJECT_ID.appspot.com/spanner` in a web browser.
Note : by default all the spanner example operations run in order, this operation may take a while to show results.
