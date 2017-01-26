# Google Cloud Datastore Sample

This sample demonstrates how to use [Google Cloud Datastore][java-datastore]
from [Google App Engine standard environment][ae-docs].

[java-datastore]: https://cloud.google.com/appengine/docs/java/datastore/
[ae-docs]: https://cloud.google.com/appengine/docs/java/


## Running locally

This example uses the
[App Engine Maven plugin](https://cloud.google.com/appengine/docs/java/tools/maven).
To run this sample locally:

    $ mvn appengine:devserver

To see the results of the sample application, open
[localhost:8080](http://localhost:8080) in a web browser.


## Deploying

In the following command, replace YOUR-PROJECT-ID with your
[Google Cloud Project ID](https://developers.google.com/console/help/new/#projectnumber)
and SOME-VERSION with a valid version number.

    $ mvn appengine:update -Dappengine.appId=YOUR-PROJECT-ID -Dappengine.version=SOME-VERSION


## Setup

To save your project settings so that you don't need to enter the
 parameters, you can:

1. Update the `<application>` tag in `src/main/webapp/WEB-INF/appengine-web.xml`
   with your project name.
1. Update the `<version>` tag in `src/main/webapp/WEB-INF/appengine-web.xml`
   with your version name.

You will now be able to run

    $ mvn appengine:update

without the need for any additional parameters.

