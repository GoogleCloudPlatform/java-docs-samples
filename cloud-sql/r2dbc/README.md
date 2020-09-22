# Connecting to Cloud SQL - MySQL

## Before you begin

1. If you haven't already, set up a Java Development Environment (including google-cloud-sdk and
maven utilities) by following the [java setup guide](https://cloud.google.com/java/docs/setup) and
[create a project](https://cloud.google.com/resource-manager/docs/creating-managing-projects#creating_a_project).

1. Create a 2nd Gen Cloud SQL Instance by following these
[instructions](https://cloud.google.com/sql/docs/mysql/create-instance). Note the connection string,
database user, and database password that you create.

1. Create a database for your application by following these
[instructions](https://cloud.google.com/sql/docs/mysql/create-manage-databases). Note the database
name.

1. Edit `src/resources/application.properties` and enter your connection details in the following format
```
connectionString = r2dbc:gcp:mysql://<user>:<password>@<connection_name>/<db_name>
```
for example
```
connectionString = r2dbc:gcp:mysql://user:123456@my-project:us-central1:r2dbctest/testdb 
```

## Deploying locally

To run this application locally, run the following command inside the project folder:

```bash
mvn spring-boot:run
```

Navigate towards `http://127.0.0.1:8080` to verify your application is running correctly.

### Deploy to Cloud Run

See the [Cloud Run documentation](https://cloud.google.com/run/docs/configuring/connect-cloudsql)
for more details on connecting a Cloud Run service to Cloud SQL.

1. Build the container image:

```sh
mvn compile com.google.cloud.tools:jib-maven-plugin:2.5.2:build \
        -Dimage=gcr.io/[YOUR_PROJECT_ID]/r2dbc-sample
```

2. Deploy the service to Cloud Run:

```sh
gcloud run deploy r2dbc-sample \
    --image gcr.io/[YOUR_PROJECT_ID]/r2dbc-sample \
    --platform managed
```

Take note of the URL output at the end of the deployment process.

3. Navigate your browser to the URL noted in step 2.

  For more details about using Cloud Run see http://cloud.run.
  Review other [Java on Cloud Run samples](../../../run/).
