# Google Cloud Run Java Samples

[![Open in Cloud Shell][shell_img]][shell_link]

[shell_img]: http://gstatic.com/cloudssh/images/open-btn.png
[shell_link]: https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=blog/README.md

This directory contains samples for [Google Cloud Run](https://cloud.run). [Cloud Run][run_docs] runs stateless [containers](https://cloud.google.com/containers/) on a fully managed environment or in your own GKE cluster.

## Samples

|           Sample                |        Description       |     Deploy    |
| ------------------------------- | ------------------------ | ------------- |
|[Hello World](helloworld/) | Quickstart | [<img src="https://storage.googleapis.com/cloudrun/button.svg" alt="Run on Google Cloud" height="30">][run_button_helloworld] |
|[Cloud Pub/Sub](pubsub/) | Handling Pub/Sub push messages | [<img src="https://storage.googleapis.com/cloudrun/button.svg" alt="Run on Google Cloud" height="30">][run_button_pubsub] |
|[System Packages](system-package/) | Using system packages with containers | [<img src="https://storage.googleapis.com/cloudrun/button.svg" alt="Run on Google Cloud" height="30">][run_button_sys_package] |
|[Image Magick](image-processing/) | Event-driven image analysis & transformation | [<img src="https://storage.googleapis.com/cloudrun/button.svg" alt="Run on Google Cloud" height="30">][run_button_image] |
|[Manual Logging](logging-manual/) | Structured logging for Stackdriver | [<img src="https://storage.googleapis.com/cloudrun/button.svg" alt="Run on Google Cloud" height="30">][run_button_log] |
|[Local Troubleshooting](hello-broken/) | Broken services for local troubleshooting tutorial | [<img src="https://storage.googleapis.com/cloudrun/button.svg" alt="Run on Google Cloud" height="30">][run_button_broken] |
|[Cloud SQL (MySQL)][mysql]        | Use MySQL with Cloud Run | [<img src="https://storage.googleapis.com/cloudrun/button.svg" alt="Run on Google Cloud" height="30">][run_button_sql] |
|[Events - Pub/Sub](events-pubsub/) | Events for Cloud Run with Pub/Sub | [<img src="https://storage.googleapis.com/cloudrun/button.svg" alt="Run on Google Cloud" height="30">][run_button_events_pubsub] |
|[Anthos Events - Pub/Sub](events-pubsub/anthos.md) | Events for Cloud Run on Anthos with Pub/Sub | - |
|[Events - GCS](events-gcs/) | Events for Cloud Run with GCS | [<img src="https://storage.googleapis.com/cloudrun/button.svg" alt="Run on Google Cloud" height="30">][run_button_events_gcs] |
|[Anthos Events - GCS](events-gcs/anthos.md) | Events for Cloud Run on Anthos with GCS | - |
|[Authentication](authentication/) | Make an authenticated request by retrieving a JSON Web Tokens (JWT) | [<img src="https://storage.googleapis.com/cloudrun/button.svg" alt="Run on Google Cloud" height="30">][run_button_auth] |

For more Cloud Run samples beyond Java, see the main list in the [Cloud Run Samples repository](https://github.com/GoogleCloudPlatform/cloud-run-samples).

## Jib

These samples use [Jib](https://github.com/GoogleContainerTools/jib) to
build Docker images using common Java tools. Jib optimizes container builds
without the need for a Dockerfile or having [Docker](https://www.docker.com/)
installed. Learn more about [how Jib works](https://github.com/GoogleContainerTools/jib).

## Setup

1. [Set up for Cloud Run development](https://cloud.google.com/run/docs/setup)

1. Clone this repository:

    ```
    git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
    ```

1. In the samples's `pom.xml`, update the image field for the `jib-maven-plugin`
with your Google Cloud Project Id:

    ```XML
    <plugin>
      <groupId>com.google.cloud.tools</groupId>
      <artifactId>jib-maven-plugin</artifactId>
      <version>2.0.0</version>
      <configuration>
        <to>
          <image>gcr.io/PROJECT_ID/SAMPLE_NAME</image>
        </to>
      </configuration>
    </plugin>
    ```

## How to run a sample locally

1. [Build the sample container using Jib](https://github.com/GoogleContainerTools/jib):

    ```Bash
    mvn compile jib:dockerBuild
    ```

1. [Run containers locally](https://cloud.google.com/run/docs/testing/local) by
replacing `PROJECT_ID` and `SAMPLE_NAME` with your values.

    With the built container:

    ```Bash
    PORT=8080 && docker run --rm -p 9090:${PORT} -e PORT=${PORT} gcr.io/PROJECT_ID/SAMPLE_NAME
    ```

    Injecting your service account key for access to GCP services:

    ```Bash
    PORT=8080 && docker run \
       -p 9090:${PORT} \
       -e PORT=${PORT} \
       -e K_SERVICE=dev \
       -e K_CONFIGURATION=dev \
       -e K_REVISION=dev-00001 \
       -e GOOGLE_APPLICATION_CREDENTIALS=/tmp/keys/[FILE_NAME].json \
       -v $GOOGLE_APPLICATION_CREDENTIALS:/tmp/keys/[FILE_NAME].json:ro \
        gcr.io/PROJECT_ID/SAMPLE_NAME
    ```

    * Use the --volume (-v) flag to inject the credential file into the container
      (assumes you have already set your `GOOGLE_APPLICATION_CREDENTIALS`
      environment variable on your machine)

    * Use the --environment (-e) flag to set the `GOOGLE_APPLICATION_CREDENTIALS`
      variable inside the container

1. Open http://localhost:9090 in your browser.

Learn more about [testing your container image locally.][testing]

## Deploying

1. [Build the sample container using Jib](https://github.com/GoogleContainerTools/jib):

    ```
    mvn compile jib:build
    ```

    **Note**: Using the image tag `gcr.io/PROJECT_ID/SAMPLE_NAME` automatically
    pushes the image to [Google Container Registry](https://cloud.google.com/container-registry/).

1. Deploy to Cloud Run by replacing `PROJECT_ID` and `SAMPLE_NAME` with your values:

    ```bash
    gcloud run deploy --image gcr.io/PROJECT_ID/SAMPLE_NAME
    ```

## Next Steps
* See [building containers][run_build] and [deploying container images][run_deploy]
  for more information.

* [Dockerize a Spring Boot app][jib-tutorial] without a Dockerfile using [Jib][jib].

* To improve [Tomcat startup time][startup], add
  `-Djava.security.egd=file:/dev/./urandom` to the Dockerfile's `CMD`
  instructions. This is acceptable for many applications. However, if your
  application does it's own key generation, such as for SSL, and you require
  greater security, you may prefer to not set `java.security.egd`.


[run_docs]: https://cloud.google.com/run/docs/
[run_build]: https://cloud.google.com/run/docs/building/containers
[run_deploy]: https://cloud.google.com/run/docs/deploying
[run_button_helloworld]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/helloworld
[run_button_broken]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/hello-broken
[run_button_image]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/image-processing
[run_button_log]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/logging-manual
[run_button_pubsub]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/pubsub
[run_button_events_gcs]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/events-gcs
[run_button_events_pubsub]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/events-pubsub
[run_button_auth]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/authentication
[run_button_sys_package]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/system-package
[push-pull]: https://cloud.google.com/container-registry/docs/pushing-and-pulling
[jib]: https://github.com/GoogleContainerTools/jib
[jib-tutorial]: https://github.com/GoogleContainerTools/jib/tree/master/examples/spring-boot
[startup]: https://cwiki.apache.org/confluence/display/TOMCAT/HowTo+FasterStartUp
[testing]: https://cloud.google.com/run/docs/testing/local#running_locally_using_docker_with_access_to_services
[mysql]: ../cloud-sql/mysql/servlet
[run_button_sql]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=cloud-sql/mysql/servlet
