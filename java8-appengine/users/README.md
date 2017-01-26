# Users Authentication sample for Google App Engine

This sample demonstrates how to use the [Users API][appid] on [Google App
Engine][ae-docs].

[appid]: https://cloud.google.com/appengine/docs/java/users/
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Running locally 
This example uses the
[Maven gcloud plugin](https://cloud.google.com/appengine/docs/java/managed-vms/maven).
To run this sample locally:

    $ mvn appengine:devserver

## Deploying
In the following command, replace YOUR-PROJECT-ID with your
[Google Cloud Project ID](https://developers.google.com/console/help/new/#projectnumber).

    $ mvn appengine:update -Dappengine.appId=YOUR-PROJECT-ID -Dappengine.version=SOME-VERSION

## Setup
To save your project settings so that you don't need to enter the
 parameters, you can:

1. Update the <application> tag in src/main/webapp/WEB-INF/appengine-web.xml
   with your project name.

2. Update the <version> tag in src/main/webapp/WEB-INF/appengine-web.xml
   with a valid version number.


You will now be able to run

    $ mvn appengine:update

without the need for any additional parameters.
