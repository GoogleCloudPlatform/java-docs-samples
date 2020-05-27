# Google App Engine Standard Environment Samples for Java 11

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java11/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This is a repository that contains Java code samples for [Google App Engine][ae-docs]
standard Java 11 environment.

[ae-docs]: https://cloud.google.com/appengine/docs/standard/java11/

## Prerequisites

### Download Maven

These samples use the [Apache Maven][maven] build system. Before getting
started, be sure to [download][maven-download] and [install][maven-install] it.
When you use Maven as described here, it will automatically download the needed
client libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

### Create a Project in the Google Cloud Platform Console

If you haven't already created a project, create one now. Projects enable you to
manage all Google Cloud Platform resources for your app, including deployment,
access control, billing, and services.

1. Open the [Cloud Platform Console][cloud-console].
1. In the drop-down menu at the top, select **Create a project**.
1. Give your project a name.
1. Make a note of the project ID, which might be different from the project
   name. The project ID is used in commands and in configurations.

[cloud-console]: https://console.cloud.google.com/

### Google Cloud Shell, Open JDK 11 setup:

To switch to an Open JDK 11 in a Cloud shell session, you can use:

```
   sudo update-alternatives --config java
   # And select the usr/lib/jvm/java-11-openjdk-amd64/bin/java version.
   # Also, set the JAVA_HOME variable for Maven to pick the correct JDK:
   export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
```

## Java 11 runtime
One way to deploy to App Engine Java 11 is directly from source.

* [`springboot-helloworld`](springboot-helloworld): Deploy a spring-boot application from source
* [`http-server`](http-server): Deploy an http application from source

Another way is using the Maven App Engine Plugin to deploy an executable [Uber JAR][uber-jar]. App Engine will automatically configure the `entrypoint` to run the JAR file. Use this method when your application requires dependencies that are located locally, such as the [`appengine-simple-jetty-main`](appengine-simple-jetty-main) artifact.

* [`gaeinfo`](gaeinfo): Build a JAR using the Maven JAR Plugin

In addition, App Engine allows you to execute the `java` command directly in the `app.yaml` `entrypoint` field, so you can further customize your app's startup.

* [`custom-entrypoint`](custom-entrypoint): Run a simple server
* [`helloworld-servlet`](helloworld-servlet): Run a WAR package servlet

With a custom `entrypoint`, you can also construct and package your application as a thin JAR (or an exploded JAR). When you deploy your application, the App Engine plugin will only upload the files that changed, rather than the entire [Uber JAR][uber-jar] package.

For more information on the Java 11 runtime, see
[Building an App](https://cloud.google.com/appengine/docs/standard/java11/building-app/)
and [Migrating your App Engine app from Java 8 to Java 11](https://cloud.google.com/appengine/docs/standard/java11/java-differences).


### Servlet Runtime

To migrate to the Java 11 runtime, your application must have a
`Main` class that starts a web server.
[`appengine-simple-jetty-main`](appengine-simple-jetty-main) is a shared artifact
that provides a Jetty Web Server for the servlet based runtime. Packaged as a
jar, the Main Class will load a war file, passed as an argument, as the
context root of the web application listening to port 8080.
Some samples create a `<sample-name>.war` which is used as an argument in the
App Engine `app.yaml` entrypoint field.


### App Engine Staging Directory

The App Engine Plugin will stage all the files to upload into App Engine
runtime in `${build.directory}/appengine-staging`. When deploying an
[Uber JAR][uber-jar], the JAR is automatically copied into this staging
directory and uploaded. It's possible to copy other files into this staging
directory (such as additional JVM Agents) and having them available in the
deployed App Engine runtime directory.

- To stage the files to be uploaded:
```
mvn appengine:stage
```

[uber-jar]: https://stackoverflow.com/questions/11947037/what-is-an-uber-jar
