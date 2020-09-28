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

1. Edit `src/main/resources/application.properties` and enter your connection details in the following format
    ```
    connectionString = r2dbc:gcp:<'mysql' or 'postgres'>://<user>:<password>@<connection_name>/<db_name>
    ```
    Example for MySQL:
    ```
    connectionString = r2dbc:gcp:mysql://user:123456@my-project:us-central1:r2dbctest/testdb 
    ``` 

    Example for PostgreSQL:
    ```
    connectionString = r2dbc:gcp:postgres://user:123456@my-project:us-central1:r2dbctest/testdb 
    ``` 
## Schema

The schema will be created automatically when the application starts.

## Running locally

To run this application locally, run the following command inside the project folder:

```sh
mvn spring-boot:run
```

Navigate to `http://localhost:8080` to verify your application is running correctly.

### Deploy to Cloud Run

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
        --platform managed
    ```
    Take note of the URL output at the end of the deployment process.

1. Navigate to the URL noted in Step 2.

  For more details about using Cloud Run see http://cloud.run.
  Review other [Java on Cloud Run samples](../../../run/).
