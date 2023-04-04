# Google App Engine Standard Environment Samples for Java 11 Bundled Services

This is a repository that contains Java code samples for [Google App Engine
standard environment Java 11 Bundled Services][ae-docs].
The Google App Engine standard environment Java 11 Bundled Services is an environment 
as close as possible as the original Google App Engine standard environment Java 8
which is using WAR packaging, GAE APIs and configured via appengine-web.xml instead of app.yaml

[ae-docs]: https://cloud.google.com/appengine/docs/standard/java11/services/access

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


## Development differences between App Engine Java8 and Java11 Bundled Services

The only differences between a Java8 application and a Java11 application are the addition of the bundled services JAR, and an added line in the `appengine-web.xml` file
where you need to define the Java11 runtime and declare you need the App Engine APIs:

In `appengine-web.xml`:
```XML
<appengine-web-app xmlns="http://appengine.google.com/ns/1.0">
    <runtime>java11</runtime>
    <app-engine-apis>true</app-engine-apis>
</appengine-web-app>
```

In your `pom.xml`'s `<dependencies>`:
```XML
<dependency>
    <groupId>com.google.appengine</groupId>
    <artifactId>appengine-api-1.0-sdk</artifactId>
    <version>2.0.4</version> <!-- or later-->
</dependency>
```


```shell
 mvn appengine:deploy
```


Everything else should remain the same in terms of App Engine APIs access, WAR project packaging, and deployment.
This way, it should  be easy to migrate your existing GAE Java8 applications to GAE Java11.

## Samples

### App Engine Datastore with Java11

This sample demonstrates how to use the App Engine Datastore APIs in a Java11 web application on Google App Engine Java11.

- [Documentation][ae-docs]
- [Code](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/main/appengine-standard-java11-bunded-services/datastore)

### How to change an App Engine Java 8 application to App Engine Java11 bundled services

You can execute the following steps to transform the java8 appengine-web.xml file to a java11 appengine-web.xml file:

```shell
git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
cd java-docs-samples
cp -pr appengine-java8 /tmp/java11-samples
cd /tmp/java11-samples
# On Linux:
shopt -s globstar dotglob
for f in **/appengine-web.xml; do sed -i 's.<runtime>java8</runtime>.<runtime>java11</runtime><app-engine-apis>true</app-engine-apis>.' ${f}; done 
# on MacOS
for f in **/appengine-web.xml; do sed -i'' -e 's.<runtime>java8</runtime>.<runtime>java11</runtime><app-engine-apis>true</app-engine-apis>.' ${f}; done
 ```
	 
You will see in the `tmp/java11` directory all the correct code samples to compile and deploy to the Java11 AppEngine runtime, with bundled services.
Just follow the same documentation as the [Java8 samples][java8-samples].
	 
	 

