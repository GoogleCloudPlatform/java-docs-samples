# Users Authentication sample for Google App Engine

This sample demonstrates how to use the [Logs API][log-docs] on [Google App
Engine][ae-docs].

[log-docs]: https://cloud.google.com/appengine/docs/java/logs/
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Running locally 

The Logs API only generates output for deployed apps, so this program should not be run locally.

## Deploying
 
This example uses the
[Maven gcloud plugin](https://cloud.google.com/appengine/docs/java/managed-vms/maven).

In the following command, replace YOUR-PROJECT-ID with your
[Google Cloud Project ID](https://support.google.com/cloud/answer/6158840) and SOME-VERSION with the desired version number.

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

