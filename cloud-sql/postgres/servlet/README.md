# Connecting to Cloud SQL - Postgres

## Before you begin

1. If you haven't already, set up a Java Development Environment (including google-cloud-sdk and 
maven utilities) by following the [java setup guide](https://cloud.google.com/java/docs/setup) and 
[create a project](https://cloud.google.com/resource-manager/docs/creating-managing-projects#creating_a_project).

1. Create a 2nd Gen Cloud SQL Instance by following these 
[instructions](https://cloud.google.com/sql/docs/postgres/create-instance). Note the connection string,
database user, and database password that you create.

1. Create a database for your application by following these 
[instructions](https://cloud.google.com/sql/docs/postgres/create-manage-databases). Note the database
name. 

1. Create a service account with the 'Cloud SQL Client' permissions by following these 
[instructions](https://cloud.google.com/sql/docs/postgres/connect-external-app#4_if_required_by_your_authentication_method_create_a_service_account).
Download a JSON key to use to authenticate your connection. 

1. Use the information noted in the previous steps:
```bash
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service/account/key.json
export INSTANCE_CONNECTION_NAME='<MY-PROJECT>:<INSTANCE-REGION>:<INSTANCE-NAME>'
export DB_USER='my-db-user'
export DB_PASS='my-db-pass'
export DB_NAME='my_db'
```
Note: Saving credentials in environment variables is convenient, but not secure - consider a more
secure solution such as [Secret Manager](https://cloud.google.com/secret-manager/) to help keep secrets safe.

## Configure SSL Certificates
For deployments that connect directly to a Cloud SQL instance with TCP,
without using the Cloud SQL Proxy,
configuring SSL certificates will ensure the connection is encrypted.
1. Use the gcloud CLI to [download the server certificate](https://cloud.google.com/sql/docs/mysql/configure-ssl-instance#server-certs) for your Cloud SQL instance.
    - Get information about the service certificate:
        ```
        gcloud beta sql ssl server-ca-certs list --instance=INSTANCE_NAME
        ```
    - Create a server certificate:
        ```
        gcloud beta sql ssl server-ca-certs create --instance=INSTANCE_NAME
        ```
    - Download the certificate information to a local PEM file
        ```
        gcloud beta sql ssl server-ca-certs list \
          --format="value(cert)" \
          --instance=INSTANCE_NAME > \
          server-ca.pem
        ```

2. Use the gcloud CLI to [create and download a client public key certificate and client private key](https://cloud.google.com/sql/docs/postgres/configure-ssl-instance#client-certs)
    - Create a client certificate using the ssl client-certs create command:
        ```
        gcloud sql ssl client-certs create CERT_NAME client-key.pem --instance=INSTANCE_NAME
        ```
    - Retrieve the public key for the certificate you just created and copy it into the client-cert.pem file with the ssl client-certs describe command:
      ```
      gcloud sql ssl client-certs describe CERT_NAME \
      --instance=INSTANCE_NAME \
      --format="value(cert)" > client-cert.pem
        ```
3. Convert the downloaded PEM certificate and key to a PKCS12 archive using `openssl`:
   ```
   openssl pkcs12 -export -in client-cert.pem -inkey client-key.pem \
   -name "mysqlclient" -passout pass:<password> -out client-keystore.p12
   ```
4. Set the `SSL_CLIENT_KEY_PATH` and `SSL_CLIENT_KEY_PASSWD` environment variables to the values from the previous step. 
The client key path should point to the PKCS12 archive file.
6. Set the `SSL_SERVER_CA_PATH` environment variables to point to the `server-ca.pem` file downloaded earlier

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


### App Engine Development Server

The following command will run the application locally in the the GAE-development server:
```bash
mvn appengine:run
```

### Cloud Functions Development Server
To run the application locally as a Cloud Function, run the following command:
```
mvn function:run -Drun.functionTarget=com.example.cloudsql.functions.Main
```

### Deploy to Google App Engine

First, update [`src/main/webapp/WEB-INF/appengine-web.xml`](src/main/webapp/WEB-INF/appengine-web.xml)
with the correct values to pass the environment variables into the runtime.

Next, the following command will deploy the application to your Google Cloud project:
```bash
mvn clean package appengine:deploy -DskipTests
```

### Deploy to Cloud Run

See the [Cloud Run documentation](https://cloud.google.com/run/docs/configuring/connect-cloudsql)
for more details on connecting a Cloud Run service to Cloud SQL.

1. Build the container image using [Jib](https://cloud.google.com/java/getting-started/jib):

  ```sh
mvn clean package com.google.cloud.tools:jib-maven-plugin:2.8.0:build \
 -Dimage=gcr.io/[YOUR_PROJECT_ID]/run-postgres -DskipTests
  ```

2. Deploy the service to Cloud Run:

  ```sh
  gcloud run deploy run-postgres \
    --image gcr.io/[YOUR_PROJECT_ID]/run-postgres \
    --platform managed \
    --allow-unauthenticated \
    --region [REGION] \
    --update-env-vars INSTANCE_CONNECTION_NAME=[INSTANCE_CONNECTION_NAME] \
    --update-env-vars DB_USER=[MY_DB_USER] \
    --update-env-vars DB_PASS=[MY_DB_PASS] \
    --update-env-vars DB_NAME=[MY_DB]
  ```

  Replace environment variables with the correct values for your Cloud SQL
  instance configuration.

  Take note of the URL output at the end of the deployment process.

  It is recommended to use the [Secret Manager integration](https://cloud.google.com/run/docs/configuring/secrets) for Cloud Run instead
  of using environment variables for the SQL configuration. The service injects the SQL credentials from
  Secret Manager at runtime via an environment variable.

  Create secrets via the command line:
  ```sh
  echo -n "my-awesome-project:us-central1:my-cloud-sql-instance" | \
      gcloud secrets versions add INSTANCE_CONNECTION_NAME_SECRET --data-file=-
  ```

  Deploy the service to Cloud Run specifying the env var name and secret name:
  ```sh
  gcloud beta run deploy SERVICE --image gcr.io/[YOUR_PROJECT_ID]/run-sql \
      --add-cloudsql-instances [INSTANCE_CONNECTION_NAME] \
      --update-secrets INSTANCE_CONNECTION_NAME=[INSTANCE_CONNECTION_NAME_SECRET]:latest,\
        DB_USER=[DB_USER_SECRET]:latest, \
        DB_PASS=[DB_PASS_SECRET]:latest, \
        DB_NAME=[DB_NAME_SECRET]:latest
  ```

3. Navigate your browser to the URL noted in step 2.

  For more details about using Cloud Run see http://cloud.run.
  Review other [Java on Cloud Run samples](../../../run/).

### Deploy to Google Cloud Functions

To deploy the application to Cloud Functions, first fill in the values for required environment variables in `.env.yaml`. Then run the following command
```
gcloud functions deploy sql-sample \
  --trigger-http \
  --entry-point com.example.cloudsql.functions.Main \
  --runtime java11 \
  --env-vars-file .env.yaml
```
