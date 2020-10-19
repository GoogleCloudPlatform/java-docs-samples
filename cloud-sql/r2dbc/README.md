# Connecting to Cloud SQL - MySQL and Postgres

## Before you begin

1. If you haven't already, set up a Java Development Environment (including google-cloud-sdk and
maven utilities) by following the [Java setup guide](https://cloud.google.com/java/docs/setup) and
[creating a project](https://cloud.google.com/resource-manager/docs/creating-managing-projects#creating_a_project).

1. You can use MySQL or PostgreSQL instance for this sample. 
Create a 2nd Gen Cloud SQL Instance by following corresponding instructions: 
[MySQL](https://cloud.google.com/sql/docs/mysql/create-instance) /
[PostgreSQL](https://cloud.google.com/sql/docs/postgres/create-instance).
Note the connection string, database user, and database password that you create. 

1. Create a database for your application by following corresponding instructions:
[MySQL](https://cloud.google.com/sql/docs/mysql/create-manage-databases) /
[PostgreSQL](https://cloud.google.com/sql/docs/postgres/create-manage-databases).
Note the database name.

1. Assign your connection details in the following format:

    ```
    r2dbc:gcp:<'mysql' or 'postgres'>://<user>:<password>@<connection_name>/<db_name>
    ```
    to an environment variable `CLOUD_SQL_CONNECTION_STRING`.

    Example for MySQL:
    ```sh
    export CLOUD_SQL_CONNECTION_STRING=r2dbc:gcp:mysql://user:123456@my-project:us-central1:r2dbctest/testdb
    ``` 

    Example for PostgreSQL:
    ```sh
    export CLOUD_SQL_CONNECTION_STRING=r2dbc:gcp:postgres://user:123456@my-project:us-central1:r2dbctest/testdb
    ``` 

## Schema

The schema will be created automatically when the application starts.

## Running locally

To run this application locally, run the following command inside the project folder:

```sh
mvn spring-boot:run
```

Navigate to `http://localhost:8080` to verify your application is running correctly.

## Deploy to Google App Engine Standard

To run on GAE-Standard, create an AppEngine project by following the setup for these
[instructions](https://cloud.google.com/appengine/docs/standard/java/quickstart#before-you-begin)
and verify that
[appengine-maven-plugin](https://cloud.google.com/java/docs/setup#optional_install_maven_or_gradle_plugin_for_app_engine)
 has been added in your build section as a plugin.

Edit `src/main/appengine/app.yaml` to set `CLOUD_SQL_CONNECTION_STRING` to your connection string. 

The following command will deploy the application to your Google Cloud project:
```bash
mvn clean package appengine:deploy
```

## Deploy to Cloud Run

See the [Cloud Run documentation](https://cloud.google.com/run/docs/configuring/connect-cloudsql)
for more details on connecting a Cloud Run service to Cloud SQL.

1. Create an environment variable with your GCP project id:
    ```sh
    export PROJECT_ID=[YOUR_PROJECT_ID]
    ```

1. Build the container image and push it to Google Container Registry (GCR):

    ```sh
    mvn compile com.google.cloud.tools:jib-maven-plugin:2.5.2:build \
            -Dimage=gcr.io/$PROJECT_ID/r2dbc-sample
    ```

1. Deploy the service to Cloud Run:

    ```sh
    gcloud run deploy r2dbc-sample \
        --image gcr.io/$PROJECT_ID/r2dbc-sample \
        --platform managed \
        --memory 512Mi \
        --set-env-vars CLOUD_SQL_CONNECTION_STRING=$CLOUD_SQL_CONNECTION_STRING
    ```
    Take note of the URL output at the end of the deployment process.

1. Navigate to the URL noted in Step 2.

  For more details about using Cloud Run see http://cloud.run.
  Review other [Java on Cloud Run samples](../../../run/).
