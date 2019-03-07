# Cloud SQL sample for Google App Engine Java 11

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/ludoch/samples&page=editor&open_in_editor=appengine-java11/cloudsql/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use [Cloud SQL](https://cloud.google.com/cloudsql/) on Google App
Engine standard Java 11. Find more information about connecting to [Cloud SQL from App Engine](https://cloud.google.com/sql/docs/mysql/connect-app-engine).

## Setup

* If you haven't already, Download and initialize the [Cloud SDK](https://cloud.google.com/sdk/)

    `gcloud init`

* If this is your first time creating an App engine application:
```
   gcloud app create
```

* If you haven't already, Setup
[Application Default Credentials](https://developers.google.com/identity/protocols/application-default-credentials)

    `gcloud auth application-default login`

* [Create an Instance](https://cloud.google.com/sql/docs/mysql/create-instance)

* [Create a Database](https://cloud.google.com/sql/docs/mysql/create-manage-databases)

* Note the **Instance connection name** under Overview > properties

## Configuring the Env Variables in app.yaml

Edit the environment variables in the [app.yaml](src/main/appengine/app.yaml)
file with your Cloud SQL credentials:

```
env_variables:
  DB_INSTANCE: <project_id>:<instance_region>:<database_instance>
  DB_DATABASE: <database_name>
  DB_USER: <user_name>
  DB_PASSWORD: <password>
```

## Deploying

```bash
mvn clean package appengine:deploy -Dapp.deploy.projectId=<your-project-id>
```

## Cleaning up

* [Delete your Instance](https://cloud.google.com/sql/docs/mysql/delete-instance)
