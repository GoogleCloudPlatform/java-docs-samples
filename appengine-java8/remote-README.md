# Google App Engine Standard Environment Remote API Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/remote-README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to access App Engine Standard Environment APIs remotely,
using the [Remote API](https://cloud.google.com/appengine/docs/java/tools/remoteapi).

## Set up the server component of Remote API
1. `gcloud init`
1. Navigate to the remote-server directory
1. Deploy the app
   `mvn appengine:deploy`
1. Alternatively, run the app locally with
   `mvn appengine:run`
## Set up the client component of Remote API
1. Package the app as a jar
   `mvn clean package`
1. Navigate to the target directory
1. Excute the jar file with the server connection string as the first argument
   1. If you deployed the app, it should be "YOUR-APP-ID.appspot.com"
   1. If you are running on the development server, it should be "localhost:8080"
   java -jar appengine-remote-client-1.0-SNAPSHOT-jar-with-dependencies.jar "YOUR-APP-NAME"

