# Google App Engine Standard Environment Samples for Java 11

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java11/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This is a repository that contains Java code samples for [Google App Engine][ae-docs]
standard Java 11 environment.

[ae-docs]: https://cloud.google.com/appengine/docs/java/

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

### appengine-simple-jetty-main

[appengine-simple-jetty-main](appengine-simple-jetty-main) is a shared artifact
that provides a Jetty Web Server for the servlet based runtime. Packaged as a
jar, the Main Class will load a war file, passed as an argument, as the
context root of the web application listening to port 8080.
Some samples create a `<sample-name>.war` which is used as an argument in the
App Engine app.yaml entrypoint field.
