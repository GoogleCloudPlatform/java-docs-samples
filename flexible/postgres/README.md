# PostgreSQL sample for Google App Engine Flexible

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=flexible/postgres/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use [Cloud SQL](https://cloud.google.com/sql/) on Google App
Engine Flexible

## Setup

* If you haven't already, Download and initialize the [Cloud SDK](https://cloud.google.com/sdk/)

    `gcloud init`

* If you haven't already, Create an App Engine app within the current Google Cloud Project

    `gcloud app create`

* If you haven't already, Setup
[Application Default Credentials](https://developers.google.com/identity/protocols/application-default-credentials)

    `gcloud auth application-default login`

* [Create an instance](https://cloud.google.com/sql/docs/postgresql/create-instance)

* [Create a Database](https://cloud.google.com/sql/docs/postgresql/create-manage-databases)

* [Create a user](https://cloud.google.com/sql/docs/postgresql/create-manage-users)

* Note the **Instance connection name** under Overview > properties

Looks like:  `projectID:region:instance`

## Running locally

```bash
$ mvn clean jetty:run -DINSTANCE_CONNECTION_NAME=instanceConnectionName -Duser=root -Dpassword=myPassowrd -Ddatabase=myDatabase
```

## Deploying

```bash
$ mvn clean appengine:deploy -DINSTANCE_CONNECTION_NAME=instanceConnectionName -Duser=root
-Dpassword=myPassword -Ddatabase=myDatabase
```


## Cleaning up

* [Delete your Instance](https://cloud.google.com/sql/docs/postgresql/delete-instance)

