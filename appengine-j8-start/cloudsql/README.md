# Google App Engine Standard Environment Cloud SQL Sample

This sample demonstrates how to deploy an App Engine Java 8 application that
uses Cloud SQL for storage.

See the [Google App Engine standard environment documentation][ae-docs] for more
detailed instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Setup

*   If you haven't already, Download and initialize the [Cloud
    SDK](https://cloud.google.com/sdk/)

    `gcloud init`

*   If you haven't already, Create an App Engine app within the current Google
    Cloud Project

    `gcloud app create`

*   If you haven't already, Setup [Application Default
    Credentials](https://developers.google.com/identity/protocols/application-default-credentials)

    `gcloud auth application-default login`

*   [Create an
    instance](https://cloud.google.com/sql/docs/mysql/create-instance)

*   [Create a
    Database](https://cloud.google.com/sql/docs/mysql/create-manage-databases)

*   [Create a user](https://cloud.google.com/sql/docs/mysql/create-manage-users)

*   Note the **Instance connection name** under Overview > properties

*   Update the `<application>` tag in the `pom.xml` with your project name.

*   Update the `<version>` tag in the `pom.xml` with your version name.

## Testing

This examples uses a local MySQL server to run tests.

1.  Download and install [MySQL Community
    Server](https://dev.mysql.com/downloads/mysql/).

1.  Create the database and user for the tests.

1.  Append the database name, username and password in the `serverUrl`
    connection variable in the test files located in
    `src/test/java/com/example/appengine`.

## Running locally

This example uses the [Cloud SDK Maven
plugin](https://cloud.google.com/appengine/docs/java/tools/using-maven). To run
this sample locally:

    $ mvn appengine:run

To see the results of the sample application, open
[localhost:8080](http://localhost:8080) in a web browser.

## Deploying

In the following command, replace YOUR-PROJECT-ID with your [Google Cloud
Project ID](https://developers.google.com/console/help/new/#projectnumber) and
SOME-VERSION with a valid version number.

    $ mvn appengine:deploy
