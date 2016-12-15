# Cloud SQL sample for Google App Engine Flexible
This sample demonstrates how to use [Cloud SQL](https://cloud.google.com/sql/) on Google App Engine
Flexible.

## Setup
Before you can run or deploy the sample, you will need to create a [Cloud SQL instance)](https://cloud.google.com/sql/docs/create-instance)

1. Create a new user and database for the application. The easiest way to do this is via the [Google
Developers Console](https://console.cloud.google.com/sql/instances). Alternatively, you can use
MySQL tools such as the command line client or workbench.
2. Change the root password (under Access Control) and / or create a new user / password.
3. Create a Database (under Databases) (or use MySQL with `gcloud sql connect <instance> --user=root`)
4. Note the **Instance connection name** under Overview > Properties
(It will look like project:region:zone for 2nd Generation)

## Deploying

```bash
$ mvn clean appengine:deploy -DINSTANCE_CONNECTION_NAME=instanceConnectionName -Duser=root
-Dpassword=myPassword -Ddatabase=myDatabase
```

Or you can update the properties in `pom.xml`

## Running locally

```bash
$ mvn clean jetty:run -DINSTANCE_CONNECTION_NAME=instanceConnectionName -Duser=root -Dpassword=myPassowrd -Ddatabase=myDatabase
```
