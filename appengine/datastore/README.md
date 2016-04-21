# Google Cloud Datastore Sample

This sample demonstrates how to use [Google Cloud Datastore][java-datastore]
from [Google App Engine standard environment][ae-docs].

[java-datastore]: https://cloud.google.com/appengine/docs/java/datastore/
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Setup
1. Update the `<application>` tag in `src/main/webapp/WEB-INF/appengine-web.xml`
   with your project name.
1. Update the `<version>` tag in `src/main/webapp/WEB-INF/appengine-web.xml`
   with your version name.

## Running locally
    $ mvn appengine:devserver

## Deploying
    $ mvn appengine:update
