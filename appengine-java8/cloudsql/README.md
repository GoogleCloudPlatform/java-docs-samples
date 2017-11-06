# Cloud SQL sample for Google App Engine

This sample demonstrates how to use [Cloud SQL](https://cloud.google.com/cloudsql/) on Google App
Engine standard Java 8

## Setup

* If you haven't already, Download and initialize the [Cloud SDK](https://cloud.google.com/sdk/)

    `gcloud init`

* If you haven't already, Create an App Engine app within the current Google Cloud Project

    `gcloud app create`

* If you haven't already, Setup
[Application Default Credentials](https://developers.google.com/identity/protocols/application-default-credentials)

    `gcloud auth application-default login`


* [Create an instance](https://cloud.google.com/sql/docs/mysql/create-instance)

* [Create a Database](https://cloud.google.com/sql/docs/mysql/create-manage-databases)

* [Create a user](https://cloud.google.com/sql/docs/mysql/create-manage-users)

* Note the **Instance connection name** under Overview > properties

## Running locally

```bash
$ mvn clean appengine:run -DINSTANCE_CONNECTION_NAME=instanceConnectionName -Duser=root -Dpassword=myPassowrd -Ddatabase=myDatabase
```

## Deploying

```bash
$ mvn clean appengine:deploy -DINSTANCE_CONNECTION_NAME=instanceConnectionName -Duser=root
-Dpassword=myPassword -Ddatabase=myDatabase
```


## Cleaning up

* [Delete your Instance](https://cloud.google.com/sql/docs/mysql/delete-instance)

