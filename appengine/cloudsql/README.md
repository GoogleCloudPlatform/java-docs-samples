# Cloud SQL sample for Google App Engine

**Java 7 runtime support on App Engine standard was [deprecated](https://cloud.google.com/appengine/docs/deprecations/java7) on
December 13, 2017 and will be shut down entirely on January 16, 2019. It is replaced by the
[Java 8 Runtime Environment](https://cloud.google.com/appengine/docs/standard/java/runtime-java8).**

Samples for the Java 8 runtime can be found [here](/appengine-java8).

This sample demonstrates how to use [Cloud SQL](https://cloud.google.com/sql/) on Google App Engine

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/cloudsql/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

## Setup
Before you can run or deploy the sample, you will need to create a [Cloud SQL instance)](https://cloud.google.com/sql/docs/create-instance)

1. Create a new user and database for the application. The easiest way to do this is via the [Google
Developers Console](https://console.cloud.google.com/sql/instances). Alternatively, you can use MySQL tools such as the command line client or workbench.
2. Change the root password (under Access Control) and / or create a new user / password.
3. Create a Database (under Databases) (or use MySQL with `gcloud beta sql connect <instance> --user=root`)
4. Note the **Instance connection name** under Overview > properties
(It will look like project:instance for 1st Generation or project:region:zone for 2nd Generation)

or

```bash
gcloud sql instances describe <instance> | grep connectionName
```

## Deploying

```bash
$ mvn clean appengine:update -DINSTANCE_CONNECTION_NAME=instanceConnectionName -Duser=root
-Dpassword=myPassword -Ddatabase=myDatabase
```

Or you can update the properties in `pom.xml`

## Running locally

```bash
$ mvn clean appengine:devserver -DINSTANCE_CONNECTION_NAME=instanceConnectionName -Duser=root -Dpassword=myPassowrd -Ddatabase=myDatabase
```
Note - you must use a local mysql instance for the 1st Generation instance and change the local Url
in `src/main/webapp/WEB-INF/appengine-web.xml` to use your local server.
