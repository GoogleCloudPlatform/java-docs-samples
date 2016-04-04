# App Identity sample for Google App Engine

This sample demonstrates how to use the [App Identity API][appid] on [Google App
Engine][ae-docs].

[appid]: https://cloud.google.com/appengine/docs/java/appidentity/
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Running locally
This example uses the
[Maven gcloud plugin](https://cloud.google.com/appengine/docs/java/managed-vms/maven).
To run this sample locally:

    $ mvn gcloud:run

## Deploying
In the following command, replace YOUR-PROJECT-ID with your
[Google Cloud Project ID](https://developers.google.com/console/help/new/#projectnumber).

    $ mvn gcloud:deploy -Dgcloud.gcloud_project=YOUR-PROJECT-ID

## Setup
To save your project settings so that you don't need to enter the
`-Dgcloud.gcloud_project=YOUR-CLOUD-PROJECT-ID` parameters, you can:

1. Update the <application> tag in src/main/webapp/WEB-INF/appengine-web.xml
   with your project name.

You will now be able to run

    $ mvn gcloud:deploy

without the need for any additional parameters.
