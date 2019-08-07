# Google App Engine Standard Environment Search API Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/search/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


Samples for the Java 8 runtime can be found [here](/appengine-java8).

This sample demonstrates how to use App Engine Search API.

See the [Google App Engine Search API documentation][search-api-docs] for more
detailed instructions.

[search-api-docs]: https://cloud.google.com/appengine/docs/java/search/

## Setup
1. Update the `<application>` tag in `src/main/webapp/WEB-INF/appengine-web.xml`
   with your project name.
1. Update the `<version>` tag in `src/main/webapp/WEB-INF/appengine-web.xml`
   with your version name.

## Running locally
    $ mvn appengine:devserver

## Deploying
    $ mvn appengine:update
