# Google App Engine Standard Environment Samples for Java 17 Bundled Services

This is a repository that contains Java code samples for [Google App Engine
standard environment Java 17 Bundled Services][ae-docs].
The Google App Engine standard environment Java 17 Bundled Services is an environment 
as close as possible as the original Google App Engine standard environment Java 8
which is using WAR packaging, GAE APIs and configured via appengine-web.xml instead of app.yaml

[ae-docs]: https://cloud.google.com/appengine/docs/standard/java-gen2/services/access

## Prerequisites

### Download Maven

These samples use the [Apache Maven][maven] build system. Before getting
started, be sure to [download][maven-download] and [install][maven-install] it.
When you use Maven as described here, it will automatically download the needed
client libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html
[java8-samples]: https://github.com/GoogleCloudPlatform/java-docs-samples/tree/main/appengine-java8#readme

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


## Development differences between App Engine Java8 and Java17 Bundled Services

The only difference between a Java8 application and a Java17 application is in the `appengine-web.xml` file
where you need to define the Java17 runtime and declare you need the App Engine APIs:

```
<appengine-web-app xmlns="http://appengine.google.com/ns/1.0">
    <runtime>java17</runtime>
    <app-engine-apis>true</app-engine-apis>
</appengine-web-app>
```

While the Java17 runtime is in Beta, in order to deploy the application, you can use the `beta` value for the `gcloudMode` Cloud SDK parameter like:

```
 mvn appengine:deploy -Dapp.deploy.gcloudMode=beta
```


Everything else should remain the same in terms of App Engine APIs access, WAR project packaging, and deployment.
This way, it should  be easy to migrate your existing GAE Java8 applications to GAE Java17.

## Samples

### App Engine Datastore with Java17

This sample demonstrates how to use the App Engine Datastore APIs in a Java17 web application on Google App Engine Java17.

- [Documentation][ae-docs]
- [Code](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/main/appengine-standard-java11-bunded-services/datastore)

### How to change an App Engine Java 8 application to App Engine Java17 bundled services

You can execute the following steps to transform the java8 appengine-web.xml file to a java17 appengine-web.xml file:

```
	 git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
	 cd java-docs-samples
	 cp -pr appengine-java8 /tmp/java17-samples
	 cd /tmp/java17-samples
	 # On Linux:
	 shopt -s globstar dotglob
     for f in **/appengine-web.xml; do sed -i 's.<runtime>java8</runtime>.<runtime>java17</runtime><app-engine-apis>true</app-engine-apis>.' ${f}; done 
	 # on MacOS
     for f in **/appengine-web.xml; do sed -i'' -e 's.<runtime>java8</runtime>.<runtime>java17</runtime><app-engine-apis>true</app-engine-apis>.' ${f}; done
```
	 
You will see in the `tmp/java17` directory all the correct code samples to compile and deploy to the Java17 AppEngine runtime, with bundled services.
Just follow the same documentation as the [Java8 samples][java8-samples].
	 
	 

