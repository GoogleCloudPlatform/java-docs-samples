SpringBoot HelloWorld for App Engine Standard (Java 8)
============================

This sample demonstrates how to deploy a Java 8 Spring Boot application on
Google App Engine. The Java 8 App Engine runtime expects a
[WAR file to be uploaded](https://cloud.google.com/appengine/docs/standard/java/tools/uploadinganapp).

Note: If your project's root directory includes other Maven modules, EAR packages,
or .jar files that do not define an App Engine service, the command will fail when
configuration files are not found.

See the [Google App Engine standard environment documentation][ae-docs] for more
detailed instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven](https://maven.apache.org/download.cgi) (at least 3.5)
* [Google Cloud SDK](https://cloud.google.com/sdk/) (aka gcloud command line tool)

## Setup

* Download and initialize the [Cloud SDK](https://cloud.google.com/sdk/)

```
gcloud init
```

* Create an App Engine app within the current Google Cloud Project

```
gcloud app create
```

* In the `pom.xml`, update the [App Engine Maven Plugin](https://cloud.google.com/appengine/docs/standard/java/tools/maven-reference)
with your Google Cloud Project Id:

```
<plugin>
  <groupId>com.google.cloud.tools</groupId>
  <artifactId>appengine-maven-plugin</artifactId>
  <version>2.2.0</version>
  <configuration>
    <projectId>myProjectId</projectId>
    <version>GCLOUD_CONFIG</version>
  </configuration>
</plugin>
```
**Note:** `GCLOUD_CONFIG` is a special version for autogenerating an App Engine
version. Change this field to specify a specific version name.

## Maven
### Running locally

`mvn package appengine:run`

To use vist: http://localhost:8080/

### Deploying

`mvn package appengine:deploy`

To use vist:  https://YOUR-PROJECT-ID.appspot.com

## Testing

`mvn verify`

As you add / modify the source code (`src/main/java/...`) it's very useful to add [unit testing](https://cloud.google.com/appengine/docs/java/tools/localunittesting)
to (`src/main/test/...`).  The following resources are quite useful:

* [Junit4](http://junit.org/junit4/)
* [Mockito](http://mockito.org/)
* [Truth](http://google.github.io/truth/)


For further information, consult the
[Java App Engine](https://developers.google.com/appengine/docs/java/overview) documentation.

## Steps to convert a Spring Boot application for App Engine Standard
### Use the WAR packaging
You must use WAR packaging to deploy into Google App Engine Standard.

If you generate a Spring Boot project from [start.spring.io](http://start.spring.io/),
make sure you *switch to the full version* view of the initializer site, and select *WAR*
packaging.

If you have an existing `JAR` packaging project, you can convert it into a `WAR` project by:
1. In `pom.xml`, change `<packaging>jar</packaging>` to `<packaging>war</packaging>`
1. Create a new `SpringBootServletInitializer` implementation:

```java
public class ServletInitializer extends SpringBootServletInitializer {
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
  return application.sources(YourApplication.class);
  }
}
```

### Remove Tomcat Starter
Google App Engine Standard deploys your `WAR` into a Jetty server. Spring Boot's starter
includes Tomcat by default. This will introduce conflicts. Exclude Tomcat dependencies:
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <exclusions>
    <exclusion>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-tomcat</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

Do not include the Jetty dependencies. But you must include Servlet API dependency:
```xml
<dependency>
  <groupId>javax.servlet</groupId>
  <artifactId>javax.servlet-api</artifactId>
  <version>3.1.0</version>
  <scope>provided</scope>
</dependency>
```

### Add App Engine Standard Plugin
In the `pom.xml`, add the App Engine Standard plugin:
```xml
<plugin>
  <groupId>com.google.cloud.tools</groupId>
  <artifactId>appengine-maven-plugin</artifactId>
  <version>2.2.0</version>
</plugin>
```

This plugin is used to run local development server as well as deploying the application
into Google App Engine.

### Add App Engine Configuration
Add a `src/main/webapp/WEB-INF/appengine-web.xml`:
```xml
<appengine-web-app xmlns="http://appengine.google.com/ns/1.0">
  <version>1</version>
  <threadsafe>true</threadsafe>
  <runtime>java8</runtime>
</appengine-web-app>
```

This configure is required for applications running in Google App Engine.

### Exclude JUL to SLF4J Bridge
Spring Boot's default logging bridge conflicts with Jetty's logging system.
To be able to capture the Spring Boot startup logs, you need to exclude
`org.slf4j:jul-to-slf4j` dependency.  The easiest way to do this is to
set the dependency scope to `provided`, so that it won't be included in
the `WAR` file:

```xml
<!-- Exclude any jul-to-slf4j -->
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>jul-to-slf4j</artifactId>
  <scope>provided</scope>
</dependency>
```

### Out of memory errors

With Spring Boot >= 1.5.6, you may run into out of memory errors on startup.
Please follow these instructions to work around this issue:

1. Inside src/main/resources, adding a logging.properties file with:
```ini
.level = INFO
```
2. Inside src/main/webapp/WEB-INF/appengine-web.xml, add a config that points to the new logging.properties file.
```xml
<system-properties>
  <property name="java.util.logging.config.file" value="WEB-INF/classes/logging.properties"/>
</system-properties>
```
