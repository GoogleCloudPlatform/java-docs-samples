# Cloud Run Endpoints Sample

This sample shows how to create a Cloud Endpoints service.

Use it with the [Getting Started with Endpoints for Cloud Run with ESPv2 tutorial][tutorial_link].

For more details on how to work with this sample read the [Google Cloud Run Java Samples README][tutorial_link].

[![Run in Google Cloud][run_img]][run_link]

## Dependencies

* **Spring Boot**: Web server framework.
* **SpringDoc OpenApi**: API documentation generation tool.
* **Jib**: Container build tool.

## Generating the Endpoints spec


1. Update `PROJECT_ID` in `pom.xml` with your GCP Project Id:

    ```
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.target>11</maven.compiler.target>
        <maven.compiler.source>11</maven.compiler.source>
        <endpoints.project.id>PROJECT ID</endpoints.project.id>
    </properties>
    ```

1. Update `<CLOUD_RUN_HOSTNAME>` and `<CLOUD_RUN_SERVICE_URL>` in `EndpointsApplication.java` according to variable

definitions at [Getting Started with Endpoints for Cloud Run with ESPv2](https://cloud.google.com/endpoints/docs/openapi/get-started-cloud-run#reserve_hostname)
 (Reserving a Cloud Run hostname).

1. Run `mvn clean install` (or alternatively just `mvn verify`) to use the SpringDoc Maven Plugin to generate a base
    openapi file in the root directory.

    You are able to customize the output filename by changing its plugin configuration `outputFileName` in pom.xml.
    
    SpringDoc Openapi Maven Plugin runs during the integration test lifecycle phase in order to pull down a Swagger 3.0
    openapi specification and automatically write into a file.
    
1. Manually, you must switch the generated file's swagger version from 3.0 to 2.0. At the end, it should look like the
    file openapi-run.yaml in this demo project. If you are using an IDE like IntelliJ IDEA Ultimate, after switching the
    version number to 2.0, it should tell you what's wrong with the file.
    
1. Follow the [Getting Started with Endpoints for Cloud Run with ESPv2](https://cloud.google.com/endpoints/docs/openapi/get-started-cloud-run#deploy_configuration)
    guide (Deploying the Endpoints configuration).

[run_img]: https://storage.googleapis.com/cloudrun/button.svg
[run_link]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/markdown-preview
[tutorial_link]: https://cloud.google.com/endpoints/docs/openapi/get-started-cloud-run
