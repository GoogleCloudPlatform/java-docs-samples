# Connecting to Cloud SQL - MySQL with Java 11 on Google App Engine Standard

This sample demonstrates how to use
[Cloud SQL - MySQL](https://cloud.google.com/sql/docs/mysql/) on
Google App Engine Standard.

## Code

The sample code is located in `java-docs-samples/cloud-sql/mysql/servlet`. This directory has the supplemental files needed to deploy to Google App Engine Standard with Java 11.

## Setup your Cloud SQL Database

- Create a 2nd Gen Cloud SQL Instance by following these
[instructions](https://cloud.google.com/sql/docs/mysql/create-instance). Note the connection string,
database user, and database password that you create.

- Create a database for your application by following these
[instructions](https://cloud.google.com/sql/docs/mysql/create-manage-databases). Note the database
name.

- Create a service account with the 'Cloud SQL Client' permissions by following these
[instructions](https://cloud.google.com/sql/docs/mysql/connect-external-app#4_if_required_by_your_authentication_method_create_a_service_account).
Download a JSON key to use to authenticate your connection.

## Setup the App

- See [Prerequisites](../README.md#Prerequisites).

- Add the [appengine-simple-jetty-main](../README.md#appengine-simple-jetty-main)
Main class to your classpath:
```
  cd java-docs-samples/appengine-java11/appengine-simple-jetty-main
  mvn install
```

- Move into the sample directory:
```
cd ../../cloud-sql/mysql/servlet/
```

- In the new Java 11 runtime, you must remove your `appengine-web.xml` file and create an `app.yaml` file to configure your application settings. Create an `src/main/appengine` directory and copy the `app.yaml` provided:
```bash
mkdir src/main/appengine
cp ../../../appengine-java11/cloudsql/app.yaml src/main/appengine/
```

- Use the information from creating your database to replace the
environment variables in your `app.yaml`:
```YAML
CLOUD_SQL_CONNECTION_NAME: '<MY-PROJECT>:<INSTANCE-REGION>:<MY-DATABASE>'
DB_NAME: 'my_db'
DB_USER: 'my-db-user'
DB_PASS: 'my-db-pass'
```

- Java 11 has specific requirements on packaging your app. Replace the `pom.xml` with the Java 11 `pom.xml`:
```bash
cp ../../../appengine-java11/cloudsql/pom.xml ./
```

### Deploy to Google Cloud

The following command will deploy the application to your Google Cloud project:
```
mvn clean package appengine:deploy
```

View your application:
```
gcloud app browse
```
or by visiting `https://<your-project-id>.appspot.com`.
