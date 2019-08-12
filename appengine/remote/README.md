# Google App Engine Standard Environment Remote API Sample

**Java 7 runtime support on App Engine standard was [deprecated](https://cloud.google.com/appengine/docs/deprecations/java7) on
December 13, 2017 and will be shut down entirely on January 16, 2019. It is replaced by the
[Java 8 Runtime Environment](https://cloud.google.com/appengine/docs/standard/java/runtime-java8).**

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/remote/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


Samples for the Java 8 runtime can be found [here](/appengine-java8).

This sample demonstrates how to access App Engine Standard Environment APIs remotely,
using the [Remote API](https://cloud.google.com/appengine/docs/java/tools/remoteapi).

## Set up the server component of Remote API
1. Navigate to the remote-server directory
1. Update the `<application>` tag in `src/main/webapp/WEB-INF/appengine-web.xml`
   with your project name.
1. Update the `<version>` tag in `src/main/webapp/WEB-INF/appengine-web.xml`
   with your version name.
1. Deploy the app
   `mvn appengine:update`
1. Alternatively, run the app locally with
   `mvn appengine:devserver`
## Set up the client component of Remote API
1. Package the app as a jar
   `mvn clean package`
1. Navigate to the target directory
1. Excute the jar file with the server connection string as the first argument
   1. If you deployed the app, it should be "YOUR-APP-ID.appspot.com"
   1. If you are running on the development server, it should be "localhost"
   java -jar appengine-remote-client-1.0-SNAPSHOT-jar-with-dependencies.jar "YOUR-APP-NAME"

