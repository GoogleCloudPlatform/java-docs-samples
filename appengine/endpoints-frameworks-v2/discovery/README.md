# Hello World Cloud Endpoints v1.0 using Endpoints Frameworks v2.0

This is an example of a [migrated][7] Cloud Endpoints v1.0 application using
Endpoints Frameworks v2.0. If you're not ready to migrate your application,
this example also provides information of how to use Endponts Frameworks v2.0 Maven
and Gradle discovery and client library generation plugins with existing
Endpoints Frameworks v1.0 projects.

# Hello World using Cloud Endpoints v1.0 Application

A "hello world" application for Google Cloud Endpoints in Java.

## Products
- [App Engine][1]

## Language
- [Java][2]

## APIs
- [Google Cloud Endpoints][3]
- [Google App Engine Maven plugin][4]

## Setup Instructions
1. [Optional]: These sub steps are not required but will enable the "Authenticated
Greeting" functionality.

    1. Update the values in [src/main/java/com/example/helloendpoints/Constants.java](src/main/java/com/example/helloendpoints/Constants.java)
      to reflect the web client ID you have registered in the [Credentials on Developers Console for OAuth 2.0 client IDs][6].

    1. Update the value of `google.devrel.samples.helloendpoints.CLIENT_ID` in
[src/main/webapp/js/base.js](src/main/webapp/js/base.js) to reflect the web client ID you have registered in the
[Credentials on Developers Console for OAuth 2.0 client IDs][6].

1. [Optional]: Use Endpoints Frameworks v2.0 Maven and Gradle disovery and
   client library generation plugins with Endpoints Frameworks v1.0.

    * Uncomment commented Endpoints Frameworks v1.0 sections and comment
        Endpoints Frameworks v2.0 sections in the following files.

      ```
        pom.xml
        build.gradle
        src/main/webapp/WEB-INF/web.xml
      ```

###  Maven

1. Build a fresh binary with

    `mvn clean compile`

1. Run the application locally at [localhost:8080][5] with

    `mvn appengine:run`

1. Generate the client library in a zip file named `helloworld-v1-java.zip` with

    `mvn endpoints-framework:clientLibs`

1. Deploy your application to Google App Engine with

    `mvn appengine:deploy`

### Gradle

1. Build a fresh binary with

    `gradle clean compileJava`

1. Run the application locally at [localhost:8080][5] with

    `gradle appengineRun`

1. Generate the client library in a zip file named `helloworld-v1-java.zip` with

    `gradle endpointsClientLibs`

1. Deploy your application to Google App Engine with

    `gradle appengineDeploy`

[1]: https://developers.google.com/appengine
[2]: http://java.com/en/
[3]: https://developers.google.com/appengine/docs/java/endpoints/
[4]: https://developers.google.com/appengine/docs/java/tools/maven
[5]: https://localhost:8080/
[6]: https://console.developers.google.com/project/_/apiui/credential
[7]: https://cloud.google.com/appengine/docs/java/endpoints/migrating
