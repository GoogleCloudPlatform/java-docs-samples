# Connecting to Cloud SQL - MySQL with Java 11 on Google App Engine Standard

This sample demonstrates how to use
[Cloud SQL - MySQL](https://cloud.google.com/sql/docs/mysql/) on Google App
Engine Standard. 

## Setup the Sample App

- See [Prerequisites](../README.md#Prerequisites).

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

- Move into the `appengine-java11/cloudsql` directory and compile the app:
```
  cd ../cloudsql
  mvn package
```

- Create a 2nd Gen Cloud SQL Instance by following these
[instructions](https://cloud.google.com/sql/docs/mysql/create-instance). Note the connection string,
database user, and database password that you create.

- Create a database for your application by following these
[instructions](https://cloud.google.com/sql/docs/mysql/create-manage-databases). Note the database
name.

- Create a service account with the 'Cloud SQL Client' permissions by following these
[instructions](https://cloud.google.com/sql/docs/mysql/connect-external-app#4_if_required_by_your_authentication_method_create_a_service_account).
Download a JSON key to use to authenticate your connection.

1. Use the information noted in the previous steps:
```bash
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service/account/key.json
export CLOUD_SQL_CONNECTION_NAME='<MY-PROJECT>:<INSTANCE-REGION>:<MY-DATABASE>'
export DB_USER='my-db-user'
export DB_PASS='my-db-pass'
export DB_NAME='my_db'
```
Note: Saving credentials in environment variables is convenient, but not secure - consider a more
secure solution such as [Cloud KMS](https://cloud.google.com/kms/) to help keep secrets safe.

## Google App Engine Standard

To run on GAE-Standard, create an AppEngine project by following the setup for these
[instructions](https://cloud.google.com/appengine/docs/standard/java/quickstart#before-you-begin)
and verify that
[appengine-maven-plugin](https://cloud.google.com/java/docs/setup#optional_install_maven_or_gradle_plugin_for_app_engine)
 has been added in your build section as a plugin.

### Deploy to Google Cloud

First, update `src/main/appengine/app.yaml` with the correct values to pass the
environment variables into the runtime.

Next, the following command will deploy the application to your Google Cloud project:
```
mvn clean package appengine:deploy -Dapp.deploy.projectId=<your-project-id>
```

View your application:
```
gcloud app browse
```
or by visiting `https://<your-project-id>.appspot.com`.
