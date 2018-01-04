# App Engine Standard & Google Cloud Endpoints Frameworks

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/endpoints-v2-guice/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use Google Cloud Endpoints Frameworks with Guice
on App Engine Standard.

## Build with Maven

### Adding the project ID to the sample API code

You must add the project ID obtained when you created your project to the
sample's `pom.xml` before you can deploy the code.

To add the project ID:

0. Edit the file `pom.xml`.

0. For `<endpoints.project.id>`, replace the value `YOUR_PROJECT_ID` with
your project ID.

0. Edit the file `src/main/java/com/example/echo/Echo.java` and
   `src/main/java/com/example/echo/EchoEndpointModule.java`.

0. Replace the value `YOUR-PROJECT-ID` with your project ID.

0. Save your changes.

### Building the sample project

To build the project:

    mvn clean package

### Generating the openapi.json file

To generate the required configuration file `openapi.json`:

    mvn endpoints-framework:openApiDocs

### Deploying the sample API to App Engine

To deploy the sample API:

0. Invoke the `gcloud` command to deploy the API configuration file:

         gcloud endpoints services deploy target/openapi-docs/openapi.json

0. Deploy the API implementation code by invoking:

         mvn appengine:deploy

    The first time you upload a sample app, you may be prompted to authorize the
    deployment. Follow the prompts: when you are presented with a browser window
    containing a code, copy it to the terminal window.

0. Wait for the upload to finish.

### Sending a request to the sample API

After you deploy the API and its configuration file, you can send requests
to the API.

To send a request to the API, from a command line, invoke the following `cURL`
command:

     curl \
         -H "Content-Type: application/json" \
         -X POST \
         -d '{"message":"echo"}' \
         https://$PROJECT_ID.appspot.com/_ah/api/echo/v1/echo

You will get a 200 response with the following data:

    {
     "message": "echo"
    }

## Build with gradle

### Adding the project ID to the sample API code

0. Edit the file `build.gradle`.

0. For `def projectId = 'YOUR_PROJECT_ID'`, replace the value `YOUR_PROJECT_ID`
with your project ID.

0. Edit the file `src/main/java/com/example/echo/Echo.java` and
   `src/main/java/com/example/echo/EchoEndpointModule.java`.

0. Replace the value `YOUR-PROJECT-ID` with your project ID.

0. Save your changes.

### Building the sample project

To build the project on unix-based systems:

    ./gradlew build

Windows users: Use `gradlew.bat` instead of `./gradlew`

<details>
 <summary>more details</summary>
 The project contains the standard java and war plugins and in addition to that it contains the following plugins:
 https://github.com/GoogleCloudPlatform/endpoints-framework-gradle-plugin for the endpoint related tasks and
 https://github.com/GoogleCloudPlatform/app-gradle-plugin for the appengine standard related tasks.

 Check the links for details about the available Plugin Goals and Parameters.
</details>

### Generating the openapi.json file

To generate the required configuration file `openapi.json`:

    ./gradlew endpointsOpenApiDocs

This results in a file in build/endpointsOpenApiDocs/openapi.json

### Deploying the sample API to App Engine

To deploy the sample API:

0. Invoke the `gcloud` command to deploy the API configuration file:

         gcloud endpoints services deploy build/endpointsOpenApiDocs/openapi.json

0. Deploy the API implementation code by invoking:

         ./gradlew appengineDeploy

    The first time you upload a sample app, you may be prompted to authorize the
    deployment. Follow the prompts: when you are presented with a browser window
    containing a code, copy it to the terminal window.

    <details>
    <summary>ERROR: (gcloud.app.deploy) The current Google Cloud project [...] does not contain an App Engine application.</summary>
    If you create a fresh cloud project that doesn't contain a appengine application you may receive this Error:

    ERROR: (gcloud.app.deploy) The current Google Cloud project [...] does not contain an App Engine application. Use `gcloud app create` to initialize an App Engine application within the project.

    In that case just execute `gcloud app create`, you will be asked to select a region and the app will be created. Then run gradle appengineDeploy again.
    </details>

0. Wait for the upload to finish.

### Sending a request to the sample API

After you deploy the API and its configuration file, you can send requests
to the API.

To send a request to the API, from a command line, invoke the following `cURL`
command:

     curl \
         -H "Content-Type: application/json" \
         -X POST \
         -d '{"message":"echo"}' \
         https://$PROJECT_ID.appspot.com/_ah/api/echo/v1/echo

You will get a 200 response with the following data:

    {
     "message": "echo"
    }
