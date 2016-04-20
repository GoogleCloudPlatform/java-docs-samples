# Google App Engine Standard Environment Remote API Sample

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
1. Navigate to the remote-client directory
1. Update the SERVER_STRING constant with the deployed app name 
   `src/main/java/com/example/appengine/remote/RemoteApiExample.java`
   1. If you deployed the app, it should be "YOUR-APP-ID.appspot.com"
   1. If you are running on the development server, it should be "localhost"
1. Package the app as a jar
   `mvn clean package`
1. Excute the jar file
   java -jar appengine-remote-client-1.0-SNAPSHOT-jar-with-dependencies.jar

