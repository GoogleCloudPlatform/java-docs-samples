HelloWorld for App Engine Standard (Java 21)
============================

This sample demonstrates how to deploy an application on Google App Engine.

See the [Google App Engine standard environment documentation][ae-docs] for more
detailed instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/


* [Java 21](https://www.oracle.com/java/technologies/downloads/)
* [Maven](https://maven.apache.org/download.cgi) (at least 3.5)
* [Google Cloud SDK](https://cloud.google.com/sdk/) (aka gcloud)

## Setup

• Download and initialize the [Cloud SDK](https://cloud.google.com/sdk/)

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
  <version>2.8.0</version>
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

    mvn package appengine:run

To use visit: http://localhost:8080/

### Deploying

    mvn package appengine:deploy

To use visit:  https://YOUR-PROJECT-ID.appspot.com

### Testing

    mvn verify

As you add / modify the source code (`src/main/java/...`) it's very useful to add [unit testing](https://cloud.google.com/appengine/docs/java/tools/localunittesting)
to (`src/main/test/...`).  The following resources are quite useful:

* [Junit4](http://junit.org/junit4/)
* [Mockito](http://mockito.org/)
* [Truth](http://google.github.io/truth/)

## Gradle

### Running locally

    ./gradlew appengineRun

To use vist: http://localhost:8080/

### Deploying

    ./gradlew appengineDeploy
