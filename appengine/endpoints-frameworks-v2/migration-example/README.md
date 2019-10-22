# Hello World Google Cloud Endpoints for App Engine

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/endpoints-frameworks-v2/migration-example/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample provides an example of a [migration][7] from the prior version of
[Google Cloud Endpoints Frameworks][3] to new
[Google Cloud Endpoints Frameworks for App Engine][8] using a
**Discovery Document**. Additionally, this sample provides an example of using
the new App Engine Maven and Gradle plugins for deploying your Google App Engine
Standard applications.

This sample contains comments of how to use the prior Endpoints Frameworks as
well. For clarity, the prior Endpoints Frameworks and the new Endpoints
Frameworks are denoted as Endpoints Frameworks v1.0 and Endpoints Frameworks
v2.0, respectively.

Google Cloud Endpoints Frameworks v2.0 provides new functionality which may
require payment and uses an OpenAPI specification. The OpenAPI development
process is explained [here][8] and a quickstart is provided [here][9].

## Products
- [Google App Engine Standard][1]

## Language
- [Java][2]

## APIs
- [Google Cloud Endpoints Frameworks v2.0][8]
- [Google Cloud Endpoints Frameworks v1.0][3]

## Build and Deployment Plugins
- [Google App Engine Maven plugin][14]
- [Google App Engine Gradle plugin][15]

## Discovery Document and Client Library Generation Plugins
- [Google Cloud Endpoints Frameworks Maven Plugin][10]
- [Google Cloud Endpoints Frameworks Gradle Plugin][11]

## Setup
1. Change `YOUR-PROJECT-ID` with your project id in the hostname parameter
   defined in either the Maven or Gradle build script. Hostname is used when a
   discovery document is generated.

   - Maven - pom.xml
   - Gradle - build.gradle

1. [Optional]: User Authenticating with Google Accounts in Web Clients

    1. Update the `WEB_CLIENT_ID` in [Constants.java](src/main/java/com/example/helloendpoints/Constants.java)
      to reflect the web client ID you have registered in the
      [Credentials on Developers Console for OAuth 2.0 client IDs][6].

    1. Update the value of `google.devrel.samples.helloendpoints.CLIENT_ID` in
       [base.js](src/main/webapp/js/base.js) to reflect the web client ID you
       have registered in the
       [Credentials on Developers Console for OAuth 2.0 client IDs][6].

1. [Optional]: User Authenticating with Google Accounts in other Applications
   Types

    - Inside [Constants.java](src/main/java/com/example/helloendpoints/Constants.java)
      you will find placeholders for Android applications using Google Accounts
      client IDs registered in the
      [Credentials on Developers Console for OAuth 2.0 client IDs][6].

    - Note: iOS support should work but has not been fully tested.

    - These client IDs are used when defining annotation for this sample API
      found in [Greetings.java](src/main/java/com/example/helloendpoints/Greetings.java).

    - You can read more about different user authentication supported [here][12].


1. [Optional]: Use Cloud Endpoints Frameworks v2.0 Maven and Gradle
   client library generation plugins with Cloud Endpoints Frameworks v1.0.

    - Uncomment `Endpoints Frameworks v1.0` sections and comment
        `Endpoints Frameworks v2.0` sections in the following files.

      ```
        pom.xml
        build.gradle
        src/main/webapp/WEB-INF/web.xml
      ```

## Build and Deployment

###  Maven

1. Build and Run the application locally at [http://localhost:8080][5] by using:

    `mvn clean appengine:run`

1. Explore local server's API explorer by browsing to:

    [http://localhost:8080/_ah/api/explorer][13]

1. Generate the discovery document located at
   `target/discovery-docs/helloworld-v1-rest.discovery` by using:

    `mvn endpoints-framework:discoveryDocs`

1. Generate the client library located at
   `target/client-libs/helloworld-v1-java.zip` by using:

    `mvn endpoints-framework:clientLibs`

1. Build and Deploy your application to Google App Engine by using:

    `mvn clean appengine:deploy`

### Gradle

1. Build and Run the application locally at [http://localhost:8080][5] by using:

    `./gradlew clean appengineRun`

   Windows users: Use `gradlew.bat` instead of `./gradlew`

1. Explore local server's API explorer by browsing to:

    [http://localhost:8080/_ah/api/explorer][13]

1. Generate the discovery document located at
   `build/endpointsDiscoveryDocs/helloworld-v1-rest.discovery` by using:

    `./gradlew endpointsDiscoveryDocs`

1. Generate the client library located at
   `build/endpointsClientLibs/helloworld-v1-java.zip` by using:

    `./gradlew endpointsClientLibs`

1. Deploy your application to Google App Engine by using:

    `./gradlew appengineDeploy`

[1]: https://cloud.google.com/appengine/docs/java/
[2]: http://java.com/en/
[3]: https://cloud.google.com/endpoints/docs/frameworks/legacy/v1/java
[4]: https://cloud.google.com/appengine/docs/java/tools/maven
[5]: http://localhost:8080/
[6]: https://console.developers.google.com/project/_/apiui/credential
[7]: https://cloud.google.com/endpoints/docs/frameworks/legacy/v1/java/migrating
[8]: https://cloud.google.com/endpoints/docs/frameworks/java/about-cloud-endpoints-frameworks
[9]: https://cloud.google.com/endpoints/docs/frameworks/java/quickstart-frameworks-java
[10]: https://github.com/GoogleCloudPlatform/endpoints-framework-maven-plugin
[11]: https://github.com/GoogleCloudPlatform/endpoints-framework-gradle-plugin
[12]: https://cloud.google.com/endpoints/docs/authenticating-users-frameworks
[13]: http://localhost:8080/_ah/api/explorer
[14]: https://github.com/GoogleCloudPlatform/app-maven-plugin
[15]: https://github.com/GoogleCloudPlatform/app-gradle-plugin
