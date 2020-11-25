# Connecting to Cloud SQL - SQL Server

This is a sample application that inserts and reads votes for two options (tabs and spaces) in a Cloud SQL Database. The application demonstrates the reommended method of connecting to Cloud SQL from an Java application using the [Cloud SQL Java Connector](https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory)

## Before you begin

1. If you haven't already, set up a Java Development Environment (including google-cloud-sdk and 
maven utilities) by following the [java setup guide](https://cloud.google.com/java/docs/setup) and 
[create a project](https://cloud.google.com/resource-manager/docs/creating-managing-projects#creating_a_project).

1. Create a 2nd Gen Cloud SQL Instance by following these 
[instructions](https://cloud.google.com/sql/docs/sqlserver/create-instance). Note the connection string,
database user, and database password that you create.

1. Create a database for your application by following these 
[instructions](https://cloud.google.com/sql/docs/sqlserver/create-manage-databases). Note the database
name. 

1. Create a service account with the 'Cloud SQL Client' permissions by following these 
[instructions](https://cloud.google.com/sql/docs/sqlserver/connect-external-app#4_if_required_by_your_authentication_method_create_a_service_account).
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

## Deploying locally

To run this application locally, run the following command inside the project folder:

```bash
mvn jetty:run
```

Navigate towards `http://127.0.0.1:8080` to verify your application is running correctly.

## Google App Engine Standard

To run on GAE-Standard, create an AppEngine project by following the setup for these 
[instructions](https://cloud.google.com/appengine/docs/standard/java/quickstart#before-you-begin) 
and verify that 
[appengine-maven-plugin](https://cloud.google.com/java/docs/setup#optional_install_maven_or_gradle_plugin_for_app_engine)
 has been added in your build section as a plugin.


### Development Server

The following command will run the application locally in the the GAE-development server:
```bash
mvn clean package appengine:run
```

Note: if the GAE development server fails to start, check that you are using a supported version of Java. Supported versions are Java 8 and Java 11.
### Deploy to Google Cloud

First, update `src/main/webapp/WEB-INF/appengine-web.xml` with the correct values to pass the 
environment variables into the runtime.

Next, the following command will deploy the application to your Google Cloud project:
```bash
mvn clean package appengine:deploy
```

### Cleanup
To avoid incurring any charges, navigate to your project's [App Engine settings](https://console.cloud.google.com/appengine/settings) and click `Disable Application`. Also [delete your Cloud SQL Instance](https://cloud.google.com/sql/docs/mysql/delete-instance) if you no longer need it.
